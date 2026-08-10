package com.hbm.entity.logic;

import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.registry.ModEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Airstrike bomber flyby (legacy EntityBomber types 0–4).
 */
public class EntityBomber extends Entity {
    private static final EntityDataAccessor<Byte> DATA_STYLE =
            SynchedEntityData.defineId(EntityBomber.class, EntityDataSerializers.BYTE);

    private int timer = 200;
    private int bombStart = 75;
    private int bombStop = 125;
    private int bombRate = 3;
    private int type;

    public EntityBomber(EntityType<? extends EntityBomber> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public EntityBomber(Level level) {
        this(ModEntities.BOMBER.get(), level);
    }

    public byte getStyle() {
        return entityData.get(DATA_STYLE);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_STYLE, (byte) 1);
    }

    @Override
    public void tick() {
        super.tick();
        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);
        updateRotationFromMotion();

        // Engine loop (audible while inbound)
        if (level().isClientSide && tickCount % 15 == 0) {
            boolean small = getStyle() <= 4;
            level().playLocalSound(getX(), getY(), getZ(),
                    (small ? ModSounds.BOMBER_SMALL_LOOP : ModSounds.BOMBER_LOOP).get(),
                    SoundSource.HOSTILE, 2.0F, 1.0F, false);
        }

        timer--;
        if (!level().isClientSide && timer <= 0) {
            discard();
            return;
        }

        if (!level().isClientSide && tickCount > bombStart && tickCount < bombStop && tickCount % bombRate == 0) {
            dropPayload();
        }
    }

    /** Legacy EntityPlaneBase.rotation(): yaw = atan2(mx, mz). */
    private void updateRotationFromMotion() {
        Vec3 motion = getDeltaMovement();
        float horiz = Mth.sqrt((float) (motion.x * motion.x + motion.z * motion.z));
        if (horiz > 1.0E-4F) {
            setYRot((float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI)));
        }
        setXRot((float) (Mth.atan2(motion.y, Math.max(horiz, 1.0E-4F)) * (180.0D / Math.PI)) - 90.0F);
    }

    private void dropPayload() {
        double x = getX();
        double y = getY();
        double z = getZ();
        if (type == 3) {
            ExplosionNukeGeneric.wasteNoSchrab(level(), (int) x, (int) y - 5, (int) z, 12);
            return;
        }

        level().playSound(null, x, y, z, ModSounds.BOMB_WHISTLE.get(), SoundSource.HOSTILE,
                12.0F, 0.9F + random.nextFloat() * 0.2F);

        EntityBombletZeta zeta = new EntityBombletZeta(level(),
                x + (random.nextDouble() - 0.5D),
                y - random.nextDouble(),
                z + (random.nextDouble() - 0.5D));
        int zetaType = type == 4 ? 4 : type;
        zeta.setBombletType(zetaType);

        Vec3 motion = getDeltaMovement();
        if (type == 0) {
            zeta.setDeltaMovement(
                    motion.x + random.nextGaussian() * 0.15D,
                    -0.4D,
                    motion.z + random.nextGaussian() * 0.15D);
        } else {
            zeta.setDeltaMovement(motion.x * 0.5D, -0.45D, motion.z * 0.5D);
        }
        level().addFreshEntity(zeta);
    }

    private void fac(Level level, double x, double y, double z) {
        Vec3 vector = new Vec3(level.random.nextDouble() - 0.5D, 0.0D, level.random.nextDouble() - 0.5D);
        if (vector.lengthSqr() < 1.0E-6D) {
            vector = new Vec3(1.0D, 0.0D, 0.0D);
        }
        vector = vector.normalize().scale(2.0D);
        setPos(x - vector.x * 100.0D, y + 50.0D, z - vector.z * 100.0D);
        setDeltaMovement(vector.x, 0.0D, vector.z);
        updateRotationFromMotion();
        entityData.set(DATA_STYLE, pickStyle(level));
    }

    private static byte pickStyle(Level level) {
        int i = 1;
        int rand = level.random.nextInt(7);
        switch (rand) {
            case 0, 1 -> i = 1;
            case 2, 3 -> i = 2;
            case 4 -> i = 5;
            case 5 -> i = 6;
            case 6 -> i = 7;
            default -> {
            }
        }
        if (level.random.nextInt(100) == 0) {
            rand = level.random.nextInt(4);
            switch (rand) {
                case 0 -> i = 0;
                case 1 -> i = 3;
                case 2 -> i = 4;
                case 3 -> i = 8;
                default -> {
                }
            }
        }
        return (byte) i;
    }

    public static EntityBomber statFacCarpet(Level level, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(level);
        bomber.timer = 200;
        bomber.bombStart = 50;
        bomber.bombStop = 100;
        bomber.bombRate = 2;
        bomber.type = 0;
        bomber.fac(level, x, y, z);
        return bomber;
    }

    public static EntityBomber statFacNapalm(Level level, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(level);
        bomber.timer = 200;
        bomber.bombStart = 50;
        bomber.bombStop = 100;
        bomber.bombRate = 5;
        bomber.type = 1;
        bomber.fac(level, x, y, z);
        return bomber;
    }

    public static EntityBomber statFacChlorine(Level level, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(level);
        bomber.timer = 200;
        bomber.bombStart = 50;
        bomber.bombStop = 100;
        bomber.bombRate = 4;
        bomber.type = 2;
        bomber.fac(level, x, y, z);
        return bomber;
    }

    public static EntityBomber statFacOrange(Level level, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(level);
        bomber.timer = 200;
        bomber.bombStart = 75;
        bomber.bombStop = 125;
        bomber.bombRate = 1;
        bomber.type = 3;
        bomber.fac(level, x, y, z);
        return bomber;
    }

    public static EntityBomber statFacABomb(Level level, double x, double y, double z) {
        EntityBomber bomber = new EntityBomber(level);
        bomber.timer = 200;
        bomber.bombStart = 60;
        bomber.bombStop = 70;
        bomber.bombRate = 65;
        bomber.type = 4;
        bomber.fac(level, x, y, z);
        // Prefer B29-style visuals for A-bomb
        bomber.entityData.set(DATA_STYLE, (byte) (5 + level.random.nextInt(3)));
        return bomber;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        timer = tag.getInt("timer");
        bombStart = tag.getInt("bombStart");
        bombStop = tag.getInt("bombStop");
        bombRate = tag.getInt("bombRate");
        type = tag.getInt("type");
        entityData.set(DATA_STYLE, tag.getByte("style"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("timer", timer);
        tag.putInt("bombStart", bombStart);
        tag.putInt("bombStop", bombStop);
        tag.putInt("bombRate", bombRate);
        tag.putInt("type", type);
        tag.putByte("style", entityData.get(DATA_STYLE));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 512.0D * 512.0D;
    }
}
