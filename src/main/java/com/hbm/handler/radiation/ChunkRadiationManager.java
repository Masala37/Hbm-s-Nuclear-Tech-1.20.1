package com.hbm.handler.radiation;

import com.hbm.config.RadiationConfig;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Per-chunk radiation facade (legacy {@code ChunkRadiationManager} + Simple handler).
 * Entity dose is applied from {@link com.hbm.handler.EntityEffectHandler}, not here.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class ChunkRadiationManager {
    public static final ChunkRadiationManager INSTANCE = new ChunkRadiationManager();

    private ChunkRadiationManager() {
    }

    public float getRadiation(Level level, int x, int y, int z) {
        return getRad(level, new BlockPos(x, y, z));
    }

    public float getRad(Level level, BlockPos blockPos) {
        if (!(level instanceof ServerLevel server) || !isChunkRadsEnabled()) {
            return 0.0F;
        }
        ChunkPos pos = new ChunkPos(blockPos);
        return ChunkRadiationSavedData.get(server).get(pos.x, pos.z);
    }

    public void setRadiation(Level level, int x, int y, int z, float amount) {
        if (!(level instanceof ServerLevel server) || !isChunkRadsEnabled()) {
            return;
        }
        ChunkPos pos = new ChunkPos(new BlockPos(x, y, z));
        ChunkRadiationSavedData.get(server).set(pos.x, pos.z, amount);
    }

    public void incrementRad(Level level, int x, int y, int z, float amount) {
        if (!(level instanceof ServerLevel server) || amount <= 0.0F || !isChunkRadsEnabled()) {
            return;
        }
        ChunkPos pos = new ChunkPos(new BlockPos(x, y, z));
        ChunkRadiationSavedData.get(server).add(pos.x, pos.z, amount);
    }

    public void decrementRad(Level level, int x, int y, int z, float amount) {
        if (amount <= 0.0F) {
            return;
        }
        setRadiation(level, x, y, z, Math.max(0.0F, getRadiation(level, x, y, z) - amount));
    }

    public void clearSystem(Level level) {
        if (level instanceof ServerLevel server) {
            ChunkRadiationSavedData.get(server).clear();
        }
    }

    private static boolean isChunkRadsEnabled() {
        return RadiationConfig.enableChunkRads == null || RadiationConfig.enableChunkRads.get();
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel server)) {
            return;
        }
        if (!isChunkRadsEnabled()) {
            return;
        }
        long time = server.getGameTime();
        if (time % 20L != 0L) {
            return;
        }
        ChunkRadiationSavedData.get(server).updateSystem();
        if (RadiationConfig.worldRadEffects != null && RadiationConfig.worldRadEffects.get() && time % 100L == 0L) {
            INSTANCE.handleWorldDestruction(server);
        }
    }

    /** Lite legacy Simple world destruction: hot chunks turn grass into waste earth. */
    private void handleWorldDestruction(ServerLevel level) {
        for (MapEntrySample sample : sampleHotChunks(level, 8)) {
            if (sample.rad < 10.0F) {
                continue;
            }
            int x = (sample.chunkX << 4) + level.random.nextInt(16);
            int z = (sample.chunkZ << 4) + level.random.nextInt(16);
            BlockPos top = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
            BlockState state = level.getBlockState(top);
            if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM)) {
                level.setBlock(top, ModBlocks.WASTE_EARTH.get().defaultBlockState(), 3);
            }
        }
    }

    private List<MapEntrySample> sampleHotChunks(ServerLevel level, int limit) {
        List<MapEntrySample> out = new ArrayList<>();
        Map<Long, Float> snap = ChunkRadiationSavedData.get(level).snapshot();
        for (Map.Entry<Long, Float> e : snap.entrySet()) {
            out.add(new MapEntrySample(
                    ChunkRadiationSavedData.unpackX(e.getKey()),
                    ChunkRadiationSavedData.unpackZ(e.getKey()),
                    e.getValue()));
            if (out.size() >= limit) {
                break;
            }
        }
        return out;
    }

    private record MapEntrySample(int chunkX, int chunkZ, float rad) {
    }
}
