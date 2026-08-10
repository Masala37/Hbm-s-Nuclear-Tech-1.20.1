package com.hbm.blocks.generic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Unbreakable sellafield-tainted bedrock (legacy {@code sellafield_bedrock}).
 */
public class SellafieldBedrockBlock extends Block {
    public SellafieldBedrockBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GREEN)
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .noLootTable());
    }
}
