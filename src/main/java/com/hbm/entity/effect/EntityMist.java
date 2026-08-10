package com.hbm.entity.effect;

import com.hbm.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

/**
 * Lingering chemical mist (legacy {@code EntityMist}, chlorine path for multi bomb).
 */
public class EntityMist extends Entity {
    private static final EntityDataAccessor<Float> DATA_WIDTH =
            SynchedEntityData.defineId(EntityMist.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT =
            SynchedEntityData.defineId(EntityMist.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(EntityMist.class, EntityDataSerializers.INT);

    public static final int TYPE_CHLORINE = 0;

    private int maxAge = 150;

    public EntityMist(EntityType<? extends EntityMist> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public EntityMist(Level level) {
        this(ModEntities.MIST.get(), level);
    }

    public EntityMist setArea(float width, float height) {
        entityData.set(DATA_WIDTH, Math.max(1.0F, width));
        entityData.set(DATA_HEIGHT, Math.max(1.0F, height));
        return this;
    }

    public EntityMist setDuration(int duration) {
        this.maxAge = Math.max(20, duration);
        return this;
    }

    public EntityMist setChlorine() {
        entityData.set(DATA_TYPE, TYPE_CHLORINE);
        return this;
    }

    public float getMistWidth() {
        return entityData.get(DATA_WIDTH);
    }

    public float getMistHeight() {
        return entityData.get(DATA_HEIGHT);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_WIDTH, 5.0F);
        entityData.define(DATA_HEIGHT, 3.0F);
        entityData.define(DATA_TYPE, TYPE_CHLORINE);
    }

    @Override
    public void tick() {
        super.tick();
        float w = getMistWidth();
        float h = getMistHeight();
        setBoundingBox(new AABB(
                getX() - w * 0.5D, getY(), getZ() - w * 0.5D,
                getX() + w * 0.5D, getY() + h, getZ() + w * 0.5D));

        if (level().isClientSide) {
            DustParticleOptions green = new DustParticleOptions(new Vector3f(0.45F, 0.85F, 0.35F), 1.0F);
            for (int i = 0; i < 3; i++) {
                double px = getX() + (random.nextDouble() - 0.5D) * w;
                double py = getY() + random.nextDouble() * h;
                double pz = getZ() + (random.nextDouble() - 0.5D) * w;
                level().addParticle(green, px, py, pz, 0.0D, 0.02D, 0.0D);
                if (random.nextInt(4) == 0) {
                    level().addParticle(ParticleTypes.CLOUD, px, py, pz, 0.0D, 0.01D, 0.0D);
                }
            }
            return;
        }

        if (tickCount >= maxAge) {
            discard();
            return;
        }

        double intensity = 1.0D - (double) tickCount / (double) maxAge;
        AABB box = getBoundingBox();
        for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, box)) {
            affect(living, intensity);
        }
    }

    private void affect(LivingEntity living, double intensity) {
        int type = entityData.get(DATA_TYPE);
        if (type == TYPE_CHLORINE) {
            int base = (int) (40 + 80 * intensity);
            living.addEffect(new MobEffectInstance(MobEffects.POISON, base, 1, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Math.max(20, base / 4), 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, base, 0, true, false));
            if (intensity > 0.4D && tickCount % 20 == 0) {
                living.hurt(level().damageSources().magic(), 1.0F);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        maxAge = tag.getInt("maxAge");
        entityData.set(DATA_WIDTH, tag.getFloat("width"));
        entityData.set(DATA_HEIGHT, tag.getFloat("height"));
        entityData.set(DATA_TYPE, tag.getInt("type"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("maxAge", maxAge);
        tag.putFloat("width", getMistWidth());
        tag.putFloat("height", getMistHeight());
        tag.putInt("type", entityData.get(DATA_TYPE));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double range = getMistWidth() + 64.0D;
        return distance < range * range;
    }
}
