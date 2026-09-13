package com.hbm.blocks.generic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Inert stand-in for 1.7 meteor_spawner until cybercrabs exist. */
public class MeteorSpawnerBlock extends Block {
    public MeteorSpawnerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(15.0F, 100.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
    }
}
