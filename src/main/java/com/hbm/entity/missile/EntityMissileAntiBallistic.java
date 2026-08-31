package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import api.hbm.entity.IRadarDetectableNT.RadarScanParams;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.registry.ModEntities;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/**
 * Pad-launched interceptor — legacy {@code EntityMissileAntiBallistic}.
 * Homes on nearby {@link EntityMissileBaseNT} (not stealth). No designator.
 */
public class EntityMissileAntiBallistic extends Entity implements IEntityAdditionalSpawnData, IRadarDetectableNT {
    public static final double BASE_SPEED = MissileSystemRules.ABM_BASE_SPEED;

    private static final EntityDataAccessor<Float> DATA_VELOCITY =
            SynchedEntityData.defineId(EntityMissileAntiBallistic.class, EntityDataSerializers.FLOAT);

    public Entity tracking;
    public double velocity;
    protected int activationTimer;

    public EntityMissileAntiBallistic(EntityType<? extends EntityMissileAntiBallistic> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
        this.setDeltaMovement(0.0D, BASE_SPEED, 0.0D);
    }

    public EntityMissileAntiBallistic(Level level) {
        this(ModEntities.MISSILE_ANTI_BALLISTIC.get(), level);
    }

    public EntityMissileAntiBallistic(Level level, double x, double y, double z) {
        this(level);
        setPos(x, y, z);
        setDeltaMovement(0.0D, BASE_SPEED, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_VELOCITY, 0.0F);
    }

    public double flightVelocity() {
        return entityData.get(DATA_VELOCITY);
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();
        yRotO = getYRot();
        xRotO = getXRot();
        this.tickCount++;

        if (level().isClientSide) {
            double vel = flightVelocity();
            Vec3 motion = getDeltaMovement();
            setPos(getX() + motion.x * vel, getY() + motion.y * vel, getZ() + motion.z * vel);
            com.hbm.HbmNuclearTechMod.proxy.spawnAbmContrail(this);
            updateRotation();
            return;
        }

        MissileChunkTickets.keepNeighbors(this);

        if (velocity < 6.0D) {
            velocity += 0.1D;
        }
        entityData.set(DATA_VELOCITY, (float) velocity);

        if (activationTimer < MissileSystemRules.ABM_CLIMB_TICKS) {
            activationTimer++;
            setDeltaMovement(0.0D, BASE_SPEED, 0.0D);
        } else {
            if (this.tracking == null || !this.tracking.isAlive()) {
                targetMissile();
            }
            if (this.tracking != null && this.tracking.isAlive()) {
                aimAtTarget();
            }
        }

        boolean liveTarget = this.tracking != null && this.tracking.isAlive();
        if (MissileSystemRules.abmGiveUp(this.tickCount, liveTarget, getY())) {
            discard();
            return;
        }

        Vec3 motion = getDeltaMovement();
        Vec3 from = position();
        Vec3 delta = new Vec3(motion.x * velocity, motion.y * velocity, motion.z * velocity);
        Vec3 to = from.add(delta);
        if (MissileSystemRules.abmArmed(activationTimer)) {
            BlockHitResult clip = level().clip(new ClipContext(
                    from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (clip.getType() == HitResult.Type.BLOCK) {
                Vec3 hit = clip.getLocation();
                setPos(hit.x, hit.y, hit.z);
                ExplosionLarge.explode(level(), getX(), getY(), getZ(), MissileSystemRules.ABM_GROUND_BLAST, true, false, false, this);
                discard();
                return;
            }
        }
        setPos(to.x, to.y, to.z);
        this.hasImpulse = true;
        updateRotation();
    }

    private void updateRotation() {
        Vec3 motion = getDeltaMovement();
        float horiz = Mth.sqrt((float) (motion.x * motion.x + motion.z * motion.z));
        setYRot((float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI)));
        setXRot((float) (Mth.atan2(motion.y, horiz) * (180.0D / Math.PI)) - 90.0F);
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
    }

    protected void targetMissile() {
        Entity closest = null;
        double dist = MissileSystemRules.ABM_SEARCH;
        AABB box = getBoundingBox().inflate(MissileSystemRules.ABM_SEARCH);
        for (EntityMissileBaseNT e : level().getEntitiesOfClass(EntityMissileBaseNT.class, box)) {
            if (!MissileSystemRules.abmTracks(e instanceof EntityMissileStealth)) {
                continue;
            }
            double len = e.position().distanceTo(position());
            if (len < dist) {
                dist = len;
                closest = e;
            }
        }
        this.tracking = closest;
    }

    protected void aimAtTarget() {
        Vec3 delta = tracking.position().subtract(position());
        double intercept = delta.length() / (BASE_SPEED * Math.max(velocity, 0.001D));
        Vec3 predicted = new Vec3(
                tracking.getX() + (tracking.getX() - tracking.xo) * intercept,
                tracking.getY() + (tracking.getY() - tracking.yo) * intercept,
                tracking.getZ() + (tracking.getZ() - tracking.zo) * intercept);
        Vec3 motion = predicted.subtract(position());
        if (motion.lengthSqr() > 1.0E-12D) {
            motion = motion.normalize();
        }

        if (MissileSystemRules.abmProximityDetonate(delta.length(), activationTimer)) {
            ExplosionLarge.explode(level(), getX(), getY(), getZ(), MissileSystemRules.ABM_PROXIMITY_BLAST, true, false, false, this);
            discard();
            return;
        }

        setDeltaMovement(motion.x * BASE_SPEED, motion.y * BASE_SPEED, motion.z * BASE_SPEED);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        velocity = tag.getDouble("veloc");
        activationTimer = tag.getInt("activation");
        entityData.set(DATA_VELOCITY, (float) velocity);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("veloc", velocity);
        tag.putInt("activation", activationTimer);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeDouble(velocity);
        Vec3 motion = getDeltaMovement();
        buf.writeDouble(motion.x);
        buf.writeDouble(motion.y);
        buf.writeDouble(motion.z);
        buf.writeFloat(getYRot());
        buf.writeFloat(getXRot());
        buf.writeInt(activationTimer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        velocity = buf.readDouble();
        setDeltaMovement(buf.readDouble(), buf.readDouble(), buf.readDouble());
        setYRot(buf.readFloat());
        setXRot(buf.readFloat());
        yRotO = getYRot();
        xRotO = getXRot();
        activationTimer = buf.readInt();
        entityData.set(DATA_VELOCITY, (float) velocity);
        this.hasImpulse = true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public String getUnlocalizedName() {
        return "radar.target.abm";
    }

    @Override
    public int getBlipLevel() {
        return IRadarDetectableNT.TIER_AB;
    }

    @Override
    public boolean canBeSeenBy(Object radar) {
        return true;
    }

    @Override
    public boolean paramsApplicable(RadarScanParams params) {
        return params.scanMissiles;
    }

    @Override
    public boolean suppliesRedstone(RadarScanParams params) {
        return false;
    }
}
