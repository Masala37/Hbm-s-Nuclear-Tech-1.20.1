package com.hbm.blocks;

import net.minecraft.world.level.block.Block;

/**
 * Simple solid block with an optional beacon-base flag.
 * Beacon behavior is also declared via {@code minecraft:beacon_base_blocks} where needed.
 */
public class HazardBlock extends Block {
    private final boolean beaconBase;

    public HazardBlock(Properties properties, boolean beaconBase) {
        super(properties);
        this.beaconBase = beaconBase;
    }

    public boolean isBeaconBase() {
        return beaconBase;
    }
}
