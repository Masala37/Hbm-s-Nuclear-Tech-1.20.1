package com.hbm.handler.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-dimension persisted chunk radiation map with legacy Simple spread/drain.
 */
public class ChunkRadiationSavedData extends SavedData {
    public static final String ID = "hbm_chunk_radiation";
    public static final float MAX_RAD = 100_000.0F;

    private final Map<Long, Float> values = new HashMap<>();

    public static ChunkRadiationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ChunkRadiationSavedData::load, ChunkRadiationSavedData::new, ID);
    }

    public float get(int chunkX, int chunkZ) {
        return values.getOrDefault(pack(chunkX, chunkZ), 0.0F);
    }

    public void set(int chunkX, int chunkZ, float amount) {
        long key = pack(chunkX, chunkZ);
        float clamped = Mth.clamp(amount, 0.0F, MAX_RAD);
        if (clamped < 0.05F) {
            values.remove(key);
        } else {
            values.put(key, clamped);
        }
        setDirty();
    }

    public void add(int chunkX, int chunkZ, float amount) {
        set(chunkX, chunkZ, get(chunkX, chunkZ) + amount);
    }

    public void clear() {
        if (!values.isEmpty()) {
            values.clear();
            setDirty();
        }
    }

    public Map<Long, Float> snapshot() {
        return Map.copyOf(values);
    }

    /**
     * Legacy Simple update: redistribute to 3x3 neighbors then absolute drain.
     */
    public void updateSystem() {
        if (values.isEmpty()) {
            return;
        }
        Map<Long, Float> buff = new HashMap<>(values);
        values.clear();

        for (Map.Entry<Long, Float> chunk : buff.entrySet()) {
            float value = chunk.getValue();
            if (value == 0.0F) {
                continue;
            }
            int cx = unpackX(chunk.getKey());
            int cz = unpackZ(chunk.getKey());

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int type = Math.abs(i) + Math.abs(j);
                    float percent = type == 0 ? 0.6F : type == 1 ? 0.075F : 0.025F;
                    long newKey = pack(cx + i, cz + j);
                    float existing = values.getOrDefault(newKey, 0.0F);
                    if (buff.containsKey(newKey)) {
                        float newRad = existing + value * percent;
                        newRad = Mth.clamp(newRad * 0.99F - 0.05F, 0.0F, MAX_RAD);
                        if (newRad >= 0.05F) {
                            values.put(newKey, newRad);
                        } else {
                            values.remove(newKey);
                        }
                    } else {
                        float seeded = value * percent;
                        if (seeded >= 0.05F) {
                            values.put(newKey, seeded);
                        }
                    }
                }
            }
        }
        setDirty();
    }

    /** @deprecated Prefer {@link #updateSystem()} */
    public void decayAll(float factor, float cullBelow) {
        if (values.isEmpty()) {
            return;
        }
        values.entrySet().removeIf(e -> {
            float next = e.getValue() * factor;
            if (next < cullBelow) {
                return true;
            }
            e.setValue(next);
            return false;
        });
        setDirty();
    }

    public static long pack(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xFFFFFFFFL);
    }

    public static int unpackX(long packed) {
        return (int) (packed >> 32);
    }

    public static int unpackZ(long packed) {
        return (int) packed;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, Float> e : values.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("x", unpackX(e.getKey()));
            entry.putInt("z", unpackZ(e.getKey()));
            entry.putFloat("r", e.getValue());
            list.add(entry);
        }
        tag.put("chunks", list);
        return tag;
    }

    public static ChunkRadiationSavedData load(CompoundTag tag) {
        ChunkRadiationSavedData data = new ChunkRadiationSavedData();
        ListTag list = tag.getList("chunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            data.values.put(pack(entry.getInt("x"), entry.getInt("z")), entry.getFloat("r"));
        }
        return data;
    }
}
