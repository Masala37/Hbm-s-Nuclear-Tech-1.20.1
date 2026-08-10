package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.CableDetectorBlock;
import com.hbm.blocks.machine.CableSwitchBlock;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared BE for cable switch / detector: relays FE only while {@code POWERED}.
 */
public class CableSwitchBlockEntity extends BlockEntity {
    public static final int CAPACITY = 32_000;
    public static final int TRANSFER = 5_000;

    private final ModEnergyStorage energy = new ModEnergyStorage(CAPACITY, TRANSFER, TRANSFER, this::onChanged);
    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);

    public CableSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE_SWITCH.get(), pos, state);
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    private void onChanged() {
        setChanged();
    }

    public void onPowerStateChanged(boolean on) {
        if (!on) {
            energy.setEnergy(0);
        }
        setChanged();
    }

    private boolean isConducting() {
        BlockState state = getBlockState();
        BooleanProperty prop = null;
        if (state.hasProperty(CableSwitchBlock.POWERED)) {
            prop = CableSwitchBlock.POWERED;
        } else if (state.hasProperty(CableDetectorBlock.POWERED)) {
            prop = CableDetectorBlock.POWERED;
        }
        return prop != null && state.getValue(prop);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CableSwitchBlockEntity be) {
        if (!be.isConducting()) {
            return;
        }
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

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyOptional = LazyOptional.of(() -> energy);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && isConducting()) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
