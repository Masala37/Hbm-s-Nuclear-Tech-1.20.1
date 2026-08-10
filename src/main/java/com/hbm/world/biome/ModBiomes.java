package com.hbm.world.biome;

import com.hbm.config.WorldConfig;
import com.hbm.lib.RefStrings;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

/**
 * Crater biome keys (datapack-defined) and fallout biome-overwrite selection.
 * Port of legacy {@code BiomeGenCraterBase} / {@code EntityFalloutRain.getBiomeChange}.
 */
public final class ModBiomes {
    public static final ResourceKey<Biome> CRATER = ResourceKey.create(
            Registries.BIOME, new ResourceLocation(RefStrings.MODID, "crater"));
    public static final ResourceKey<Biome> CRATER_INNER = ResourceKey.create(
            Registries.BIOME, new ResourceLocation(RefStrings.MODID, "crater_inner"));
    public static final ResourceKey<Biome> CRATER_OUTER = ResourceKey.create(
            Registries.BIOME, new ResourceLocation(RefStrings.MODID, "crater_outer"));

    private ModBiomes() {
    }

    /**
     * @return crater biome to apply at this distance/scale, or {@code null} for no change
     */
    @Nullable
    public static ResourceKey<Biome> getBiomeChange(double dist, int scale, Holder<Biome> original) {
        return getBiomeChange(dist, scale, original.unwrapKey().orElse(null));
    }

    /**
     * Legacy {@code EntityFalloutRain.getBiomeChange} logic using resource keys.
     */
    @Nullable
    public static ResourceKey<Biome> getBiomeChange(double dist, int scale, @Nullable ResourceKey<Biome> original) {
        if (!WorldConfig.enableCraterBiomes.get()) {
            return null;
        }
        if (scale >= 150 && dist < 15) {
            return CRATER_INNER;
        }
        if (scale >= 100 && dist < 55 && !CRATER_INNER.equals(original)) {
            return CRATER;
        }
        if (scale >= 25 && !CRATER_INNER.equals(original) && !CRATER.equals(original)) {
            return CRATER_OUTER;
        }
        return null;
    }
}
