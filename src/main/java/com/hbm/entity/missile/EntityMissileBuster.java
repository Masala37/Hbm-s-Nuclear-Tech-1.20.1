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
 * Bunker-buster — Tier-1/2 vertical dig shaft + debris (legacy 15×5 / 20×7.5).
 */
public class EntityMissileBuster extends EntityMissileBaseNT {
    private static final EntityDataAccessor<Boolean> STRONG =
            SynchedEntityData.defineId(EntityMissileBuster.class, EntityDataSerializers.BOOLEAN);

    public EntityMissileBuster(EntityType<? extends EntityMissileBuster> type, Level level) {
        super(type, level);
    }

    public EntityMissileBuster(Level level, double x, double y, double z,
                               int targetX, int targetY, int targetZ, boolean strong) {
        super(ModEntities.MISSILE_BUSTER.get(), level, x, y, z, targetX, targetY, targetZ);
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
            MissileImpacts.busterTier2(level(), this, getX(), getY(), getZ());
        } else {
            MissileImpacts.busterTier1(level(), this, getX(), getY(), getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setStrong(tag.getBoolean("strong"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("strong", isStrong());
    }
}
