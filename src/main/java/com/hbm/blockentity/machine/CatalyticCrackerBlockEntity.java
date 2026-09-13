package com.hbm.blockentity.machine;

import com.hbm.fluid.SteamCycle;
import com.hbm.inventory.recipes.CrackingRecipes;
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
 * 1.7.10 {@code TileEntityMachineCatalyticCracker}. OpenComputers skipped.
 */
public class CatalyticCrackerBlockEntity extends BlockEntity {
    public static final int OIL_CAP = 4_000;
    public static final int STEAM_CAP = 8_000;
    public static final int SPENT_CAP = 800;

    private Fluid oilType = null;
    private final FluidTank oil = tank(OIL_CAP);
    private final FluidTank steam = tank(STEAM_CAP);
    private final FluidTank left = tank(OIL_CAP);
    private final FluidTank right = tank(OIL_CAP);
    private final FluidTank spent = tank(SPENT_CAP);
    private final IFluidHandler fluids = new Handler();
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);

    public CatalyticCrackerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_CATALYTIC_CRACKER.get(), pos, state);
    }

    public FluidTank getOil() {
        return oil;
    }

    public FluidTank getSteam() {
        return steam;
    }

    public FluidTank getLeft() {
        return left;
    }

    public FluidTank getRight() {
        return right;
    }

    public FluidTank getSpent() {
        return spent;
    }

    public IFluidHandler getFluidHandler() {
        return fluids;
    }

    public Fluid getOilType() {
        return oilType != null ? oilType : defaultOil();
    }

    public void setOilType(Fluid type) {
        if (type == null || CrackingRecipes.get(type) == null) {
            return;
        }
        if (getOilType() == type) {
            return;
        }
        int amount = oil.getFluidAmount();
        oilType = type;
        oil.setFluid(amount > 0 ? new FluidStack(type, amount) : FluidStack.EMPTY);
        left.setFluid(FluidStack.EMPTY);
        right.setFluid(FluidStack.EMPTY);
        onChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CatalyticCrackerBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % CrackingRecipes.TICK_DELAY == 0) {
            be.crack();
        }
        be.onChanged();
    }

    private void crack() {
        CrackingRecipes.Outputs recipe = CrackingRecipes.get(getOilType());
        if (recipe == null) {
            return;
        }
        for (int i = 0; i < CrackingRecipes.OPS_PER_PULSE; i++) {
            if (oil.getFluidAmount() < CrackingRecipes.INPUT_MB
                    || steam.getFluidAmount() < CrackingRecipes.STEAM_MB
                    || !SteamCycle.isSteam(steam.getFluid().getFluid())) {
                return;
            }
            if (left.getFluidAmount() + recipe.leftMb() > left.getCapacity()
                    || (recipe.right() != null && right.getFluidAmount() + recipe.rightMb() > right.getCapacity())
                    || spent.getFluidAmount() + CrackingRecipes.SPENT_MB > spent.getCapacity()) {
                return;
            }
            oil.drain(CrackingRecipes.INPUT_MB, IFluidHandler.FluidAction.EXECUTE);
            steam.drain(CrackingRecipes.STEAM_MB, IFluidHandler.FluidAction.EXECUTE);
            left.fill(new FluidStack(recipe.left(), recipe.leftMb()), IFluidHandler.FluidAction.EXECUTE);
            if (recipe.right() != null && recipe.rightMb() > 0) {
                right.fill(new FluidStack(recipe.right(), recipe.rightMb()), IFluidHandler.FluidAction.EXECUTE);
            }
            spent.fill(new FluidStack(SteamCycle.spentSteam(), CrackingRecipes.SPENT_MB),
                    IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-3, 0, -3), worldPosition.offset(4, 16, 4));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ResourceLocation key = ForgeRegistries.FLUIDS.getKey(getOilType());
        if (key != null) {
            tag.putString("OilType", key.toString());
        }
        tag.put("Oil", oil.writeToNBT(new CompoundTag()));
        tag.put("Steam", steam.writeToNBT(new CompoundTag()));
        tag.put("Left", left.writeToNBT(new CompoundTag()));
        tag.put("Right", right.writeToNBT(new CompoundTag()));
        tag.put("Spent", spent.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("OilType")) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("OilType")));
            if (fluid != null) {
                oilType = fluid;
            }
        }
        if (tag.contains("Oil")) {
            oil.readFromNBT(tag.getCompound("Oil"));
        }
        if (tag.contains("Steam")) {
            steam.readFromNBT(tag.getCompound("Steam"));
        }
        if (tag.contains("Left")) {
            left.readFromNBT(tag.getCompound("Left"));
        }
        if (tag.contains("Right")) {
            right.readFromNBT(tag.getCompound("Right"));
        }
        if (tag.contains("Spent")) {
            spent.readFromNBT(tag.getCompound("Spent"));
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

    private FluidTank tank(int capacity) {
        return new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                onChanged();
            }
        };
    }

    private static Fluid defaultOil() {
        return ModFluids.OIL.source.get();
    }

    private final class Handler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 5;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> typedView(oil, getOilType());
                case 1 -> typedView(steam, SteamCycle.steam());
                case 2 -> left.getFluid();
                case 3 -> right.getFluid();
                case 4 -> typedView(spent, SteamCycle.spentSteam());
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return switch (tank) {
                case 0, 2, 3 -> OIL_CAP;
                case 1 -> STEAM_CAP;
                case 4 -> SPENT_CAP;
                default -> 0;
            };
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (tank == 0) {
                return stack.getFluid() == getOilType();
            }
            return tank == 1 && SteamCycle.isSteam(stack.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return 0;
            }
            if (SteamCycle.isSteam(resource.getFluid())) {
                return steam.fill(resource, action);
            }
            if (resource.getFluid() == getOilType()) {
                return oil.fill(resource, action);
            }
            return 0;
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
            if (!spent.getFluid().isEmpty() && spent.getFluid().getFluid() == resource.getFluid()) {
                return spent.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!left.getFluid().isEmpty()) {
                return left.drain(maxDrain, action);
            }
            if (!right.getFluid().isEmpty()) {
                return right.drain(maxDrain, action);
            }
            return spent.drain(maxDrain, action);
        }
    }

    private static FluidStack typedView(FluidTank tank, Fluid type) {
        if (!tank.getFluid().isEmpty()) {
            return tank.getFluid();
        }
        return new FluidStack(type, 0);
    }
}
