package com.hbm.world.feature;

import com.hbm.config.WorldConfig;
import com.hbm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Multi-chunk oil / tar-sand bubbles (legacy {@code MapGenBubble} via {@code HbmWorld}).
 */
public class OilBubbleFeature extends Feature<NoneFeatureConfiguration> {
    public enum Kind {
        STONE,
        SAND
    }

    private static final Direction[] HOLE_DIRS = {
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private final Kind kind;

    public OilBubbleFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) {
        super(codec);
        this.kind = kind;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int frequency = frequency();
        if (frequency <= 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        int range = OilBubbleMath.neighborRange(maxSize());

        RandomSource seedRand = RandomSource.create(level.getSeed());
        long xSeed = seedRand.nextLong();
        long zSeed = seedRand.nextLong();

        boolean any = false;
        for (int ox = chunkX - range; ox <= chunkX + range; ox++) {
            for (int oz = chunkZ - range; oz <= chunkZ + range; oz++) {
                RandomSource rand = RandomSource.create(((long) ox * xSeed) ^ ((long) oz * zSeed) ^ level.getSeed());
                if (generateFromOrigin(level, rand, ox, oz, chunkX, chunkZ, frequency)) {
                    any = true;
                }
            }
        }
        return any;
    }

    private int frequency() {
        if (kind == Kind.SAND) {
            return 200;
        }
        return WorldConfig.oilSpawn.get();
    }

    private int minY() {
        // 1.7 stone oil was Y 15–39; keep that ceiling and extend into 1.18 deepslate.
        return kind == Kind.SAND ? 56 : -49;
    }

    private int rangeY() {
        return kind == Kind.SAND ? 16 : 89;
    }

    private int minSize() {
        return kind == Kind.SAND ? 16 : 8;
    }

    private int maxSize() {
        return kind == Kind.SAND ? 48 : 16;
    }

    private boolean fuzzy() {
        return kind == Kind.SAND;
    }

    private boolean generateFromOrigin(WorldGenLevel level, RandomSource rand, int offsetX, int offsetZ,
            int chunkX, int chunkZ, int frequency) {
        BlockPos biomePos = new BlockPos(offsetX << 4, 64, offsetZ << 4);
        Holder<Biome> biomeHolder = level.getBiome(biomePos);
        Biome biome = biomeHolder.value();
        if (!canSpawn(biome)) {
            return false;
        }

        int effectFreq = frequency;
        if (kind == Kind.STONE && biome.getBaseTemperature() >= 2.0F && !biome.hasPrecipitation()) {
            effectFreq /= 3;
        }
        if (effectFreq <= 0) {
            effectFreq = 1;
        }
        if (rand.nextInt(effectFreq) != effectFreq - 1) {
            return false;
        }

        int xCoord = (chunkX - offsetX) * 16 + rand.nextInt(16);
        int zCoord = (chunkZ - offsetZ) * 16 + rand.nextInt(16);
        int yCoord = rand.nextInt(rangeY()) + minY();
        double radius = rand.nextInt(maxSize() - minSize()) + minSize();
        double radiusSqr = OilBubbleMath.radiusSqr(radius);

        int yMin = Math.max(level.getMinBuildHeight(), Mth.floor(yCoord - radius));
        int yMax = Math.min(level.getMaxBuildHeight(), Mth.ceil(yCoord + radius));

        Block fill = fillBlock();
        boolean placed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int originX = chunkX << 4;
        int originZ = chunkZ << 4;

        for (int bx = 15; bx >= 0; bx--) {
            for (int bz = 15; bz >= 0; bz--) {
                for (int by = yMin; by < yMax; by++) {
                    pos.set(originX + bx, by, originZ + bz);
                    if (!isReplaceable(level.getBlockState(pos))) {
                        continue;
                    }
                    int x = xCoord + bx;
                    int z = zCoord + bz;
                    int y = yCoord - by;
                    double fuzzy = fuzzy() ? OilBubbleMath.fuzzyOffset(radiusSqr, rand.nextDouble()) : 0.0D;
                    if (OilBubbleMath.inside(x, y, z, radiusSqr, fuzzy)) {
                        level.setBlock(pos, fill.defaultBlockState(), 2);
                        placed = true;
                    }
                }
            }
        }

        addSurfaceSpot(level, rand, chunkX, chunkZ, xCoord, zCoord);
        return placed;
    }

    private boolean canSpawn(Biome biome) {
        if (kind == Kind.SAND) {
            return !biome.hasPrecipitation() && biome.getBaseTemperature() >= 1.5F;
        }
        return true;
    }

    private Block fillBlock() {
        return kind == Kind.SAND ? ModBlocks.ORE_OIL_SAND.get() : ModBlocks.ORE_OIL.get();
    }

    private boolean isReplaceable(BlockState state) {
        if (kind == Kind.SAND) {
            return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND);
        }
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }

