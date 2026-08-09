package com.hbm.blockentity.machine;

import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.MachineBatteryMenu;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
 * Basic machine battery (~legacy 1M HE buffer).
 */
public class MachineBatteryBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 1_000_000;
    public static final int MAX_RECEIVE = 5_000;
    public static final int MAX_EXTRACT = 1_667;

    private final ModEnergyStorage energy = new ModEnergyStorage(CAPACITY, MAX_RECEIVE, MAX_EXTRACT, this::onEnergyChanged);
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);

    public MachineBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_BATTERY.get(), pos, state);
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.machine_battery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new MachineBatteryMenu(id, inv, this);
    }

    private void onEnergyChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBatteryBlockEntity be) {
        EnergyNetworkHelper.pushToNeighbors(level, pos, be.energy, MAX_EXTRACT);
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
