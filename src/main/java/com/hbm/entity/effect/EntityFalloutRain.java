package com.hbm.entity.effect;

import com.hbm.config.BombConfig;
import com.hbm.config.FalloutConfigJSON;
import com.hbm.config.FalloutConfigJSON.FalloutEntry;
import com.hbm.HbmNuclearTechMod;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModEntities;
import com.hbm.world.WorldUtil;
import com.hbm.world.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

/**
 * Spreading fallout after an MK5 dig (legacy {@code EntityFalloutRain}).
 * Uses {@link FalloutConfigJSON}, thin ash layer, crater biomes.
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
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
        int scale = getScale();
        boolean biomeModified = false;

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

                Holder<Biome> original = level.getBiome(new BlockPos(x, level.getSeaLevel(), z));
                ResourceKey<Biome> next = ModBiomes.getBiomeChange(percent, scale, original);
                if (next != null) {
                    try {
                        WorldUtil.setBiome(server, x, z, next);
                        biomeModified = true;
                    } catch (Exception e) {
                        // Never let biome overwrite kill fallout stomping
                        HbmNuclearTechMod.LOGGER.error("Failed to set crater biome at {},{}", x, z, e);
                    }
                }
            }
        }

        if (biomeModified) {
            try {
                WorldUtil.syncBiomeChange(server, chunkX << 4, chunkZ << 4);
            } catch (Exception e) {
                HbmNuclearTechMod.LOGGER.error("Failed to sync crater biomes for chunk {},{}", chunkX, chunkZ, e);
            }
        }
    }

    private void stomp(LevelChunk chunk, int x, int z, double percent) {
        Level level = level();
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        int depth = 0;

        for (int y = maxY; y >= minY && depth < 3; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = chunk.getBlockState(pos);
            Block block = state.getBlock();

            if (state.isAir() || block == ModBlocks.FALLOUT.get()) {
                continue;
            }

            if (block == ModBlocks.VOLCANO_CORE.get()) {
                level.setBlock(pos, ModBlocks.VOLCANO_RAD_CORE.get().defaultBlockState(), 3);
                continue;
            }

            BlockPos abovePos = pos.above();
            BlockState above = y < maxY ? chunk.getBlockState(abovePos) : Blocks.AIR.defaultBlockState();

            // Thin ash layer on first solid surface (legacy fallout placement).
            if (depth == 0 && block != ModBlocks.FALLOUT.get()
                    && (above.isAir() || (above.canBeReplaced() && above.getFluidState().isEmpty()))) {
                double d = percent / 100.0D;
                double chance = 0.1D - Math.pow((d - 0.7D) * 1.0D, 2);
                if (chance >= level.random.nextDouble()
                        && ModBlocks.FALLOUT.get().defaultBlockState().canSurvive(level, abovePos)) {
                    level.setBlock(abovePos, ModBlocks.FALLOUT.get().defaultBlockState(), 3);
                }
            }

            // Fire on flammable under ~65% radius.
            if (percent < 65.0D && state.isFlammable(level, pos, Direction.UP)) {
                if (level.random.nextInt(5) == 0 && above.isAir()) {
                    level.setBlock(abovePos, Blocks.FIRE.defaultBlockState(), 3);
                }
            }

            // Column undercut: unsupported stone-hardness stacks within ~65% radius.
            if (percent < 65.0D && y > minY) {
                float hardness = state.getDestroySpeed(level, pos);
                float stoneLimit = Blocks.STONE_BRICKS.defaultBlockState().getDestroySpeed(level, pos);
                if (hardness >= 0.0F && hardness <= stoneLimit
                        && chunk.getBlockState(pos.below()).isAir()) {
                    for (int i = 0; i <= depth; i++) {
                        BlockPos fallPos = pos.above(i);
                        if (fallPos.getY() > maxY) {
                            break;
                        }
                        BlockState fallState = chunk.getBlockState(fallPos);
                        float fallHard = fallState.getDestroySpeed(level, fallPos);
                        if (fallState.isAir() || fallHard < 0.0F || fallHard > stoneLimit) {
                            continue;
                        }
                        net.minecraft.world.entity.item.FallingBlockEntity falling =
                                net.minecraft.world.entity.item.FallingBlockEntity.fall(level, fallPos, fallState);
                        falling.dropItem = false;
                        falling.setHurtsEntities(2.0F, 40);
                    }
                }
            }

            boolean eval = false;
            for (FalloutEntry entry : FalloutConfigJSON.entries) {
                if (entry.eval(level, pos, state, percent)) {
                    if (entry.isSolid()) {
                        depth++;
                    }
                    eval = true;
                    break;
                }
            }

            if (!eval && state.canOcclude()) {
                depth++;
            }
        }
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
