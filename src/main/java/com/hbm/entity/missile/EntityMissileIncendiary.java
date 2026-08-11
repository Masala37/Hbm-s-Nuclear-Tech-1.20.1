package com.hbm.entity.missile;

import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Incendiary missile — Tier-1 FIRE blast; Tier-2 strong matches legacy {@code EntityMissileIncendiaryStrong}.
 */
public class EntityMissileIncendiary extends EntityMissileBaseNT {
    private static final EntityDataAccessor<Boolean> STRONG =
            SynchedEntityData.defineId(EntityMissileIncendiary.class, EntityDataSerializers.BOOLEAN);

    public EntityMissileIncendiary(EntityType<? extends EntityMissileIncendiary> type, Level level) {
        super(type, level);
    }

    public EntityMissileIncendiary(Level level, double x, double y, double z,
                                   int targetX, int targetY, int targetZ, boolean strong) {
        super(ModEntities.MISSILE_INCENDIARY.get(), level, x, y, z, targetX, targetY, targetZ);
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
    protected void onImpact(HitResult hit) {
        if (isStrong()) {
            MissileImpacts.incendiaryTier2(level(), this, getX(), getY(), getZ());
        } else {
            MissileImpacts.incendiaryTier1(level(), this, getX(), getY(), getZ());
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
