package com.hbm.blocks.generic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Palette-only cubes used while loading 1.7 NBT; generation replaces them. */
public class StructureWandBlock extends Block {
    public StructureWandBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 10.0F)
                .sound(SoundType.METAL));
    }
}
