package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class EntityMissileTier4 extends EntityMissileBaseNT {
    /**
     * Three engines: thrust (0,0,1) after the same yaw/pitch conjugation as huge,
     * plus the center puff and {@code (-thrust.x, -thrust.z, -thrust.z)}.
     */
    private static final Vec3[] ENGINE_OFFSETS = {
            new Vec3(0.0D, 0.0D, 1.0D),
            Vec3.ZERO,
            new Vec3(0.0D, -1.0D, -1.0D)
    };

    protected EntityMissileTier4(EntityType<? extends EntityMissileTier4> type, Level level) {
        super(type, level);
    }

    protected EntityMissileTier4(EntityType<? extends EntityMissileTier4> type, Level level,
                                  double x, double y, double z, int targetX, int targetY, int targetZ) {
        super(type, level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected int radarTier() {
        return IRadarDetectableNT.TIER4;
    }

    @Override
    public Vec3[] contrailOffsets() {
        return ENGINE_OFFSETS;
    }
}
