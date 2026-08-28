package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Cluster missile — Tier-1/2 airburst + Chaos bomblets (legacy ×25 / ×50).
 */
public class EntityMissileCluster extends EntityMissileBaseNT {
    private static final EntityDataAccessor<Boolean> STRONG =
            SynchedEntityData.defineId(EntityMissileCluster.class, EntityDataSerializers.BOOLEAN);

    public EntityMissileCluster(EntityType<? extends EntityMissileCluster> type, Level level) {
        super(type, level);
        this.isCluster = true;
    }

    public EntityMissileCluster(Level level, double x, double y, double z,
                                int targetX, int targetY, int targetZ, boolean strong) {
        super(ModEntities.MISSILE_CLUSTER.get(), level, x, y, z, targetX, targetY, targetZ);
        this.isCluster = true;
        setStrong(strong);
    }

    public boolean isStrong() {
        return this.entityData.get(STRONG);
    }

    private void setStrong(boolean strong) {
        this.entityData.set(STRONG, strong);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STRONG, false);
    }

    @Override
    protected float getContrailScale() {
        return isStrong() ? 1.0F : 0.5F;
    }

    @Override
    protected int radarTier() {
        return isStrong() ? IRadarDetectableNT.TIER2 : IRadarDetectableNT.TIER1;
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (isStrong()) {
            MissileImpacts.clusterTier2(level(), this, getX(), getY(), getZ(), getYRot(), getXRot());
        } else {
            MissileImpacts.clusterTier1(level(), this, getX(), getY(), getZ(), getYRot(), getXRot());
        }
    }

    @Override
    protected void cluster() {
        onImpact(null);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setStrong(tag.getBoolean("strong"));
        this.isCluster = true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("strong", isStrong());
    }
}
