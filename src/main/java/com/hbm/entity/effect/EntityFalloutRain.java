package com.hbm.entity.effect;

import com.hbm.config.BombConfig;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

/**
 * Spreading fallout after an MK5 dig (legacy EntityFalloutRain, simplified).
 * Converts surface soil to waste earth and strips foliage — no biomes / sellafield yet.
 */
public class EntityFalloutRain extends Entity {
    private static final EntityDataAccessor<Integer> DATA_SCALE =
            SynchedEntityData.defineId(EntityFalloutRain.class, EntityDataSerializers.INT);

    private final List<Long> chunksToProcess = new ArrayList<>();
    private boolean gathered;
    private int tickDelay;

    public EntityFalloutRain(EntityType<? extends EntityFalloutRain> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
        this.tickDelay = 0;
    }

    public EntityFalloutRain(Level level) {
        this(ModEntities.FALLOUT_RAIN.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_SCALE, 100);
    }

    public void setScale(int scale) {
        entityData.set(DATA_SCALE, Math.max(1, scale));
    }

    public int getScale() {
        return entityData.get(DATA_SCALE);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        if (!gathered) {
            gatherChunks();
            gathered = true;
        }

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }
        tickDelay = BombConfig.fDelay.get();

        long start = System.currentTimeMillis();
        int budget = BombConfig.mk5.get();

        while (System.currentTimeMillis() < start + budget) {
            if (chunksToProcess.isEmpty()) {
                discard();
                return;
            }

            long packed = chunksToProcess.remove(chunksToProcess.size() - 1);
            processChunk(ChunkPos.getX(packed), ChunkPos.getZ(packed));
        }
    }

    private void gatherChunks() {
        int scale = getScale();
        int chunkRadius = (scale >> 4) + 2;
        int centerChunkX = BlockPos.containing(getX(), getY(), getZ()).getX() >> 4;
        int centerChunkZ = BlockPos.containing(getX(), getY(), getZ()).getZ() >> 4;

        List<Long> outer = new ArrayList<>();
        for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++) {
            for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++) {
                double dist = Math.hypot((cx << 4) + 8 - getX(), (cz << 4) + 8 - getZ());
                long packed = ChunkPos.asLong(cx, cz);
                if (dist + 12.0D <= scale) {
                    chunksToProcess.add(packed);
                } else if (dist - 12.0D <= scale) {
                    outer.add(packed);
                }
            }
        }
        chunksToProcess.addAll(0, outer);
    }

    private void processChunk(int chunkX, int chunkZ) {
        Level level = level();
        if (!level.hasChunk(chunkX, chunkZ)) {
            return;
        }
        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
        int scale = getScale();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int x = (chunkX << 4) + lx;
                int z = (chunkZ << 4) + lz;
                double distance = Math.hypot(x + 0.5D - getX(), z + 0.5D - getZ());
                if (distance > scale) {
                    continue;
                }
                double percent = distance * 100.0D / scale;
                stomp(chunk, x, z, percent);
            }
        }
    }

    private void stomp(LevelChunk chunk, int x, int z, double percent) {
        Level level = level();
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        int converted = 0;

        for (int y = maxY; y >= minY && converted < 3; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = chunk.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            // Strip plants / leaves out to ~full radius with falloff
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.getBlock() instanceof BushBlock
                    || state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)
                    || state.is(Blocks.LARGE_FERN) || state.is(Blocks.DEAD_BUSH) || state.is(Blocks.VINE)
                    || state.is(Blocks.SNOW)) {
                if (level.random.nextFloat() < chanceFor(percent, 0.95F, 0.25F)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
                continue;
            }

            if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                    || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) || state.is(Blocks.ROOTED_DIRT)
                    || state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH)) {
                if (level.random.nextFloat() < chanceFor(percent, 1.0F, 0.15F)) {
                    level.setBlock(pos, ModBlocks.WASTE_EARTH.get().defaultBlockState(), 3);
                    converted++;
                }
                break;
            }

            if (state.is(BlockTags.LOGS) && percent < 65.0D) {
                if (level.random.nextFloat() < 0.7F) {
                    // Char / strip leaves already handled; leave logs but scorched chance → coal-ish dirt look
                    level.setBlock(pos, Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 2);
                }
                converted++;
                continue;
            }

            if (!state.getFluidState().isEmpty()) {
                continue;
            }

            if (state.canOcclude()) {
                converted++;
            }
        }
    }

    private static float chanceFor(double percent, float near, float far) {
        double t = Math.min(1.0D, Math.max(0.0D, percent / 100.0D));
        return (float) (near + (far - near) * t);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setScale(tag.getInt("scale"));
        gathered = false;
        chunksToProcess.clear();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("scale", getScale());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double range = getScale() + 64.0D;
        return distance < range * range;
    }
}
