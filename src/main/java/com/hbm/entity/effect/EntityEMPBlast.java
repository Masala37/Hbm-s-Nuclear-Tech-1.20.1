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
 * Expanding EMP ring visual (legacy {@code EntityEMPBlast}).
 */
public class EntityEMPBlast extends Entity {
    private static final EntityDataAccessor<Integer> DATA_MAX_AGE =
            SynchedEntityData.defineId(EntityEMPBlast.class, EntityDataSerializers.INT);

    private int age;
    private float scale;

    public EntityEMPBlast(EntityType<? extends EntityEMPBlast> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
        this.age = 0;
        this.scale = 0.0F;
    }

    public EntityEMPBlast(Level level, int maxAge) {
        this(ModEntities.EMP_BLAST.get(), level);
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

    public float getScale() {
        return scale;
    }

    public int getAge() {
        return age;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        scale++;
        if (age >= getMaxAge()) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getShort("age");
        scale = tag.getFloat("scale");
        if (tag.contains("maxAge")) {
            setMaxAge(tag.getInt("maxAge"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("age", (short) age);
        tag.putFloat("scale", scale);
        tag.putInt("maxAge", getMaxAge());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static void spawn(Level level, double x, double y, double z, int maxAge) {
        if (level.isClientSide) {
            return;
        }
        EntityEMPBlast wave = new EntityEMPBlast(level, maxAge);
        wave.setPos(x, y, z);
        level.addFreshEntity(wave);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0D * 256.0D;
    }
}
