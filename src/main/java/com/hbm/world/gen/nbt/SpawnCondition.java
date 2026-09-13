package com.hbm.world.gen.nbt;

import com.hbm.world.gen.MapGenFeatures;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;
import java.util.function.Predicate;

/**
 * One weighted NBT structure, MapGen piece, or a null-weight spacer for a biome filter.
 */
public final class SpawnCondition {
    public final String name;
    public JigsawPiece structure;
    public MapGenFeatures.Factory mapGen;
    public Predicate<Holder<Biome>> canSpawn;
    public int spawnWeight = 1;
    public Map<String, JigsawPool> pools;
    public String startPool;
    public int sizeLimit = 8;
    public int rangeLimit = 128;
    public int minHeight = 1;
    public int maxHeight = 128;

    public SpawnCondition(String name) {
        this.name = name;
    }

    public SpawnCondition(int weight, Predicate<Holder<Biome>> predicate) {
        this.name = null;
        this.spawnWeight = weight;
        this.canSpawn = predicate;
    }

    public boolean isValid(Holder<Biome> biome) {
        return canSpawn == null || canSpawn.test(biome);
    }

    public boolean hasGeometry() {
        return structure != null || pools != null || mapGen != null;
    }

    public JigsawPool pool(String poolName) {
        if (pools == null) {
            return null;
        }
        JigsawPool pool = pools.get(poolName);
        return pool == null ? null : pool.copy();
    }
}
