package com.hbm.fluid;

import net.minecraft.resources.ResourceLocation;

/**
 * Typed fluid pipe node. Empty type ({@code null}) adopts the first neighboring fluid.
 */
public interface IFluidPipe {
    boolean isFluidPipe();

    ResourceLocation pipeFluid();

    void setPipeFluid(ResourceLocation fluid);

    long lastFluidNetTick();

    void markFluidNetTick(long gameTime);

    /** 1.7 {@code FluidNetMK2.fluidTracker} applied to counter valves on this net. */
    default void onNetFluidMoved(int millibuckets) {
    }
}
