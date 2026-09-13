package com.hbm.heat;

import api.hbm.tile.IHeatSource;

/**
 * 1.7.10 {@code TileEntityHeatBoiler.tryPullHeat}: diffuse heat from a source into a buffer.
 */
public final class HeatTransfer {
    public static final double BOILER_DIFFUSION = 0.1D;

    private HeatTransfer() {
    }

    /**
     * Pull heat from {@code source} into {@code heat} (capped at {@code maxHeat}).
     * Equal temperatures are left alone. A missing or colder source decays by
     * {@code max(heat / 1000, 1)}, matching 1.7 {@code tryPullHeat}.
     *
     * @return new heat stored
     */
    public static int pull(IHeatSource source, int heat, int maxHeat, double diffusion) {
        if (source != null) {
            int diff = source.getHeatStored() - heat;
            if (diff == 0) {
                return heat;
            }
            if (diff > 0) {
                diff = (int) Math.ceil(diff * diffusion);
                diff = Math.min(diff, maxHeat - heat);
                source.useUpHeat(diff);
                heat += diff;
                return Math.min(heat, maxHeat);
            }
        }
        return Math.max(heat - Math.max(heat / 1000, 1), 0);
    }

    public static int pull(IHeatSource source, int heat, int maxHeat) {
        return pull(source, heat, maxHeat, BOILER_DIFFUSION);
    }
}
