package com.hbm.explosion;

import com.hbm.api.explosion.IExplosionRay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Batched spiral-ray nuke dig (legacy ExplosionNukeRayBatched).
 */
public class ExplosionNukeRayBatched implements IExplosionRay {
    private final Map<ChunkPos, List<FloatTriplet>> perChunk = new HashMap<>();
    private final List<ChunkPos> orderedChunks = new ArrayList<>();
    private final CoordComparator comparator = new CoordComparator();

    private final Level level;
    private final int posX;
    private final int posY;
    private final int posZ;
    private final int strength;
    private final int length;
    private final int speed;

    private final int gspNumMax;
    private int gspNum;
    private double gspX;
    private double gspY;

    private boolean isAusf3Complete;

    public ExplosionNukeRayBatched(Level level, int x, int y, int z, int strength, int speed, int length) {
        this.level = level;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.strength = strength;
        this.speed = speed;
        this.length = length;
        this.gspNumMax = (int) (2.5 * Math.PI * Math.pow(this.strength, 2));
        this.gspNum = 1;
        this.gspX = Math.PI;
        this.gspY = 0.0;
    }

    private void generateGspUp() {
        if (this.gspNum < this.gspNumMax && this.gspNumMax > 1) {
            int k = this.gspNum + 1;
            double hk = -1.0 + 2.0 * (k - 1.0) / (this.gspNumMax - 1.0);
            hk = Math.max(-1.0, Math.min(1.0, hk));
            this.gspX = Math.acos(hk);

            double denom = Math.sqrt(Math.max(1.0E-12, 1.0 - hk * hk));
            double prevLon = this.gspY;
            double lon = prevLon + 3.6 / Math.sqrt(this.gspNumMax) / denom;
            this.gspY = lon % (Math.PI * 2);
        } else {
            this.gspX = 0.0;
            this.gspY = 0.0;
        }
        this.gspNum++;
    }

    private Vec3 getSpherical2cartesian() {
        double dx = Math.sin(this.gspX) * Math.cos(this.gspY);
        double dz = Math.sin(this.gspX) * Math.sin(this.gspY);
        double dy = Math.cos(this.gspX);
        return new Vec3(dx, dy, dz);
    }

    public void collectTip(int count) {
        int amountProcessed = 0;

        while (this.gspNumMax >= this.gspNum) {
            Vec3 vec = this.getSpherical2cartesian();

            int rayLength = (int) Math.ceil(strength);
            float res = strength;

            FloatTriplet lastPos = null;
            Set<ChunkPos> chunkCoords = new HashSet<>();

            for (int i = 0; i < rayLength; i++) {
                if (i > this.length) {
                    break;
                }

                float x0 = (float) (posX + (vec.x * i));
                float y0 = (float) (posY + (vec.y * i));
                float z0 = (float) (posZ + (vec.z * i));

                int iX = (int) Math.floor(x0);
                int iY = (int) Math.floor(y0);
                int iZ = (int) Math.floor(z0);
                BlockPos blockPos = new BlockPos(iX, iY, iZ);

                double fac = 100.0D - ((double) i) / ((double) rayLength) * 100.0D;
                fac *= 0.07D;

                BlockState state = level.getBlockState(blockPos);
                if (!state.liquid()) {
                    res -= (float) Math.pow(masqueradeResistance(level, blockPos, state), 7.5D - fac);
                }

                if (res > 0 && !state.isAir()) {
                    lastPos = new FloatTriplet(x0, y0, z0);
                    chunkCoords.add(new ChunkPos(iX >> 4, iZ >> 4));
                }

                if (res <= 0 || i + 1 >= this.length || i == rayLength - 1) {
                    break;
                }
            }

            for (ChunkPos pos : chunkCoords) {
                List<FloatTriplet> triplets = perChunk.computeIfAbsent(pos, key -> new ArrayList<>());
                triplets.add(lastPos);
            }

            this.generateGspUp();

            amountProcessed++;
            if (amountProcessed >= count) {
                return;
            }
        }

        orderedChunks.addAll(perChunk.keySet());
        orderedChunks.sort(comparator);
        isAusf3Complete = true;
    }

