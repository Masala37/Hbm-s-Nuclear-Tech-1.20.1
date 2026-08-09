package com.hbm.blocks.rbmk;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class RBMKDecoBlock extends Block {
    public RBMKDecoBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 100.0F)
                .requiresCorrectToolForDrops());
    }
}
