package com.hbm.energy;

import net.minecraftforge.energy.EnergyStorage;

/**
 * Energy storage that notifies a listener when contents change.
 */
public class ModEnergyStorage extends EnergyStorage {
    private final Runnable onChanged;

    public ModEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        super(capacity, maxReceive, maxExtract);
        this.onChanged = onChanged;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0) {
            onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) {
            onChanged.run();
        }
        return extracted;
    }

    public void setEnergy(int value) {
        this.energy = Math.max(0, Math.min(capacity, value));
        onChanged.run();
    }

    /** Consume stored energy for machine work, ignoring maxExtract limits. */
    public int consume(int amount) {
        int used = Math.min(this.energy, Math.max(0, amount));
        if (used > 0) {
            this.energy -= used;
            onChanged.run();
        }
        return used;
    }

    public void write(net.minecraft.nbt.CompoundTag tag) {
        tag.putInt("Energy", this.energy);
    }

    public void read(net.minecraft.nbt.CompoundTag tag) {
        this.energy = tag.getInt("Energy");
    }
}
