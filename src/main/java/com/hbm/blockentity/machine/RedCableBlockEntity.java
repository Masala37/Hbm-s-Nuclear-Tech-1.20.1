package com.hbm.blockentity.machine;

import com.hbm.energy.HeCableNet;
import com.hbm.energy.IEnergyConductor;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * HE cable node: lossless net with adjacent machines (1.7 PowerNetMK2). FE 1:1.
 */
public class RedCableBlockEntity extends BlockEntity implements IEnergyConductor {
    public static final int CAPACITY = 32_000;
    public static final int TRANSFER = 5_000;

    private final ModEnergyStorage energy = new ModEnergyStorage(CAPACITY, TRANSFER, TRANSFER, this::onChanged);
    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private long lastEnergyNetTick = Long.MIN_VALUE;

    public RedCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_CABLE.get(), pos, state);
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    @Override
    public boolean isEnergyConductor() {
        return true;
    }

    @Override
    public long lastEnergyNetTick() {
        return lastEnergyNetTick;
    }

    @Override
    public void markEnergyNetTick(long gameTime) {
        lastEnergyNetTick = gameTime;
    }

    @Override
    public int drainConductorBuffer() {
        int stored = energy.getEnergyStored();
        if (stored > 0) {
            energy.setEnergy(0);
        }
        return stored;
    }

    private void onChanged() {
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RedCableBlockEntity be) {
        HeCableNet.tick(level, pos);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
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
        energyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyOptional = LazyOptional.of(() -> energy);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
