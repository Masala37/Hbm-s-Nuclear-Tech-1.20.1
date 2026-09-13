package com.hbm.world.feature;

import com.hbm.config.WorldConfig;
import com.hbm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 1.7 gas bubbles: one size-32 vein at Y 30–39 every nTH chunk (explosive default off).
 */
public class GasPocketFeature extends Feature<NoneFeatureConfiguration> {
    public enum Kind {
        FLAMMABLE,
        EXPLOSIVE
    }

    private static final int SIZE = 32;
    private static final int MIN_Y = 30;
    private static final int VARIANCE = 10;

    private final Kind kind;

    public GasPocketFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) {
        super(codec);
        this.kind = kind;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int chance = kind == Kind.EXPLOSIVE
                ? WorldConfig.explosivebubbleSpawn.get()
                : WorldConfig.gasbubbleSpawn.get();
        if (chance <= 0 || context.random().nextInt(chance) != 0) {
            return false;
        }
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int originX = origin.getX() & ~15;
        int originZ = origin.getZ() & ~15;
        int x = originX + random.nextInt(16);
        int y = MIN_Y + random.nextInt(VARIANCE);
        int z = originZ + random.nextInt(16);
        return placeVein(level, random, x, y, z);
    }

    private boolean placeVein(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        BlockState gas = fill().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        // Compact ellipsoid similar to WorldGenMinable's size-32 blob.
        int radius = Math.max(2, (int) Math.round(Math.pow(SIZE, 1.0D / 3.0D)));
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radius * radius + random.nextInt(2)) {
                        continue;
                    }
                    pos.set(x + dx, y + dy, z + dz);
                    if (!isReplaceable(level.getBlockState(pos))) {
                        continue;
                    }
                    level.setBlock(pos, gas, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private Block fill() {
        return kind == Kind.EXPLOSIVE ? ModBlocks.GAS_EXPLOSIVE.get() : ModBlocks.GAS_FLAMMABLE.get();
    }

    static boolean isReplaceable(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }
}
