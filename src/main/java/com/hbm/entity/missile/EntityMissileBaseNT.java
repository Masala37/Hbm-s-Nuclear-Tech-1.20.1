package com.hbm.entity.missile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Lite ballistic missile base — accelerates toward target, falls under gravity, explodes on impact.
 */
public abstract class EntityMissileBaseNT extends Entity {
    protected int startX;
    protected int startZ;
    protected int targetX;
    protected int targetY;
    protected int targetZ;
    protected double velocity;
    protected double decelY;
    protected double accelXZ;

    public EntityMissileBaseNT(EntityType<? extends EntityMissileBaseNT> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public EntityMissileBaseNT(EntityType<? extends EntityMissileBaseNT> type, Level level,
                               double x, double y, double z, int targetX, int targetY, int targetZ) {
        this(type, level);
        setPos(x, y, z);
        this.startX = Mth.floor(x);
        this.startZ = Mth.floor(z);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        setDeltaMovement(0.0D, 2.0D, 0.0D);

        double dx = targetX - startX;
        double dz = targetZ - startZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0D) {
            dist = 1.0D;
        }
        accelXZ = 1.0D / dist;
        decelY = accelXZ * 2.0D;
        velocity = 0.0D;
        setYRot((float) (Mth.atan2(targetX - x, targetZ - z) * (180.0D / Math.PI)));
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();

        if (velocity < 4.0D) {
            velocity += Mth.clamp(tickCount / 60.0D * 0.05D, 0.0D, 0.05D);
        }

        Vec3 motion = getDeltaMovement();
        if (!level().isClientSide) {
            double my = motion.y - decelY * velocity;
            double mx = motion.x;
            double mz = motion.z;

            Vec3 vector = new Vec3(targetX - startX, 0.0D, targetZ - startZ);
            if (vector.lengthSqr() > 1.0E-6D) {
                vector = vector.normalize().scale(accelXZ);
            }

            if (my > 0.0D) {
                mx += vector.x * velocity;
                mz += vector.z * velocity;
            } else {
                mx -= vector.x * velocity;
                mz -= vector.z * velocity;
            }

            setDeltaMovement(mx, my, mz);
            setYRot((float) (Mth.atan2(targetX - getX(), targetZ - getZ()) * (180.0D / Math.PI)));
            float horiz = Mth.sqrt((float) (mx * mx + mz * mz));
            setXRot((float) (Mth.atan2(my, horiz) * (180.0D / Math.PI)) - 90.0F);
        }

        motion = getDeltaMovement();
        setPos(getX() + motion.x * Math.max(velocity, 0.25D),
                getY() + motion.y * Math.max(velocity, 0.25D),
                getZ() + motion.z * Math.max(velocity, 0.25D));

        if (!level().isClientSide) {
            if (getY() < level().getMinBuildHeight() - 16 || tickCount > 1200) {
                discard();
                return;
            }
            // Impact when descending into solid ground
            if (motion.y < 0.0D && !level().getBlockState(blockPosition()).isAir()) {
                onImpact(null);
                discard();
            }
        }
    }

    /** Called server-side when the missile hits ground/target. */
    protected abstract void onImpact(HitResult hit);

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        startX = tag.getInt("startX");
        startZ = tag.getInt("startZ");
        targetX = tag.getInt("targetX");
        targetY = tag.getInt("targetY");
        targetZ = tag.getInt("targetZ");
        velocity = tag.getDouble("velocity");
        decelY = tag.getDouble("decelY");
        accelXZ = tag.getDouble("accelXZ");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("startX", startX);
        tag.putInt("startZ", startZ);
        tag.putInt("targetX", targetX);
        tag.putInt("targetY", targetY);
        tag.putInt("targetZ", targetZ);
        tag.putDouble("velocity", velocity);
        tag.putDouble("decelY", decelY);
        tag.putDouble("accelXZ", accelXZ);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 300.0D * 300.0D;
    }
}
