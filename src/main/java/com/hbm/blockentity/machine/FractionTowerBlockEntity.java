package com.hbm.blockentity.machine;

import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachineFractionTower}. OpenComputers skipped.
 */
public class FractionTowerBlockEntity extends BlockEntity {
    public static final int TANK_CAPACITY = 4_000;
    public static final int STACK_OFFSET = 3;

    private Fluid inputType = null;
    private final FluidTank input = tank();
    private final FluidTank left = tank();
    private final FluidTank right = tank();
    private final IFluidHandler fluids = new Handler();
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);

    public FractionTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_FRACTION_TOWER.get(), pos, state);
    }

    public FluidTank getInput() {
        return input;
    }

    public FluidTank getLeft() {
        return left;
    }

    public FluidTank getRight() {
        return right;
    }

    public IFluidHandler getFluidHandler() {
        return fluids;
    }

    public Fluid getInputType() {
        return inputType != null ? inputType : defaultInput();
    }

    public void setInputType(Fluid type) {
        if (type == null || FractionRecipes.get(type) == null) {
            return;
        }
        if (getInputType() == type) {
            return;
        }
        int amount = input.getFluidAmount();
        inputType = type;
        input.setFluid(amount > 0 ? new FluidStack(type, amount) : FluidStack.EMPTY);
        left.setFluid(FluidStack.EMPTY);
        right.setFluid(FluidStack.EMPTY);
        onChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FractionTowerBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.transferStack(level, pos);
        if (level.getGameTime() % FractionRecipes.TICK_DELAY == 0) {
            be.fractionate();
        }
        be.onChanged();
    }

    private void transferStack(Level level, BlockPos pos) {
        BlockEntity above = level.getBlockEntity(pos.above(STACK_OFFSET));
        if (!(above instanceof FractionTowerBlockEntity frac)) {
            return;
        }
        frac.setInputType(getInputType());
        int oil = Math.min(input.getFluidAmount(), frac.input.getCapacity() - frac.input.getFluidAmount());
        int pullLeft = Math.min(frac.left.getFluidAmount(), left.getCapacity() - left.getFluidAmount());
        int pullRight = Math.min(frac.right.getFluidAmount(), right.getCapacity() - right.getFluidAmount());
        if (oil > 0) {
            FluidStack moved = input.drain(oil, IFluidHandler.FluidAction.EXECUTE);
            frac.input.fill(moved, IFluidHandler.FluidAction.EXECUTE);
        }
        if (pullLeft > 0) {
            FluidStack moved = frac.left.drain(pullLeft, IFluidHandler.FluidAction.EXECUTE);
            left.fill(moved, IFluidHandler.FluidAction.EXECUTE);
        }
        if (pullRight > 0) {
            FluidStack moved = frac.right.drain(pullRight, IFluidHandler.FluidAction.EXECUTE);
            right.fill(moved, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void fractionate() {
        FractionRecipes.Outputs recipe = FractionRecipes.get(getInputType());
        if (recipe == null) {
            return;
        }
        if (input.getFluidAmount() < FractionRecipes.INPUT_MB) {
            return;
        }
        if (left.getFluidAmount() + recipe.leftMb() > left.getCapacity()
                || right.getFluidAmount() + recipe.rightMb() > right.getCapacity()) {
            return;
        }
        input.drain(FractionRecipes.INPUT_MB, IFluidHandler.FluidAction.EXECUTE);
        left.fill(new FluidStack(recipe.left(), recipe.leftMb()), IFluidHandler.FluidAction.EXECUTE);
        right.fill(new FluidStack(recipe.right(), recipe.rightMb()), IFluidHandler.FluidAction.EXECUTE);
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ResourceLocation key = ForgeRegistries.FLUIDS.getKey(getInputType());
        if (key != null) {
            tag.putString("InputType", key.toString());
        }
        tag.put("Input", input.writeToNBT(new CompoundTag()));
        tag.put("Left", left.writeToNBT(new CompoundTag()));
        tag.put("Right", right.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("InputType")) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("InputType")));
            if (fluid != null) {
                inputType = fluid;
            }
        }
        if (tag.contains("Input")) {
            input.readFromNBT(tag.getCompound("Input"));
        }
        if (tag.contains("Left")) {
            left.readFromNBT(tag.getCompound("Left"));
        }
        if (tag.contains("Right")) {
            right.readFromNBT(tag.getCompound("Right"));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidOptional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    private FluidTank tank() {
        return new FluidTank(TANK_CAPACITY) {
            @Override
            protected void onContentsChanged() {
                onChanged();
            }
        };
    }

    private static Fluid defaultInput() {
        return ModFluids.HEAVYOIL.source.get();
    }

    private final class Handler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 3;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> typedView(input, getInputType());
                case 1 -> left.getFluid();
                case 2 -> right.getFluid();
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank >= 0 && tank < 3 ? TANK_CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0 && !stack.isEmpty() && stack.getFluid() == getInputType();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty() || resource.getFluid() != getInputType()) {
                return 0;
            }
            return input.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            if (!left.getFluid().isEmpty() && left.getFluid().getFluid() == resource.getFluid()) {
                return left.drain(resource, action);
            }
            if (!right.getFluid().isEmpty() && right.getFluid().getFluid() == resource.getFluid()) {
                return right.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!left.getFluid().isEmpty()) {
                return left.drain(maxDrain, action);
            }
            return right.drain(maxDrain, action);
        }
    }

    private static FluidStack typedView(FluidTank tank, Fluid type) {
        if (!tank.getFluid().isEmpty()) {
            return tank.getFluid();
        }
        return new FluidStack(type, 0);
    }
}
