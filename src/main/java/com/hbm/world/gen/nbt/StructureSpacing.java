package com.hbm.world.gen.nbt;

import net.minecraft.world.level.ChunkPos;

import java.util.Random;

/**
 * 1.7 {@code NBTStructure.GenStructure.getSpawnAtCoords} grid math, without a World.
 */
public final class StructureSpacing {
    public static final long GRID_SALT = 996996996L;

    private StructureSpacing() {
    }

    public static boolean isSpawnChunk(int chunkX, int chunkZ, long seed, int minChunks, int maxChunks) {
        GridRoll roll = roll(chunkX, chunkZ, seed, minChunks, maxChunks);
        return chunkX == roll.spawnChunkX && chunkZ == roll.spawnChunkZ;
    }

    /**
     * RNG state after the 1.7 grid offset rolls. Next {@code nextInt} is {@code findSpawn}'s weight pick.
     */
    public static Random pickRandom(int chunkX, int chunkZ, long seed, int minChunks, int maxChunks) {
        return roll(chunkX, chunkZ, seed, minChunks, maxChunks).random;
    }

    /** 1.7 cell index for a chunk. */
    public static int cellIndex(int chunk, int minChunks, int maxChunks) {
        return cellIndexRaw(chunk, clamp(minChunks, maxChunks)[1]);
    }

    /**
     * Spawn chunk for a vanilla locate region ({@code chunk / spacing}) using 1.7 cell RNG.
     */
    public static ChunkPos spawnChunkForCell(long seed, int cellX, int cellZ, int minChunks, int maxChunks) {
        int[] span = clamp(minChunks, maxChunks);
        minChunks = span[0];
        maxChunks = span[1];
        Random rand = cellRandom(seed, cellX, cellZ);
        int x = cellX * maxChunks + rand.nextInt(maxChunks - minChunks);
        int z = cellZ * maxChunks + rand.nextInt(maxChunks - minChunks);
        return new ChunkPos(x, z);
    }

    private static GridRoll roll(int chunkX, int chunkZ, long seed, int minChunks, int maxChunks) {
        int[] span = clamp(minChunks, maxChunks);
        minChunks = span[0];
        maxChunks = span[1];
        int cellX = cellIndexRaw(chunkX, maxChunks);
        int cellZ = cellIndexRaw(chunkZ, maxChunks);
        Random rand = cellRandom(seed, cellX, cellZ);
        int x = cellX * maxChunks + rand.nextInt(maxChunks - minChunks);
        int z = cellZ * maxChunks + rand.nextInt(maxChunks - minChunks);
        return new GridRoll(rand, x, z);
    }

    private static int cellIndexRaw(int chunk, int maxChunks) {
        int x = chunk;
        if (x < 0) {
            x -= maxChunks - 1;
        }
        return x / maxChunks;
    }

    private static Random cellRandom(long seed, int cellX, int cellZ) {
        Random rand = new Random();
        rand.setSeed((long) cellX * 341873128712L + (long) cellZ * 132897987541L + seed + GRID_SALT);
        return rand;
    }

    private static int[] clamp(int minChunks, int maxChunks) {
        if (minChunks < 1) {
            minChunks = 4;
        }
        if (maxChunks <= minChunks) {
            minChunks = 8;
            maxChunks = 24;
        }
        return new int[]{minChunks, maxChunks};
    }

    private record GridRoll(Random random, int spawnChunkX, int spawnChunkZ) {
    }
}
