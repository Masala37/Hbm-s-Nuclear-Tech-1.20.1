package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.CableDiodeBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ModEnergyStorage;
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
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One-way FE node: receive from every side except output, push only toward FACING.
 * 1.7 throughput is {@code 10^level} HE; level is 1–11.
 */
public class CableDiodeBlockEntity extends BlockEntity {
    public static final int CAPACITY = 32_000;
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 11;

    private int throughputLevel = MIN_LEVEL;
    private final ModEnergyStorage energy = new ModEnergyStorage(CAPACITY, transferRate(MIN_LEVEL),
            transferRate(MIN_LEVEL), this::onChanged);
    private final IEnergyStorage receiveOnly = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return energy.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energy.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energy.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };
    private final IEnergyStorage extractOnly = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return energy.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return energy.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energy.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };
    private LazyOptional<IEnergyStorage> receiveOptional = LazyOptional.of(() -> receiveOnly);
    private LazyOptional<IEnergyStorage> extractOptional = LazyOptional.of(() -> extractOnly);

    public CableDiodeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE_DIODE.get(), pos, state);
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public int getThroughputLevel() {
        return throughputLevel;
    }

    public int transferRate() {
        return transferRate(throughputLevel);
    }

    public static int transferRate(int level) {
        int clamped = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        double value = Math.pow(10, clamped);
        return (int) Math.min(Integer.MAX_VALUE, value);
    }

    public boolean raiseThroughput() {
        if (throughputLevel >= MAX_LEVEL) {
            return true;
        }
        throughputLevel++;
        applyTransfer();
        sync();
        return true;
    }

    public boolean lowerThroughput() {
        if (throughputLevel <= MIN_LEVEL) {
            return true;
        }
        throughputLevel--;
        applyTransfer();
        sync();
        return true;
    }

    private void applyTransfer() {
        energy.setMaxTransfer(transferRate());
        setChanged();
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void onChanged() {
        setChanged();
    }

    private Direction output() {
        return getBlockState().getValue(CableDiodeBlock.FACING);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CableDiodeBlockEntity be) {
        Direction out = state.getValue(CableDiodeBlock.FACING);
        int rate = be.transferRate();
        EnergyNetworkHelper.pullFromNeighborsExcept(level, pos, be.energy, rate, out);
        EnergyNetworkHelper.pushToNeighbor(level, pos, be.energy, rate, out);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.putInt("diodeLevel", throughputLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("diodeLevel")) {
            throughputLevel = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, tag.getInt("diodeLevel")));
        }
        applyTransfer();
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
    public void invalidateCaps() {
        super.invalidateCaps();
        receiveOptional.invalidate();
        extractOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        receiveOptional = LazyOptional.of(() -> receiveOnly);
        extractOptional = LazyOptional.of(() -> extractOnly);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            if (side == null) {
                return receiveOptional.cast();
            }
            if (side == output()) {
                return extractOptional.cast();
            }
            return receiveOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
