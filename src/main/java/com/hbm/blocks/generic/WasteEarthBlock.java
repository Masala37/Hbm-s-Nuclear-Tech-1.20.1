package com.hbm.blocks.generic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Contaminated soil left by nuclear fallout (legacy WasteEarth).
 */
public class WasteEarthBlock extends Block {
    public WasteEarthBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
                .strength(0.6F)
                .sound(SoundType.GRASS));
    }
}
