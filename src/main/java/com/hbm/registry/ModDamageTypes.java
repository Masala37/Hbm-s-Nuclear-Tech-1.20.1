package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/**
 * Datapack damage types. JSON lives under {@code data/hbm/damage_type/}.
 */
public final class ModDamageTypes {
    public static final ResourceKey<DamageType> BLACKHOLE =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(RefStrings.MODID, "blackhole"));

    private ModDamageTypes() {
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }
}
