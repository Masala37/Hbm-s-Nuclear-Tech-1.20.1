package com.hbm.entity.missile;

import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Black-hole micro missile — legacy {@code EntityMissileTier0.EntityMissileBHole}.
 * Impact: 1.5F TNT blast then a 1.5-size {@code EntityBlackHole}.
 */
public class EntityMissileBHole extends EntityMissileBaseNT {
    public EntityMissileBHole(EntityType<? extends EntityMissileBHole> type, Level level) {
        super(type, level);
    }

    public EntityMissileBHole(Level level) {
        this(ModEntities.MISSILE_BHOLE.get(), level);
    }

    public EntityMissileBHole(Level level, double x, double y, double z,
                              int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_BHOLE.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected float getContrailScale() {
        return 0.5F;
    }

    @Override
    protected void onImpact(HitResult hit) {
        MissileImpacts.blackHoleMicro(level(), this);
    }
}
