package com.hbm.entity.missile;

import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Generic HE missile — legacy Tier-1 {@code explodeStandard(15, 24, false)} + small FX.
 */
public class EntityMissileGeneric extends EntityMissileBaseNT {
    public EntityMissileGeneric(EntityType<? extends EntityMissileGeneric> type, Level level) {
        super(type, level);
    }

    public EntityMissileGeneric(Level level) {
        this(ModEntities.MISSILE_GENERIC.get(), level);
    }

    public EntityMissileGeneric(Level level, double x, double y, double z,
                                int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_GENERIC.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected float getContrailScale() {
        return 0.5F;
    }

    @Override
    protected void onImpact(HitResult hit) {
        MissileImpacts.heTier1(level(), this, getX(), getY(), getZ());
    }
}
