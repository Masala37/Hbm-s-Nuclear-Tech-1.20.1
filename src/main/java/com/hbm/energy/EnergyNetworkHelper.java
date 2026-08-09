package com.hbm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Shared helpers for moving FE between neighboring energy handlers.
 */
public final class EnergyNetworkHelper {
    private EnergyNetworkHelper() {
    }

    public static void pushToNeighbors(Level level, BlockPos pos, IEnergyStorage source, int maxTransfer) {
        if (source.getEnergyStored() <= 0 || maxTransfer <= 0) {
            return;
        }

        for (Direction direction : Direction.values()) {
            if (source.getEnergyStored() <= 0) {
                return;
            }

            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            if (neighbor == null) {
                continue;
            }

            neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(target -> {
                if (!target.canReceive()) {
                    return;
                }
                int offer = source.extractEnergy(maxTransfer, true);
                if (offer <= 0) {
                    return;
                }
                int accepted = target.receiveEnergy(offer, false);
                if (accepted > 0) {
                    source.extractEnergy(accepted, false);
                }
            });
        }
    }

    public static void pullFromNeighbors(Level level, BlockPos pos, IEnergyStorage sink, int maxTransfer) {
        if (sink.getEnergyStored() >= sink.getMaxEnergyStored() || maxTransfer <= 0) {
            return;
        }

        for (Direction direction : Direction.values()) {
            int room = sink.getMaxEnergyStored() - sink.getEnergyStored();
            if (room <= 0) {
                return;
            }

            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            if (neighbor == null) {
                continue;
            }

            neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(source -> {
                if (!source.canExtract()) {
                    return;
                }
                int want = Math.min(maxTransfer, sink.getMaxEnergyStored() - sink.getEnergyStored());
                int available = source.extractEnergy(want, true);
                if (available <= 0) {
                    return;
                }
                int accepted = sink.receiveEnergy(available, false);
                if (accepted > 0) {
                    source.extractEnergy(accepted, false);
                }
            });
        }
    }
}
