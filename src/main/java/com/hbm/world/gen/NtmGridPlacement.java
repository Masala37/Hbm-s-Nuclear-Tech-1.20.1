package com.hbm.world.gen;

import com.hbm.config.StructureConfig;
import com.hbm.world.gen.nbt.StructureSpacing;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

/**
 * 1.7 NBT structure grid. Extends {@link RandomSpreadStructurePlacement} so vanilla
 * {@code /locate} searches it (1.20 only walks random-spread and concentric-ring placements).
 */
public class NtmGridPlacement extends RandomSpreadStructurePlacement {
    public static final Codec<NtmGridPlacement> CODEC = RecordCodecBuilder.create(instance ->
            placementCodec(instance).apply(instance, NtmGridPlacement::new));

    public NtmGridPlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod,
                            float frequency, int salt, Optional<ExclusionZone> exclusionZone) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone,
                16, 4, RandomSpreadType.LINEAR);
    }

    @Override
    public ChunkPos getPotentialStructureChunk(long seed, int regionX, int regionZ) {
        return StructureSpacing.spawnChunkForCell(seed, regionX, regionZ, liveMinChunks(), liveMaxChunks());
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        if (StructureConfig.parseFlag() == 0) {
            return false;
        }
        return StructureSpacing.isSpawnChunk(chunkX, chunkZ, state.getLevelSeed(),
                liveMinChunks(), liveMaxChunks());
    }

    @Override
    public StructurePlacementType<?> type() {
        return com.hbm.registry.ModStructures.NTM_GRID.get();
    }

    private static int liveMinChunks() {
        return StructureConfig.minChunks();
    }

    private static int liveMaxChunks() {
        return StructureConfig.maxChunks();
    }
}
