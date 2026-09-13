package com.hbm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

/**
 * Marker for HE cable graph nodes. Switches return false when open.
 */
public interface IEnergyConductor {
    boolean isEnergyConductor();

    long lastEnergyNetTick();

    void markEnergyNetTick(long gameTime);

    /** Flush leftover FE in this conductor into the net (cables should not hoard). */
    default int drainConductorBuffer() {
        return 0;
    }

    /** Adjacent HE along this face. Connectors and transformer pylons are directional. */
    default boolean connectsEnergy(Direction dir) {
        return true;
    }

    /** Non-adjacent nodes (pylon wires, dummyable extra cells). */
    default List<BlockPos> extraEnergyLinks() {
        return List.of();
    }
}
