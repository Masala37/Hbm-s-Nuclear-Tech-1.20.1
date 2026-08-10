package com.hbm.world;

import com.hbm.HbmNuclearTechMod;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * Server-side helpers for runtime biome overwrite (nuke fallout crater biomes).
 * Avoids {@link net.minecraftforge.fml.util.ObfuscationReflectionHelper} field-name remapping —
 * that crashed on SRG runtime looking for {@code biomes}.
 */
public final class WorldUtil {
    private static Field biomesField;
    private static boolean biomesFieldResolved;
    private static boolean loggedMissingBiome;
    private static boolean loggedFieldFailure;

    private WorldUtil() {
    }

    /**
     * Sets the noise biome for the entire column at block {@code (x, z)}
     * (all Y quarts in every chunk section for that 4×4 XZ biome cell).
     */
    public static void setBiome(ServerLevel level, int x, int z, ResourceKey<Biome> biomeKey) {
        Optional<Holder.Reference<Biome>> holderOpt = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolder(biomeKey);
        if (holderOpt.isEmpty()) {
            if (!loggedMissingBiome) {
                loggedMissingBiome = true;
                HbmNuclearTechMod.LOGGER.warn("Crater biome {} is not registered; skipping biome overwrite", biomeKey.location());
            }
            return;
        }
        Holder<Biome> holder = holderOpt.get();

        LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int localQuartX = QuartPos.fromBlock(x) & 3;
        int localQuartZ = QuartPos.fromBlock(z) & 3;

        for (LevelChunkSection section : chunk.getSections()) {
            if (section == null) {
                continue;
            }
            PalettedContainer<Holder<Biome>> biomes = mutableBiomes(section);
            if (biomes == null) {
                return;
            }
            for (int localQuartY = 0; localQuartY < 4; localQuartY++) {
                biomes.set(localQuartX, localQuartY, localQuartZ, holder);
            }
        }

        chunk.setUnsaved(true);
    }

    /**
     * Resyncs chunk biome data to players tracking the chunk containing {@code (chunkBlockX, chunkBlockZ)}.
     */
    public static void syncBiomeChange(ServerLevel level, int chunkBlockX, int chunkBlockZ) {
        ChunkPos pos = new ChunkPos(SectionPos.blockToSectionCoord(chunkBlockX), SectionPos.blockToSectionCoord(chunkBlockZ));
        LevelChunk chunk = level.getChunk(pos.x, pos.z);
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.<ChunkAccess>of(chunk));
    }

    @SuppressWarnings("unchecked")
    private static PalettedContainer<Holder<Biome>> mutableBiomes(LevelChunkSection section) {
        PalettedContainerRO<Holder<Biome>> biomesRO = section.getBiomes();
        if (biomesRO instanceof PalettedContainer) {
            return (PalettedContainer<Holder<Biome>>) biomesRO;
        }

        // Rare: immutable RO wrapper — recreate and swap field by type (SRG-safe).
        Field field = resolveBiomesField();
        if (field == null) {
            return null;
        }
        PalettedContainer<Holder<Biome>> copy = biomesRO.recreate();
        try {
            field.set(section, copy);
            return copy;
        } catch (IllegalAccessException e) {
            if (!loggedFieldFailure) {
                loggedFieldFailure = true;
                HbmNuclearTechMod.LOGGER.error("Failed to replace LevelChunkSection biomes container", e);
            }
            return null;
        }
    }

    private static Field resolveBiomesField() {
        if (biomesFieldResolved) {
            return biomesField;
        }
        biomesFieldResolved = true;
        try {
            for (Field field : LevelChunkSection.class.getDeclaredFields()) {
                // states is PalettedContainer; biomes is declared PalettedContainerRO
                if (field.getType() == PalettedContainerRO.class) {
                    field.setAccessible(true);
                    biomesField = field;
                    return biomesField;
                }
            }
            // Fallback: RO-assignable but not the block-state container type
            for (Field field : LevelChunkSection.class.getDeclaredFields()) {
                Class<?> type = field.getType();
                if (PalettedContainerRO.class.isAssignableFrom(type)
                        && !PalettedContainer.class.isAssignableFrom(type)) {
                    field.setAccessible(true);
                    biomesField = field;
                    return biomesField;
                }
            }
        } catch (Exception e) {
            HbmNuclearTechMod.LOGGER.error("Could not resolve LevelChunkSection biomes field", e);
        }
        if (!loggedFieldFailure) {
            loggedFieldFailure = true;
            HbmNuclearTechMod.LOGGER.warn("LevelChunkSection biomes field unavailable; crater biome overwrite disabled for immutable sections");
        }
        return null;
    }
}
