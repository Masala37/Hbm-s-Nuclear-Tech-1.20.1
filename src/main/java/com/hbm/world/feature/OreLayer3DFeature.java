package com.hbm.world.feature;

import com.hbm.config.WorldConfig;
import com.hbm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 1.7 {@code OreLayer3D}: per-chunk 3D Perlin product, including the cacheX-for-nz quirk.
 */
public class OreLayer3DFeature extends Feature<NoneFeatureConfiguration> {
    public enum Kind {
        HEMATITE(0, 0.04D, 0.25D, 230.0D),
        BAUXITE(1, 0.03D, 0.15D, 300.0D),
        MALACHITE(2, 0.1D, 0.15D, 275.0D);

        final int id;
        final double scaleH;
        final double scaleV;
        final double threshold;

        Kind(int id, double scaleH, double scaleV, double threshold) {
            this.id = id;
            this.scaleH = scaleH;
            this.scaleV = scaleV;
            this.threshold = threshold;
        }

        boolean enabled() {
            return switch (this) {
                case HEMATITE -> WorldConfig.enableHematite.get();
                case BAUXITE -> WorldConfig.enableBauxite.get();
                case MALACHITE -> WorldConfig.enableMalachite.get();
            };
        }

        Block block() {
            return switch (this) {
                case HEMATITE -> ModBlocks.STONE_RESOURCE_HEMATITE.get();
                case BAUXITE -> ModBlocks.STONE_RESOURCE_BAUXITE.get();
                case MALACHITE -> ModBlocks.STONE_RESOURCE_MALACHITE.get();
            };
        }
    }

    private final Kind kind;

    public OreLayer3DFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) {
        super(codec);
        this.kind = kind;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!kind.enabled()) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int originX = origin.getX() & ~15;
        int originZ = origin.getZ() & ~15;
        long seed = level.getSeed();

        OreLayer3DNoise noiseX = new OreLayer3DNoise(seed + 101L + kind.id);
        OreLayer3DNoise noiseY = new OreLayer3DNoise(seed + 102L + kind.id);
        Block fill = kind.block();
        BlockState fillState = fill.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        int minY = Math.max(OreLayer3DNoise.MIN_Y_EXCLUSIVE + 1, level.getMinBuildHeight());
        int maxY = Math.min(OreLayer3DNoise.MAX_Y, level.getMaxBuildHeight() - 1);

        for (int lx = 0; lx < 16; lx++) {
            int x = originX + lx;
            for (int lz = 0; lz < 16; lz++) {
                int z = originZ + lz;
                double ny = noiseY.sample(x * kind.scaleH, z * kind.scaleH);
                for (int y = maxY; y >= minY; y--) {
                    // 1.7: nx = cacheX[oz][y], nz = cacheX[ox][y] (both noiseX; cacheZ unused).
                    double nx = noiseX.sample(y * kind.scaleV, z * kind.scaleH);
                    double nz = noiseX.sample(y * kind.scaleV, x * kind.scaleH);
                    if (!OreLayer3DNoise.inside(nx, ny, nz, kind.threshold)) {
                        continue;
                    }
                    pos.set(x, y, z);
                    if (!isReplaceable(level.getBlockState(pos))) {
                        continue;
                    }
                    level.setBlock(pos, fillState, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    static boolean isReplaceable(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }
}
