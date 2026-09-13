package com.hbm.world.feature;

import com.hbm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 1.7 nether smoldering ore: 30 column probes, netherrack with air above, Y 16–111.
 */
public class NetherSmolderingFeature extends Feature<NoneFeatureConfiguration> {
    public NetherSmolderingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int originX = origin.getX() & ~15;
        int originZ = origin.getZ() & ~15;
        BlockState smoldering = ModBlocks.ORE_NETHER_SMOLDERING.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int k = 0; k < 30; k++) {
            int x = originX + context.random().nextInt(16);
            int z = originZ + context.random().nextInt(16);
            int d = 16 + context.random().nextInt(96);
            for (int y = d - 5; y <= d; y++) {
                pos.set(x, y, z);
                if (level.getBlockState(pos).is(Blocks.NETHERRACK) && level.getBlockState(pos.above()).isAir()) {
                    level.setBlock(pos, smoldering, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }
}
