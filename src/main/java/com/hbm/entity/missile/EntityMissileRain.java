package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Bomblet Rain — legacy {@code EntityMissileTier3.EntityMissileRain}.
 * Huge cluster: airburst when descending, 25F casing blast + 100 Chaos bomblets.
 * Four-engine contrails match {@code EntityMissileTier3.spawnContrail}.
 */
public class EntityMissileRain extends EntityMissileBaseNT {
    private static final Vec3[] ENGINE_OFFSETS = {
            new Vec3(0.0D, 0.0D, 0.5D),
            new Vec3(-0.5D, 0.0D, 0.0D),
            new Vec3(0.0D, -0.5D, -0.5D),
            new Vec3(0.5D, -0.5D, 0.0D)
    };

    public EntityMissileRain(EntityType<? extends EntityMissileRain> type, Level level) {
        super(type, level);
        this.isCluster = true;
    }

    public EntityMissileRain(Level level) {
        this(ModEntities.MISSILE_RAIN.get(), level);
    }

    public EntityMissileRain(Level level, double x, double y, double z,
                              int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_RAIN.get(), level, x, y, z, targetX, targetY, targetZ);
        this.isCluster = true;
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
        MissileImpacts.rain(level(), this);
    }

    @Override
    protected void cluster() {
        onImpact(null);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.isCluster = true;
    }
}