    private void addSurfaceSpot(WorldGenLevel level, RandomSource rand, int chunkX, int chunkZ,
            int xCoord, int zCoord) {
        int originX = chunkX << 4;
        int originZ = chunkZ << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int spotCount = 150;
        int spotWidth = 7;

        for (int i = 0; i < spotCount; i++) {
            int offX = (int) (rand.nextGaussian() * spotWidth);
            int offZ = (int) (rand.nextGaussian() * spotWidth);
            int rx = offX - xCoord;
            int rz = offZ - zCoord;
            if (rx < 0 || rx >= 16 || rz < 0 || rz >= 16) {
                continue;
            }
            int worldX = originX + rx;
            int worldZ = originZ + rz;
            int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ);
            int minY = level.getMinBuildHeight();
            for (int y = Math.min(top, level.getMaxBuildHeight() - 1); y >= minY; y--) {
                pos.set(worldX, y, worldZ);
                BlockState ground = level.getBlockState(pos);
                if (!ground.canOcclude()) {
                    continue;
                }
                int distSq = offX * offX + offZ * offZ;
                boolean inner = distSq < (spotWidth / 2) * (spotWidth / 2);
                for (int oy = 1; oy > -3; oy--) {
                    pos.set(worldX, y + oy, worldZ);
                    if (level.isOutsideBuildHeight(pos.getY())) {
                        continue;
                    }
                    BlockState at = level.getBlockState(pos);
                    if (at.is(Blocks.GRASS_BLOCK) || at.is(Blocks.DIRT)) {
                        level.setBlock(pos, (inner ? ModBlocks.DIRT_OILY : ModBlocks.DIRT_DEAD).get()
                                .defaultBlockState(), 2);
                        break;
                    }
                    if (at.is(Blocks.SAND) || at.is(ModBlocks.ORE_OIL_SAND.get())) {
                        level.setBlock(pos, ModBlocks.SAND_DIRTY.get().defaultBlockState(), 2);
                        break;
                    }
                    if (at.is(Blocks.RED_SAND)) {
                        level.setBlock(pos, ModBlocks.SAND_DIRTY_RED.get().defaultBlockState(), 2);
                        break;
                    }
                    if (at.is(Blocks.STONE)) {
                        level.setBlock(pos, ModBlocks.STONE_CRACKED.get().defaultBlockState(), 2);
                        break;
                    }
                }
                break;
            }
        }

        for (int i = 0; i < HOLE_DIRS.length; i++) {
            Direction dir = HOLE_DIRS[i];
            int x = dir.getStepX() - xCoord;
            int z = dir.getStepZ() - zCoord;
            if (x < 0 || x >= 16 || z < 0 || z >= 16) {
                continue;
            }
            int worldX = originX + x;
            int worldZ = originZ + z;
            int solids = 0;
            int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ);
            for (int y = Math.min(top, level.getMaxBuildHeight() - 1); y >= level.getMinBuildHeight(); y--) {
                pos.set(worldX, y, worldZ);
                BlockState state = level.getBlockState(pos);
                if (state.isAir()) {
                    continue;
                }
                if (!state.getFluidState().isEmpty()) {
                    break;
                }
                if (!state.canOcclude()) {
                    continue;
                }
                solids++;
                if (i > 0) {
                    level.setBlock(pos, ModBlocks.STONE_CRACKED.get().defaultBlockState(), 2);
                    if (solids >= 4) {
                        break;
                    }
                } else {
                    if (solids < 3) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                    if (solids == 3) {
                        level.setBlock(pos, ModBlocks.OIL_SPILL.get().defaultBlockState(), 2);
                    }
                    if (solids > 3 && solids < 7) {
                        level.setBlock(pos, ModBlocks.STONE_CRACKED.get().defaultBlockState(), 2);
                    }
                    if (solids >= 7) {
                        break;
                    }
                }
            }
        }
    }
}
