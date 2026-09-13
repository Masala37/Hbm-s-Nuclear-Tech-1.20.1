package com.hbm.world.gen;

import com.hbm.world.gen.component.CivilianFeatures;
import com.hbm.world.gen.component.OfficeFeatures;
import com.hbm.world.gen.component.SiloComponent;
import com.hbm.world.gen.nbt.SpawnCondition;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraftforge.common.Tags;

import java.util.Random;

/**
 * 1.7 {@code MapGenNTMFeatures.Start} inner pick. Same {@link Random} as the NBT weight roll.
 */
public final class MapGenFeatures {
    public static final Factory SILO = (rand, chunk) ->
            new SiloComponent(rand, chunk.getMinBlockX() + 8, chunk.getMinBlockZ() + 8);
    public static final Factory HOUSE_1 = (rand, chunk) ->
            new CivilianFeatures.NTMHouse1(rand, chunk.getMinBlockX() + 8, chunk.getMinBlockZ() + 8);
    public static final Factory HOUSE_2 = (rand, chunk) ->
            new CivilianFeatures.NTMHouse2(rand, chunk.getMinBlockX() + 8, chunk.getMinBlockZ() + 8);
    public static final Factory LAB_1 = (rand, chunk) ->
            new CivilianFeatures.NTMLab1(rand, chunk.getMinBlockX() + 8, chunk.getMinBlockZ() + 8);
    public static final Factory LAB_2 = (rand, chunk) ->
            new CivilianFeatures.NTMLab2(rand, chunk.getMinBlockX() + 8, chunk.getMinBlockZ() + 8);
    public static final Factory OFFICE = (rand, chunk) ->
            new OfficeFeatures.LargeOffice(rand, chunk.getMinBlockX() + 8, chunk.getMinBlockZ() + 8);
    public static final Factory OFFICE_CORNER = (rand, chunk) ->
            new OfficeFeatures.LargeOfficeCorner(rand, chunk.getMinBlockX() + 8, chunk.getMinBlockZ() + 8);
    public static final Factory RURAL_HOUSE = (rand, chunk) ->
            new CivilianFeatures.RuralHouse1(rand, chunk.getMinBlockX() + 8, chunk.getMinBlockZ() + 8);

    private MapGenFeatures() {
    }

    @FunctionalInterface
    public interface Factory {
        StructurePiece create(Random random, ChunkPos chunk);
    }

    /**
     * Continues the 1.7 feature RNG after the weight-50 {@code features} bucket is chosen.
     */
    public static SpawnCondition pickInner(Holder<Biome> biome, Random random) {
        if (NTMStructures.flatEnoughForSilo(biome) && random.nextInt(10) == 0) {
            return NTMStructures.byName("silo");
        }
        if (hotDry(biome)) {
            return NTMStructures.byName(random.nextBoolean() ? "house_1" : "house_2");
        }
        return switch (random.nextInt(6)) {
            case 0 -> NTMStructures.byName("lab_2");
            case 1 -> NTMStructures.byName("lab_1");
            case 2 -> NTMStructures.byName("office");
            case 3 -> NTMStructures.byName("office_corner");
            default -> NTMStructures.byName("rural_house");
        };
    }

    private static boolean hotDry(Holder<Biome> biome) {
        return biome.is(Tags.Biomes.IS_HOT) && biome.is(Tags.Biomes.IS_DRY) && !biome.is(BiomeTags.IS_BADLANDS);
    }
}
