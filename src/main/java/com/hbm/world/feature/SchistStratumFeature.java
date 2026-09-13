package com.hbm.world.feature;

import com.hbm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 1.7 {@code SchistStratum}: gneiss lens around Y 30.
 */
public class SchistStratumFeature extends Feature<NoneFeatureConfiguration> {
    public SchistStratumFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int originX = origin.getX() & ~15;
        int originZ = origin.getZ() & ~15;
        OreLayer3DNoise noise = new OreLayer3DNoise(level.getSeed());
        BlockState gneiss = ModBlocks.STONE_GNEISS.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int lx = 0; lx < 16; lx++) {
            int x = originX + lx;
            for (int lz = 0; lz < 16; lz++) {
                int z = originZ + lz;
                double n = SchistStratumMath.unitNoise(
                        noise.sample(x * SchistStratumMath.SCALE, z * SchistStratumMath.SCALE));
                int range = SchistStratumMath.bandRange(n);
                if (range < 0) {
                    continue;
                }
                for (int y = SchistStratumMath.CENTER_Y - range; y <= SchistStratumMath.CENTER_Y + range; y++) {
                    pos.set(x, y, z);
                    if (!isReplaceable(level.getBlockState(pos))) {
                        continue;
                    }
                    level.setBlock(pos, gneiss, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    static boolean isReplaceable(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES);
    }
}
