package com.hbm.blocks.generic;

import com.hbm.inventory.loot.StructureLoot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;

/**
 * 1.7 {@code BlockMeteoriteTreasure}: 1–3 rolls of {@code POOL_METEORITE_TREASURE}.
 */
public class MeteorTreasureBlock extends Block {
    public MeteorTreasureBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(15.0F, 360.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        int count = 1 + builder.getLevel().random.nextInt(3);
        return StructureLoot.generate("POOL_METEORITE_TREASURE", count, builder.getLevel().random);
    }
}
