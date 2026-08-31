package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityMissileDrill extends EntityMissileBaseNT {
    private static final Vec3[] ENGINE_OFFSETS = {
            new Vec3(0.0D, 0.0D, 0.5D),
            new Vec3(-0.5D, 0.0D, 0.0D),
            new Vec3(0.0D, -0.5D, -0.5D),
            new Vec3(0.5D, -0.5D, 0.0D)
    };

    public EntityMissileDrill(EntityType<? extends EntityMissileDrill> type, Level level) {
        super(type, level);
    }

    public EntityMissileDrill(Level level) {
        this(ModEntities.MISSILE_DRILL.get(), level);
    }

    public EntityMissileDrill(Level level, double x, double y, double z,
                              int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_DRILL.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected int radarTier() {
        return IRadarDetectableNT.TIER3;
    }

    @Override
    public Vec3[] contrailOffsets() {
        return ENGINE_OFFSETS;
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide) {
            return;
        }
        double x = getX();
        double y = getY();
        double z = getZ();
        for (int i = 0; i < 30; i++) {
            ExplosionNT explosion = new ExplosionNT(level(), this, x, y - i, z, 10.0F);
            explosion.addAllAttrib(ExplosionNT.ExAttrib.ERRODE);
            explosion.explode();
        }
        ExplosionLarge.spawnParticles(level(), x, y, z, 25);
        ExplosionLarge.spawnShrapnels(level(), x, y, z, 12);
        ExplosionLarge.jolt(level(), x, y, z, 10, 50, 1);
    }
}
