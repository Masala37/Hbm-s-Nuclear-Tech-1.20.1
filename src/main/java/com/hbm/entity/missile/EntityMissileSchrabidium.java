package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Schrabidium micro missile — legacy {@code EntityMissileTier0.EntityMissileSchrabidium}.
 * Impact: MK3 FLEIJA dig + cyan cloud at {@code BombConfig.aSchrabRadius} (default 20).
 */
public class EntityMissileSchrabidium extends EntityMissileBaseNT {
    public EntityMissileSchrabidium(EntityType<? extends EntityMissileSchrabidium> type, Level level) {
        super(type, level);
    }

    public EntityMissileSchrabidium(Level level) {
        this(ModEntities.MISSILE_SCHRABIDIUM.get(), level);
    }

    public EntityMissileSchrabidium(Level level, double x, double y, double z,
                                    int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_SCHRABIDIUM.get(), level, x, y, z, targetX, targetY, targetZ);
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
        MissileImpacts.schrabidiumMicro(level(), this);
    }
}
