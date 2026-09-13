package com.hbm.blockentity.machine;

import com.hbm.fluid.IoFluidHandler;
import com.hbm.fluid.SteamCycle;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityCondenser}.
 */
public class CondenserBlockEntity extends BlockEntity {
    public static final int TANK_CAP = 100;

    private final FluidTank spent = new FluidTank(TANK_CAP) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return SteamCycle.isSpentSteam(stack.getFluid());
        }
    };
    private final FluidTank water = new FluidTank(TANK_CAP) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return SteamCycle.isWater(stack.getFluid());
        }
    };
    private final IFluidHandler fluids = new IoFluidHandler(spent, water, stack -> SteamCycle.isSpentSteam(stack.getFluid()));
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);

    public CondenserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_CONDENSER.get(), pos, state);
    }

    public FluidTank getSpent() {
        return spent;
    }

    public FluidTank getWater() {
        return water;
    }

    public IFluidHandler getFluidHandler() {
        return fluids;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CondenserBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        int convert = SteamCycle.condenserConvert(be.spent.getFluidAmount(), be.water.getFluidAmount(), be.water.getCapacity());
        if (convert > 0) {
            be.spent.drain(convert, IFluidHandler.FluidAction.EXECUTE);
            be.water.fill(new FluidStack(SteamCycle.hbmWater(), convert), IFluidHandler.FluidAction.EXECUTE);
        }
        be.onChanged();
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Spent", spent.writeToNBT(new CompoundTag()));
        tag.put("Water", water.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Spent")) {
            spent.readFromNBT(tag.getCompound("Spent"));
        }
        if (tag.contains("Water")) {
            water.readFromNBT(tag.getCompound("Water"));
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
}
