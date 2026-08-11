package com.hbm.entity.missile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/**
 * Lite ballistic missile — legacy {@code EntityMissileBaseNT} ballistics + contrail.
 * <p>
 * Rotation matches 1.7.10: yaw faces target, pitch is {@code atan2(my, horiz) - 90}
 * (0 = nose up). Renderer uses the same yaw/pitch as legacy.
 */
public abstract class EntityMissileBaseNT extends Entity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> DATA_TARGET_X =
            SynchedEntityData.defineId(EntityMissileBaseNT.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_Z =
            SynchedEntityData.defineId(EntityMissileBaseNT.class, EntityDataSerializers.INT);

    protected int startX;
    protected int startZ;
    protected int targetX;
    protected int targetY;
    protected int targetZ;
    protected double velocity;
    protected double decelY;
    protected double accelXZ;
    /** Finish open-loop ballistic arc above this height over the target, then guide in. */
    private static final double ARC_HANDOFF_HEIGHT = 20.0D;
    /** Legacy cluster warheads airburst when descending steeply. */
    protected boolean isCluster;
    private boolean playedClientTakeoff;

    public EntityMissileBaseNT(EntityType<? extends EntityMissileBaseNT> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public EntityMissileBaseNT(EntityType<? extends EntityMissileBaseNT> type, Level level,
                               double x, double y, double z, int targetX, int targetY, int targetZ) {
        this(type, level);
        setPos(x, y, z);
        // Legacy uses (int) cast toward zero, not floor — matters in negative coords.
        this.startX = (int) x;
        this.startZ = (int) z;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        entityData.set(DATA_TARGET_X, targetX);
        entityData.set(DATA_TARGET_Z, targetZ);
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
        setXRot(0.0F);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_TARGET_X, 0);
        entityData.define(DATA_TARGET_Z, 0);
    }

    public int getTargetX() {
        return entityData.get(DATA_TARGET_X);
    }

    public int getTargetZ() {
        return entityData.get(DATA_TARGET_Z);
    }

    protected float getContrailScale() {
        return 1.0F;
    }

    /** Client particle helper access. */
    public float getContrailScalePublic() {
        return getContrailScale();
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();
        yRotO = getYRot();
        xRotO = getXRot();

        // Legacy Entity.onUpdate increments age before throwable movement.
        this.tickCount++;

        // --- 1) MOVE with current motion * velocity (legacy EntityThrowableNT first) ---
        Vec3 motion = getDeltaMovement();
        Vec3 from = position();
        Vec3 delta = new Vec3(motion.x * velocity, motion.y * velocity, motion.z * velocity);
        Vec3 to = from.add(delta);
        boolean dead = false;

        if (level().isClientSide) {
            setPos(to.x, to.y, to.z);
            playClientTakeoffOnce();
            spawnContrail();
        } else {
            if (getY() < level().getMinBuildHeight() - 16 || tickCount > 1200) {
                discard();
                return;
            }

            net.minecraft.world.phys.BlockHitResult clip = level().clip(
                    new net.minecraft.world.level.ClipContext(
                            from, to,
                            net.minecraft.world.level.ClipContext.Block.COLLIDER,
                            net.minecraft.world.level.ClipContext.Fluid.NONE,
                            this));
            if (clip.getType() == HitResult.Type.BLOCK) {
                Vec3 hitPos = clip.getLocation();
                setPos(hitPos.x, hitPos.y, hitPos.z);
                onImpact(clip);
                discard();
                dead = true;
            } else {
                setPos(to.x, to.y, to.z);
            }
        }

        // --- 2) THEN ramp velocity (legacy after throwable move) ---
        if (velocity < 4.0D) {
            velocity += Mth.clamp(tickCount / 60.0D * 0.05D, 0.0D, 0.05D);
        }

        if (dead) {
            return;
        }

        // --- 3) THEN update motion for next tick (legacy missile onUpdate tail) ---
        motion = getDeltaMovement();
        double my = motion.y - decelY * velocity;
        double mx = motion.x;
        double mz = motion.z;

        Vec3 vector = new Vec3(targetX - startX, 0.0D, targetZ - startZ);
        if (vector.lengthSqr() > 1.0E-6D) {
            vector = vector.normalize().scale(accelXZ);
            if (my > 0.0D) {
                // Legacy open-loop boost toward target while climbing.
                mx += vector.x * velocity;
                mz += vector.z * velocity;
            } else if (my < 0.0D) {
                // Finish the ballistic arc open-loop until ~20 above the target,
                // then lock onto the designator for the last stretch (~10+ block buffer).
                if (getY() > targetY + ARC_HANDOFF_HEIGHT) {
                    mx -= vector.x * velocity;
                    mz -= vector.z * velocity;
                } else {
                    double sdx = (targetX + 0.5D) - getX();
                    double sdz = (targetZ + 0.5D) - getZ();
                    double sinkPerTick = Math.max(1.0E-3D, -my * Math.max(velocity, 1.0E-3D));
                    double ticksLeft = Math.max(1.0D, (getY() - targetY) / sinkPerTick);
                    if (getY() <= targetY) {
                        ticksLeft = 1.0D;
                    }
                    double vStep = Math.max(velocity, 1.0E-3D);
                    mx = sdx / ticksLeft / vStep;
                    mz = sdz / ticksLeft / vStep;
                }
            }
        }

        setDeltaMovement(mx, my, mz);
        this.hasImpulse = true;

        // Legacy: yaw toward target (stable while climbing), pitch from motion.
        setYRot((float) (Mth.atan2(targetX - getX(), targetZ - getZ()) * (180.0D / Math.PI)));
        float horiz = Mth.sqrt((float) (mx * mx + mz * mz));
        setXRot((float) (Mth.atan2(my, horiz) * (180.0D / Math.PI)) - 90.0F);

        while (getXRot() - xRotO < -180.0F) {
            xRotO -= 360.0F;
        }
        while (getXRot() - xRotO >= 180.0F) {
            xRotO += 360.0F;
        }
        while (getYRot() - yRotO < -180.0F) {
            yRotO -= 360.0F;
        }
        while (getYRot() - yRotO >= 180.0F) {
            yRotO += 360.0F;
        }

        if (!level().isClientSide) {
            // Legacy: cluster airburst check after motion update.
            if (isCluster && my < -1.5D) {
                cluster();
                discard();
            }
        }
    }

    private void playClientTakeoffOnce() {
        if (playedClientTakeoff) {
            return;
        }
        playedClientTakeoff = true;
        com.hbm.HbmNuclearTechMod.proxy.playMissileTakeoff(this);
    }

    /** Exhaust opposite travel direction — legacy {@code ParticleRocketFlame} / missileContrail. */
    protected void spawnContrail() {
        com.hbm.HbmNuclearTechMod.proxy.spawnMissileContrail(this);
    }

    protected abstract void onImpact(HitResult hit);

    /** Legacy cluster airburst entry — default reuses ground impact. */
    protected void cluster() {
        onImpact(null);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        startX = tag.getInt("startX");
        startZ = tag.getInt("startZ");
        targetX = tag.getInt("targetX");
        targetY = tag.getInt("targetY");
        targetZ = tag.getInt("targetZ");
        entityData.set(DATA_TARGET_X, targetX);
        entityData.set(DATA_TARGET_Z, targetZ);
        velocity = tag.getDouble("velocity");
        decelY = tag.getDouble("decelY");
        accelXZ = tag.getDouble("accelXZ");
        setDeltaMovement(tag.getDouble("moX"), tag.getDouble("moY"), tag.getDouble("moZ"));
        isCluster = tag.getBoolean("isCluster");
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
        Vec3 motion = getDeltaMovement();
        tag.putDouble("moX", motion.x);
        tag.putDouble("moY", motion.y);
        tag.putDouble("moZ", motion.z);
        tag.putBoolean("isCluster", isCluster);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeInt(startX);
        buf.writeInt(startZ);
        buf.writeInt(targetX);
        buf.writeInt(targetY);
        buf.writeInt(targetZ);
        buf.writeDouble(velocity);
        buf.writeDouble(decelY);
        buf.writeDouble(accelXZ);
        Vec3 motion = getDeltaMovement();
        buf.writeDouble(motion.x);
        buf.writeDouble(motion.y);
        buf.writeDouble(motion.z);
        buf.writeFloat(getYRot());
        buf.writeFloat(getXRot());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        startX = buf.readInt();
        startZ = buf.readInt();
        targetX = buf.readInt();
        targetY = buf.readInt();
        targetZ = buf.readInt();
        velocity = buf.readDouble();
        decelY = buf.readDouble();
        accelXZ = buf.readDouble();
        setDeltaMovement(buf.readDouble(), buf.readDouble(), buf.readDouble());
        setYRot(buf.readFloat());
        setXRot(buf.readFloat());
        yRotO = getYRot();
        xRotO = getXRot();
        entityData.set(DATA_TARGET_X, targetX);
        entityData.set(DATA_TARGET_Z, targetZ);
        this.hasImpulse = true;
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
