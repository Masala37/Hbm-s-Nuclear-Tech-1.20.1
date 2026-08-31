package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Inferno Missile G.R.N. Mk.II — legacy {@code EntityMissileTier3.EntityMissileInferno}.
 * Huge incendiary: {@code explodeStandard(50, 48, true)} + composeEffectLarge
 * + igniteAllBlocks(10) + igniteFlammableBlocks(25).
 * Four-engine contrails match {@code EntityMissileTier3.spawnContrail}.
 */
public class EntityMissileInferno extends EntityMissileBaseNT {
    private static final Vec3[] ENGINE_OFFSETS = {
            new Vec3(0.0D, 0.0D, 0.5D),
            new Vec3(-0.5D, 0.0D, 0.0D),
            new Vec3(0.0D, -0.5D, -0.5D),
            new Vec3(0.5D, -0.5D, 0.0D)
    };

    public EntityMissileInferno(EntityType<? extends EntityMissileInferno> type, Level level) {
        super(type, level);
    }

    public EntityMissileInferno(Level level) {
        this(ModEntities.MISSILE_INFERNO.get(), level);
    }

    public EntityMissileInferno(Level level, double x, double y, double z,
                                int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_INFERNO.get(), level, x, y, z, targetX, targetY, targetZ);
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
        MissileImpacts.inferno(level(), this);
    }
}
