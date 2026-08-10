package com.hbm.entity.effect;

import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * Expanding cyan FLEIJA sphere cloud (legacy {@code EntityCloudFleija}).
 */
public class EntityCloudFleija extends Entity {
    private static final EntityDataAccessor<Integer> DATA_MAX_AGE =
            SynchedEntityData.defineId(EntityCloudFleija.class, EntityDataSerializers.INT);

    public int age;
    public float scale;

    public EntityCloudFleija(EntityType<? extends EntityCloudFleija> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
        this.age = 0;
        this.scale = 0;
    }

    public EntityCloudFleija(Level level, int maxAge) {
        this(ModEntities.CLOUD_FLEIJA.get(), level);
        setMaxAge(maxAge);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_MAX_AGE, 100);
    }

    public void setMaxAge(int maxAge) {
        entityData.set(DATA_MAX_AGE, Math.max(1, maxAge));
    }

    public int getMaxAge() {
        return entityData.get(DATA_MAX_AGE);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        scale++;
        if (!level().isClientSide && age >= getMaxAge()) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getShort("age");
        scale = tag.getShort("scale");
        setMaxAge(tag.getInt("maxAge"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("age", (short) age);
        tag.putShort("scale", (short) scale);
        tag.putInt("maxAge", getMaxAge());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 25000.0D * 25000.0D;
    }
}
