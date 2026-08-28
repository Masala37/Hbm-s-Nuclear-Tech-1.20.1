package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * EMP micro missile — legacy {@code EntityMissileTier0.EntityMissileEMP}.
 * Impact: {@code empBlast} radius 50 plus expanding {@code EntityEMPBlast} ring.
 */
public class EntityMissileEMP extends EntityMissileBaseNT {
    public EntityMissileEMP(EntityType<? extends EntityMissileEMP> type, Level level) {
        super(type, level);
    }

    public EntityMissileEMP(Level level) {
        this(ModEntities.MISSILE_EMP.get(), level);
    }

    public EntityMissileEMP(Level level, double x, double y, double z,
                            int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_EMP.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected float getContrailScale() {
        return 0.5F;
    }

    @Override
    protected int radarTier() {
        return IRadarDetectableNT.TIER0;
    }

    @Override
    protected void onImpact(HitResult hit) {
        MissileImpacts.empMicro(level(), this);
    }
}