    public static float masqueradeResistance(Level level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE)) {
            return Blocks.STONE.defaultBlockState().getExplosionResistance(level, pos, null);
        }
        if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)) {
            return Blocks.STONE.defaultBlockState().getExplosionResistance(level, pos, null) * 3.0F;
        }
        return Math.max(0.0F, state.getExplosionResistance(level, pos, null));
    }

    private class CoordComparator implements Comparator<ChunkPos> {
        @Override
        public int compare(ChunkPos o1, ChunkPos o2) {
            int chunkX = ExplosionNukeRayBatched.this.posX >> 4;
            int chunkZ = ExplosionNukeRayBatched.this.posZ >> 4;
            int diff1 = Math.abs(chunkX - o1.x) + Math.abs(chunkZ - o1.z);
            int diff2 = Math.abs(chunkX - o2.x) + Math.abs(chunkZ - o2.z);
            return diff1 - diff2;
        }
    }

    public void processChunk() {
        if (this.perChunk.isEmpty() || orderedChunks.isEmpty()) {
            return;
        }

        ChunkPos coord = orderedChunks.get(0);
        List<FloatTriplet> list = perChunk.get(coord);
        if (list == null) {
            orderedChunks.remove(0);
            return;
        }

        Set<BlockPos> toRem = new HashSet<>();
        Set<BlockPos> toRemTips = new HashSet<>();
        int chunkX = coord.x;
        int chunkZ = coord.z;

        int enter = (int) (Math.min(
                Math.abs(posX - (chunkX << 4)),
                Math.abs(posZ - (chunkZ << 4)))) - 16;
        enter = Math.max(enter, 0);

        for (FloatTriplet triplet : list) {
            if (triplet == null) {
                continue;
            }
            float x = triplet.xCoord;
            float y = triplet.yCoord;
            float z = triplet.zCoord;
            Vec3 vec = new Vec3(x - this.posX, y - this.posY, z - this.posZ);
            double length = vec.length();
            if (length < 1.0E-4D) {
                continue;
            }
            double pX = vec.x / length;
            double pY = vec.y / length;
            double pZ = vec.z / length;

            int tipX = (int) Math.floor(x);
            int tipY = (int) Math.floor(y);
            int tipZ = (int) Math.floor(z);

            boolean inChunk = false;
            for (int i = enter; i < length; i++) {
                int x0 = (int) Math.floor(posX + pX * i);
                int y0 = (int) Math.floor(posY + pY * i);
                int z0 = (int) Math.floor(posZ + pZ * i);

                if ((x0 >> 4) != chunkX || (z0 >> 4) != chunkZ) {
                    if (inChunk) {
                        break;
                    }
                    continue;
                }

                inChunk = true;
                BlockPos pos = new BlockPos(x0, y0, z0);
                if (!level.isEmptyBlock(pos)) {
                    if (x0 == tipX && y0 == tipY && z0 == tipZ) {
                        toRemTips.add(pos);
                    }
                    toRem.add(pos);
                }
            }
        }

        for (BlockPos pos : toRem) {
            if (toRemTips.contains(pos)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        perChunk.remove(coord);
        orderedChunks.remove(0);
    }

    @Override
    public boolean isComplete() {
        return isAusf3Complete && perChunk.isEmpty();
    }

    @Override
    public void cacheChunksTick(int time) {
        if (!isAusf3Complete) {
            collectTip(speed * 10);
        }
    }

    @Override
    public void destructionTick(int time) {
        if (!isAusf3Complete) {
            return;
        }
        long start = System.currentTimeMillis();
        while (!perChunk.isEmpty() && System.currentTimeMillis() < start + time) {
            processChunk();
        }
    }

    @Override
    public void cancel() {
        isAusf3Complete = true;
        perChunk.clear();
        orderedChunks.clear();
    }

    private static final class FloatTriplet {
        private final float xCoord;
        private final float yCoord;
        private final float zCoord;

        private FloatTriplet(float x, float y, float z) {
            this.xCoord = x;
            this.yCoord = y;
            this.zCoord = z;
        }
    }
}
