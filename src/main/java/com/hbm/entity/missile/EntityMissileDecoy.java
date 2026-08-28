package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Decoy V2 — legacy {@code EntityMissileTier1.EntityMissileDecoy}.
 * Radar spoof: reports as a Tier 4 / nuclear missile. Impact is a tiny 4-block blast
 * with no fire and no block damage.
 */
public class EntityMissileDecoy extends EntityMissileBaseNT {
    public EntityMissileDecoy(EntityType<? extends EntityMissileDecoy> type, Level level) {
        super(type, level);
    }

    public EntityMissileDecoy(Level level) {
        this(ModEntities.MISSILE_DECOY.get(), level);
    }

    public EntityMissileDecoy(Level level, double x, double y, double z,
                              int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_DECOY.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected float getContrailScale() {
        return 0.5F;
    }

    @Override
    protected int radarTier() {
        return IRadarDetectableNT.TIER4;
    }

    @Override
    protected void onImpact(HitResult hit) {
        MissileImpacts.decoy(level(), this);
    }
}
