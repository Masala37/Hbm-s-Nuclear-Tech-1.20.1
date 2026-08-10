package com.hbm.entity.effect;

import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * Rainbow euphemium Fleija cloud (legacy {@code EntityCloudFleijaRainbow}).
 */
public class EntityCloudFleijaRainbow extends Entity {
    private static final EntityDataAccessor<Integer> DATA_MAX_AGE =
            SynchedEntityData.defineId(EntityCloudFleijaRainbow.class, EntityDataSerializers.INT);

    public int age;
    public float scale;

    public EntityCloudFleijaRainbow(EntityType<? extends EntityCloudFleijaRainbow> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
        this.age = 0;
        this.scale = 0;
    }

    public EntityCloudFleijaRainbow(Level level, int maxAge) {
        this(ModEntities.CLOUD_FLEIJA_RAINBOW.get(), level);
        setMaxAge(maxAge);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_MAX_AGE, 50);
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
        scale = Math.min(1.0F, age / (float) getMaxAge());

        if (!level().isClientSide && age % 5 == 0 && level() instanceof ServerLevel server) {
            double lx = getX() + (random.nextDouble() - 0.5D) * 40.0D;
            double ly = getY() + 50.0D + random.nextDouble() * 20.0D;
            double lz = getZ() + (random.nextDouble() - 0.5D) * 40.0D;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
            if (bolt != null) {
                bolt.moveTo(lx, ly, lz);
                bolt.setVisualOnly(true);
                server.addFreshEntity(bolt);
            }
        }

        if (age >= getMaxAge()) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("age");
        setMaxAge(tag.getInt("maxAge"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("age", age);
        tag.putInt("maxAge", getMaxAge());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
