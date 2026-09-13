package com.hbm.fluid;

import com.hbm.registry.ModFluids;
import net.minecraft.world.level.material.Fluid;

/**
 * 1.7.10 {@code FT_Flammable} heat energy per bucket (1000 mB), copied from {@code Fluids}.
 * Wood burner FE = heatEnergy * millibuckets / 2000.
 */
public final class FlammableHeatEnergy {
    private FlammableHeatEnergy() {
    }

    /** TU (and FE 1:1) per 1000 mB. */
    public static long perBucket(Fluid fluid) {
        if (fluid == null) {
            return 0L;
        }
        if (fluid == ModFluids.WOODOIL.source.get()) {
            return 110_000L;
        }
        if (fluid == ModFluids.DIESEL.source.get() || fluid == ModFluids.LIGHTOIL.source.get()) {
            return 200_000L;
        }
        if (fluid == ModFluids.GASOLINE.source.get()) {
            return 400_000L;
        }
        if (fluid == ModFluids.KEROSENE.source.get() || fluid == ModFluids.KEROSENE_REFORM.source.get()) {
            return 300_000L;
        }
        if (fluid == ModFluids.ETHANOL.source.get()) {
            return 75_000L;
        }
        if (fluid == ModFluids.OIL.source.get() || fluid == ModFluids.CRACKOIL.source.get()) {
            return 10_000L;
        }
        if (fluid == ModFluids.HEAVYOIL.source.get()) {
            return 50_000L;
        }
        if (fluid == ModFluids.SMEAR.source.get()) {
            return 50_000L;
        }
        if (fluid == ModFluids.HEATINGOIL.source.get()) {
            return 150_000L;
        }
        if (fluid == ModFluids.NAPHTHA.source.get()) {
            return 125_000L;
        }
        if (fluid == ModFluids.GAS.source.get()) {
            return 10_000L;
        }
        if (fluid == ModFluids.HYDROGEN.source.get()) {
            return 5_000L;
        }
        if (fluid == ModFluids.BALEFIRE.source.get()) {
            return 1_000_000L;
        }
        return 0L;
    }

    /** 1.7 {@code trait.getHeatEnergy() * toBurn / 2_000L}. */
    public static int burnFe(Fluid fluid, int millibuckets) {
        if (millibuckets <= 0) {
            return 0;
        }
        long heat = perBucket(fluid);
        if (heat <= 0L) {
            return 0;
        }
        return (int) (heat * millibuckets / 2_000L);
    }
}
