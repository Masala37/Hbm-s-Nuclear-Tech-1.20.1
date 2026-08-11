package com.hbm.blockentity.machine;

import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.InfiniteEnergyStorage;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
 * Creative infinite FE block — place next to a launch pad / machine and forget about power.
 */
public class InfiniteBatteryBlockEntity extends BlockEntity {
    public static final int PUSH_PER_TICK = 100_000;

    private final InfiniteEnergyStorage energy = new InfiniteEnergyStorage();
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);

    public InfiniteBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_BATTERY_INFINITE.get(), pos, state);
    }

    public Component statusMessage() {
        return Component.literal("Infinite FE — pushing to neighbors");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfiniteBatteryBlockEntity be) {
        EnergyNetworkHelper.pushToNeighbors(level, pos, be.energy, PUSH_PER_TICK);
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
