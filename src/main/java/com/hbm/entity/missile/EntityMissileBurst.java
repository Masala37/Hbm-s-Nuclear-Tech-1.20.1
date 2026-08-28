package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Spare missile — legacy {@code EntityMissileTier3.EntityMissileBurst}.
 * Huge HE airframe: {@code explodeStandard(50, 48, false)} + composeEffectLarge.
 * Four-engine contrails match {@code EntityMissileTier3.spawnContrail}.
 */
public class EntityMissileBurst extends EntityMissileBaseNT {
    /**
     * Four engines in missile space (nose +Y) at legacy pitch 0 / yaw 0:
     * thrust (0,0,0.5) through yaw+90 / pitch / -(yaw+90), then the four permutations.
     */
    private static final Vec3[] ENGINE_OFFSETS = {
            new Vec3(0.0D, 0.0D, 0.5D),
            new Vec3(-0.5D, 0.0D, 0.0D),
            new Vec3(0.0D, -0.5D, -0.5D),
            new Vec3(0.5D, -0.5D, 0.0D)
    };

    public EntityMissileBurst(EntityType<? extends EntityMissileBurst> type, Level level) {
        super(type, level);
    }

    public EntityMissileBurst(Level level) {
        this(ModEntities.MISSILE_BURST.get(), level);
    }

    public EntityMissileBurst(Level level, double x, double y, double z,
                              int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_BURST.get(), level, x, y, z, targetX, targetY, targetZ);
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
        MissileImpacts.spare(level(), this);
    }
}
