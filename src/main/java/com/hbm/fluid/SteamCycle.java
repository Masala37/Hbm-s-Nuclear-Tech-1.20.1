package com.hbm.fluid;

import com.hbm.registry.ModFluids;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code FT_Heatable}/{@code FT_Coolable} steam steps used by the first heat loop.
 * Numbers copied from {@code Fluids.java}; do not invent ratios.
 */
public final class SteamCycle {
    public static final int WATER_HEAT = 200;
    public static final int WATER_IN = 1;
    public static final int STEAM_OUT = 100;
    public static final double BOILER_EFF = 1.0D;

    /** 1.7 {@code OIL.addTraits(FT_Heatable).setEff(BOILER, 1).addStep(10, 1, HOTOIL, 1)}. */
    public static final int OIL_HEAT = 10;
    public static final int OIL_IN = 1;
    public static final int HOTOIL_OUT = 1;
    public static final double OIL_BOILER_EFF = 1.0D;

    public static final int STEAM_TURBINE_REQ = 100;
    public static final int STEAM_TURBINE_PROD = 1;
    public static final int STEAM_TURBINE_HEAT = 200;
    public static final double TURBINE_TRAIT_EFF = 1.0D;

    public static final int HOTSTEAM_TURBINE_REQ = 1;
    public static final int HOTSTEAM_TURBINE_PROD = 10;
    public static final int HOTSTEAM_TURBINE_HEAT = 2;

    private SteamCycle() {
    }

    public static boolean isWater(@Nullable Fluid fluid) {
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER
                || (ModFluids.WATER.source.isPresent() && fluid == ModFluids.WATER.source.get());
    }

    public static boolean isSteam(@Nullable Fluid fluid) {
        return ModFluids.STEAM.source.isPresent() && fluid == ModFluids.STEAM.source.get();
    }

    public static boolean isHotSteam(@Nullable Fluid fluid) {
        return ModFluids.HOTSTEAM.source.isPresent() && fluid == ModFluids.HOTSTEAM.source.get();
    }

    public static boolean isSpentSteam(@Nullable Fluid fluid) {
        return ModFluids.SPENTSTEAM.source.isPresent() && fluid == ModFluids.SPENTSTEAM.source.get();
    }

    public static Fluid hbmWater() {
        return ModFluids.WATER.source.get();
    }

    public static Fluid steam() {
        return ModFluids.STEAM.source.get();
    }

    public static Fluid spentSteam() {
        return ModFluids.SPENTSTEAM.source.get();
    }

    public static FluidStack canonicalizeWater(FluidStack stack) {
        if (stack.isEmpty() || !isWater(stack.getFluid()) || stack.getFluid() == hbmWater()) {
            return stack;
        }
        return new FluidStack(hbmWater(), stack.getAmount());
    }

    public static int boilerHeatReq() {
        return (int) Math.max(WATER_HEAT / BOILER_EFF, 1);
    }

    public static boolean isOil(@Nullable Fluid fluid) {
        return ModFluids.OIL.source.isPresent() && fluid == ModFluids.OIL.source.get();
    }

    public static boolean isHotOil(@Nullable Fluid fluid) {
        return ModFluids.HOTOIL.source.isPresent() && fluid == ModFluids.HOTOIL.source.get();
    }

    public static Fluid oil() {
        return ModFluids.OIL.source.get();
    }

    public static Fluid hotOil() {
        return ModFluids.HOTOIL.source.get();
    }

    public static int oilHeatReq() {
        return (int) Math.max(OIL_HEAT / OIL_BOILER_EFF, 1);
    }

    /**
     * @return operations performed
     */
    public static int heatOil(int oil, int hotOil, int hotOilCap, int heat, int heatReq) {
        if (heatReq <= 0) {
            return 0;
        }
        int inputOps = oil / OIL_IN;
        int outputOps = (hotOilCap - hotOil) / HOTOIL_OUT;
        int heatOps = heat / heatReq;
        return Math.max(0, Math.min(inputOps, Math.min(outputOps, heatOps)));
    }

    /**
     * @return operations performed
     */
    public static int boil(int water, int steam, int steamCap, int heat, int heatReq) {
        if (heatReq <= 0) {
            return 0;
        }
        int inputOps = water / WATER_IN;
        int outputOps = (steamCap - steam) / STEAM_OUT;
        int heatOps = heat / heatReq;
        return Math.max(0, Math.min(inputOps, Math.min(outputOps, heatOps)));
    }

    @Nullable
    public static CoolStep turbineStep(@Nullable Fluid input) {
        if (isSteam(input)) {
            return new CoolStep(spentSteam(), STEAM_TURBINE_REQ, STEAM_TURBINE_PROD, STEAM_TURBINE_HEAT, TURBINE_TRAIT_EFF);
        }
        if (isHotSteam(input)) {
            return new CoolStep(steam(), HOTSTEAM_TURBINE_REQ, HOTSTEAM_TURBINE_PROD, HOTSTEAM_TURBINE_HEAT, TURBINE_TRAIT_EFF);
        }
        return null;
    }

    public static int turbineOps(int inputFill, int outputFill, int outputCap, int amountReq, int amountProd, int maxPerTick) {
        if (amountReq <= 0 || amountProd <= 0) {
            return 0;
        }
        int inputOps = inputFill / amountReq;
        int outputOps = (outputCap - outputFill) / amountProd;
        int cap = maxPerTick / amountReq;
        return Math.max(0, Math.min(inputOps, Math.min(outputOps, cap)));
    }

    public static int condenserConvert(int spent, int water, int waterCap) {
        return Math.max(0, Math.min(spent, waterCap - water));
    }

    public record CoolStep(Fluid output, int amountReq, int amountProduced, int heatEnergy, double traitEff) {
        public double turbineEfficiency(double machineEff) {
            return traitEff * machineEff;
        }
    }
}
