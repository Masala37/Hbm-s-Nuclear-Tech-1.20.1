package com.hbm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Machine knobs used by early ports (legacy {@code MachineConfig} RTG flags).
 */
public final class MachineConfig {
    public static ForgeConfigSpec.BooleanValue scaleRTGPower;
    public static ForgeConfigSpec.BooleanValue doRTGsDecay;

    private MachineConfig() {
    }

    static void build(ForgeConfigSpec.Builder builder) {
        builder.comment("Machine flags (ported subset)").push("machines");

        scaleRTGPower = builder
                .comment("Should RTG/Betavoltaic fuel power scale down as it decays?")
                .define("scaleRTGPower", false);

        doRTGsDecay = builder
                .comment("Should RTG/Betavoltaic fuel decay at all?")
                .define("doRTGsDecay", true);

        builder.pop();
    }

    /** 1.7 {@code VersatileConfig.rtgDecay()} without enable528 (not ported). */
    public static boolean rtgDecay() {
        return doRTGsDecay == null || doRTGsDecay.get();
    }

    /** 1.7 {@code VersatileConfig.scaleRTGPower()} without enable528 (not ported). */
    public static boolean scaleRTGPower() {
        return scaleRTGPower != null && scaleRTGPower.get();
    }
}
