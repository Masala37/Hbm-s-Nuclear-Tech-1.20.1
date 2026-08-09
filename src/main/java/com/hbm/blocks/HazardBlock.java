package com.hbm.blocks;

import net.minecraft.world.level.block.Block;

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
