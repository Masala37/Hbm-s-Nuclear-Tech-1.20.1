package com.hbm.entity.missile;

import com.hbm.explosion.ExplosionNT;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Generic HE missile — ExplosionNT ~12 radius on impact.
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
    protected void onImpact(HitResult hit) {
        if (level().isClientSide) {
            return;
        }
        new ExplosionNT(level(), this, getX(), getY(), getZ(), 12.0F)
                .overrideResolution(16)
                .addAttrib(ExplosionNT.ExAttrib.NODROP)
                .explode();
    }
}
