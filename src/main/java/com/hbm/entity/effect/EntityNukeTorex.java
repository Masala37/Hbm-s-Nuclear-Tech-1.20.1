package com.hbm.entity.effect;

import com.hbm.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * Nuclear mushroom cloud (legacy EntityNukeTorex, simplified particle pass).
 */
public class EntityNukeTorex extends Entity {
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(EntityNukeTorex.class, EntityDataSerializers.FLOAT);

    public EntityNukeTorex(EntityType<? extends EntityNukeTorex> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    public EntityNukeTorex(Level level) {
        this(ModEntities.NUKE_TOREX.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_SCALE, 1.5F);
    }

    public EntityNukeTorex setScale(float scale) {
        entityData.set(DATA_SCALE, scale);
        return this;
    }

    public float getCloudScale() {
        return entityData.get(DATA_SCALE);
    }

    public int getMaxAge() {
        return (int) (45 * 20 * getCloudScale());
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            if (tickCount > getMaxAge()) {
                discard();
            }
            return;
        }

        spawnCloudParticles();
    }

    private void spawnCloudParticles() {
        float scale = getCloudScale();
        int age = tickCount;

        double stemHeight = 8.0D + scale * 18.0D * Math.min(1.0D, age / 40.0D);
        double capRadius = 4.0D + scale * 12.0D * Math.min(1.0D, age / 60.0D);
        double stemRadius = 1.0D + scale * 1.5D;

        // Rising stem
        int stemCount = 2 + (int) (scale * 2);
        for (int i = 0; i < stemCount; i++) {
            double h = random.nextDouble() * stemHeight;
            double ang = random.nextDouble() * Math.PI * 2.0D;
            double r = stemRadius * (0.3D + random.nextDouble() * 0.7D) * (0.4D + h / stemHeight);
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    getX() + Math.cos(ang) * r,
                    getY() + h,
                    getZ() + Math.sin(ang) * r,
                    0.0D, 0.05D + random.nextDouble() * 0.08D, 0.0D);
        }

        // Cap / torus
        if (age > 15) {
            int ringCount = 4 + (int) (scale * 4);
            for (int i = 0; i < ringCount; i++) {
                double ang = random.nextDouble() * Math.PI * 2.0D;
                double rr = capRadius * (0.6D + random.nextDouble() * 0.5D);
                double y = getY() + stemHeight + (random.nextDouble() - 0.5D) * scale * 3.0D;
                level().addParticle(ParticleTypes.LARGE_SMOKE,
                        getX() + Math.cos(ang) * rr,
                        y,
                        getZ() + Math.sin(ang) * rr,
                        Math.cos(ang) * 0.02D, 0.01D, Math.sin(ang) * 0.02D);
                if (random.nextBoolean()) {
                    level().addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                            getX() + Math.cos(ang) * rr * 0.85D,
                            y + 1.0D,
                            getZ() + Math.sin(ang) * rr * 0.85D,
                            0.0D, 0.02D, 0.0D);
                }
            }
        }

        // Early ground shock ring
        if (age < 80) {
            double shockR = age * 0.45D * scale;
            int shocks = 6 + (int) (scale * 3);
            for (int i = 0; i < shocks; i++) {
                double ang = (Math.PI * 2.0D * i) / shocks + random.nextDouble() * 0.2D;
                level().addParticle(ParticleTypes.CLOUD,
                        getX() + Math.cos(ang) * shockR,
                        getY() + 0.5D + random.nextDouble(),
                        getZ() + Math.sin(ang) * shockR,
                        Math.cos(ang) * 0.15D, 0.02D, Math.sin(ang) * 0.15D);
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double range = 256.0D * getCloudScale();
        return distance < range * range;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Ephemeral visual — discard on load like legacy.
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    /** Squirt helper from legacy BobMathUtil. */
    public static double squirt(double x) {
        return Math.sqrt(x + 1.0D / Math.pow(x + 2.0D, 2)) - 1.0D / (x + 2.0D);
    }

    public static float scaleFromRadius(float radius) {
        return Mth.clamp((float) (squirt(radius * 0.01D) * 1.5D), 0.5F, 5.0F);
    }

    public static void statFacStandard(Level level, double x, double y, double z, float radius) {
        if (level.isClientSide) {
            return;
        }
        EntityNukeTorex torex = new EntityNukeTorex(level).setScale(scaleFromRadius(radius));
        torex.setPos(x, y, z);
        level.addFreshEntity(torex);
    }
}
