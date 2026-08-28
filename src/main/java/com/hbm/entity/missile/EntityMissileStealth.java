package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Stealth missile — unique mesh, invisible to radar ({@code canBeSeenBy} is always false).
 * Impact: {@code explodeStandard(20F, 24, false)} + composeEffectStandard.
 * Form factor is Strong (pad fuel drain) but radar/item tooltip stay Tier 1.
 */
public class EntityMissileStealth extends EntityMissileBaseNT {
    public EntityMissileStealth(EntityType<? extends EntityMissileStealth> type, Level level) {
        super(type, level);
    }

    public EntityMissileStealth(Level level) {
        this(ModEntities.MISSILE_STEALTH.get(), level);
    }

    public EntityMissileStealth(Level level, double x, double y, double z,
                                int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_STEALTH.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    public boolean canBeSeenBy(Object radar) {
        return false;
    }

    @Override
    protected int radarTier() {
        return IRadarDetectableNT.TIER1;
    }

    @Override
    protected void onImpact(HitResult hit) {
        MissileImpacts.stealth(level(), this);
    }
}
