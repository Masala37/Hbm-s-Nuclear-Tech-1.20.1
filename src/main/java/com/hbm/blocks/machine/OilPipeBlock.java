package com.hbm.blocks.machine;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 1.7.10 {@code oil_pipe} ({@code BlockNoDrop}). Pumpjacks place this as they drill.
 */
public class OilPipeBlock extends Block {
    public OilPipeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .noLootTable());
    }
}
