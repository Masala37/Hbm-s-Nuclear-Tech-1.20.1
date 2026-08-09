package com.hbm.blockentity.machine;

import com.hbm.energy.EnergyNetworkHelper;
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
 * Conductive cable node: pulls FE from neighbors, then pushes onward.
 */
public class RedCableBlockEntity extends BlockEntity {
    public static final int CAPACITY = 32_000;
    public static final int TRANSFER = 5_000;

    private final ModEnergyStorage energy = new ModEnergyStorage(CAPACITY, TRANSFER, TRANSFER, this::onChanged);
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);

    public RedCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_CABLE.get(), pos, state);
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    private void onChanged() {
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RedCableBlockEntity be) {
        EnergyNetworkHelper.pullFromNeighbors(level, pos, be.energy, TRANSFER);
        EnergyNetworkHelper.pushToNeighbors(level, pos, be.energy, TRANSFER);
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

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
