package com.hbm.entity.missile;

import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Taint-tipped micro missile — legacy {@code EntityMissileTier0.EntityMissileTaint}.
 * Impact: 5F TNT blast + 100 random taint replacements in an 11³ cube.
 */
public class EntityMissileTaint extends EntityMissileBaseNT {
    public EntityMissileTaint(EntityType<? extends EntityMissileTaint> type, Level level) {
        super(type, level);
    }

    public EntityMissileTaint(Level level) {
        this(ModEntities.MISSILE_TAINT.get(), level);
    }

    public EntityMissileTaint(Level level, double x, double y, double z,
                              int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_TAINT.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected float getContrailScale() {
        return 0.5F;
    }

    @Override
    protected void onImpact(HitResult hit) {
        MissileImpacts.taintMicro(level(), this, hit);
    }
}
