package com.hbm.energy;

import net.minecraftforge.energy.IEnergyStorage;

/**
 * Creative / infinite FE buffer — always full, never depletes.
 */
public final class InfiniteEnergyStorage implements IEnergyStorage {
    public static final int TRANSFER = Integer.MAX_VALUE / 4;

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return Math.max(0, maxReceive);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return Math.max(0, Math.min(TRANSFER, maxExtract));
    }

    @Override
    public int getEnergyStored() {
        return Integer.MAX_VALUE / 2;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
