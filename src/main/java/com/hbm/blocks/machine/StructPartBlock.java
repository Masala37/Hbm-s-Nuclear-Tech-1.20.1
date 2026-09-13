package com.hbm.blocks.machine;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 1.7.10 {@code struct_launcher} / {@code struct_scaffold} — frame pieces for compact
 * launcher and launch-table forming.
 */
public class StructPartBlock extends Block {
    public StructPartBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }
}
