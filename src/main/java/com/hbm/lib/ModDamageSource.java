package com.hbm.lib;

import com.hbm.registry.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

/**
 * Convenience damage sources matching legacy {@code ModDamageSource}.
 */
public final class ModDamageSource {
    private ModDamageSource() {
    }

    public static DamageSource blackhole(Level level) {
        return ModDamageTypes.source(level, ModDamageTypes.BLACKHOLE);
    }
}
