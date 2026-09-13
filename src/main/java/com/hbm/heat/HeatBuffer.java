package com.hbm.heat;

import api.hbm.tile.IHeatSource;

/**
 * Simple heat buffer for machines that both store and expose {@link IHeatSource}.
 */
public final class HeatBuffer implements IHeatSource {
    private int heat;
    private final int maxHeat;

    public HeatBuffer(int maxHeat) {
        this.maxHeat = maxHeat;
    }

    public int getMaxHeat() {
        return maxHeat;
    }

    public void setHeat(int heat) {
        this.heat = Math.max(0, Math.min(heat, maxHeat));
    }

    public void addHeat(int amount) {
        if (amount > 0) {
            setHeat(heat + amount);
        }
    }

    @Override
    public int getHeatStored() {
        return heat;
    }

    @Override
    public void useUpHeat(int amount) {
        if (amount <= 0) {
            return;
        }
        heat = Math.max(0, heat - amount);
    }
}
