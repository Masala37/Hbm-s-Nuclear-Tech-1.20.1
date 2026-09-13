package com.hbm.world.feature;

import com.hbm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 1.7 {@code DepthDeposit}: rare filled spheres near bedrock, replacing stone/netherrack
 * and bedrock. Overworld Y is shifted to the 1.18 floor (−64); nether height is unchanged.
 */
public class DepthDepositFeature extends Feature<NoneFeatureConfiguration> {
    public enum Kind {
        IRON(false, 5, 0.6D, 24, 0, 3),
        TITANIUM(false, 5, 0.6D, 32, 0, 3),
        TUNGSTEN(false, 5, 0.6D, 32, 0, 3),
        CINNEBAR(false, 5, 0.8D, 16, 0, 3),
        ZIRCONIUM(false, 5, 0.8D, 16, 0, 3),
        BORAX(false, 5, 0.8D, 16, 0, 3),
        NETHER_FLOOR(true, 7, 0.6D, 16, 0, 3),
        NETHER_CEILING(true, 7, 0.6D, 16, 125, 3);

        final boolean nether;
        final int size;
        final double fill;
        final int chance;
        final int yMin;
        final int yDev;

        Kind(boolean nether, int size, double fill, int chance, int yMin, int yDev) {
            this.nether = nether;
            this.size = size;
            this.fill = fill;
            this.chance = chance;
            this.yMin = yMin;
            this.yDev = yDev;
        }

        Block ore() {
            return switch (this) {
                case IRON -> ModBlocks.CLUSTER_DEPTH_IRON.get();
                case TITANIUM -> ModBlocks.CLUSTER_DEPTH_TITANIUM.get();
                case TUNGSTEN -> ModBlocks.CLUSTER_DEPTH_TUNGSTEN.get();
                case CINNEBAR -> ModBlocks.ORE_DEPTH_CINNEBAR.get();
                case ZIRCONIUM -> ModBlocks.ORE_DEPTH_ZIRCONIUM.get();
                case BORAX -> ModBlocks.ORE_DEPTH_BORAX.get();
                case NETHER_FLOOR, NETHER_CEILING -> ModBlocks.ORE_DEPTH_NETHER_NEODYMIUM.get();
            };
        }

        Block filler() {
            return nether ? ModBlocks.STONE_DEPTH_NETHER.get() : ModBlocks.STONE_DEPTH.get();
        }
    }

    private final Kind kind;

    public DepthDepositFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) {
        super(codec);
        this.kind = kind;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        if (random.nextInt(kind.chance) != 0) {
            return false;
        }
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int originX = origin.getX() & ~15;
        int originZ = origin.getZ() & ~15;
        int x = originX + random.nextInt(16);
        int z = originZ + random.nextInt(16);
        int yBase = kind.nether ? kind.yMin : level.getMinBuildHeight() + 1;
        int y = yBase + random.nextInt(kind.yDev);
        return generate(level, random, x, y, z);
    }

    boolean generate(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        int minY = kind.nether ? 1 : level.getMinBuildHeight() + 1;
        int maxY = kind.nether ? 126 : Math.min(126, level.getMaxBuildHeight() - 1);
        BlockState ore = kind.ore().defaultBlockState();
        BlockState filler = kind.filler().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        int size = kind.size;

        for (int i = x - size; i <= x + size; i++) {
            for (int j = y - size; j <= y + size; j++) {
                for (int k = z - size; k <= z + size; k++) {
                    if (j < minY || j > maxY) {
                        continue;
                    }
                    pos.set(i, j, k);
                    if (!isReplaceable(level.getBlockState(pos))) {
                        continue;
                    }
                    double len = Math.sqrt((double) (x - i) * (x - i) + (double) (y - j) * (y - j)
                            + (double) (z - k) * (z - k));
                    if (len + random.nextInt(2) < size * kind.fill) {
                        level.setBlock(pos, ore, 2);
                        placed = true;
                    } else if (len + random.nextInt(2) <= size) {
                        level.setBlock(pos, filler, 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    static boolean isReplaceable(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES)
                || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                || state.is(Blocks.NETHERRACK)
                || state.is(Blocks.BEDROCK);
    }
}
