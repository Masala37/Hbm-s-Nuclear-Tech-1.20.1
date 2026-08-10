package com.hbm.entity.projectile;

import com.hbm.entity.effect.EntityMist;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.explosion.ExplosionThermo;
import com.hbm.registry.ModEntities;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Bomber submunition (legacy {@code EntityBombletZeta}) — types 0/1/2/4.
 */
public class EntityBombletZeta extends Entity {
    private static final EntityDataAccessor<Byte> DATA_TYPE =
            SynchedEntityData.defineId(EntityBombletZeta.class, EntityDataSerializers.BYTE);

    public EntityBombletZeta(EntityType<? extends EntityBombletZeta> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public EntityBombletZeta(Level level) {
        this(ModEntities.BOMBLET_ZETA.get(), level);
    }

    public EntityBombletZeta(Level level, double x, double y, double z) {
        this(level);
        setPos(x, y, z);
    }

    public EntityBombletZeta setBombletType(int type) {
        entityData.set(DATA_TYPE, (byte) type);
        return this;
    }

    public int getBombletType() {
        return entityData.get(DATA_TYPE) & 0xFF;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_TYPE, (byte) 0);
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();
        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        setDeltaMovement(motion.x * 0.99D, motion.y - 0.05D, motion.z * 0.99D);
        updateRotation();

        if (!level().isClientSide) {
            BlockPos pos = BlockPos.containing(getX(), getY(), getZ());
            BlockState state = level().getBlockState(pos);
            if (!state.isAir()) {
                detonate();
                discard();
                return;
            }
            if (tickCount > 400) {
                discard();
            }
        }
    }

    private void updateRotation() {
        Vec3 motion = getDeltaMovement();
        float horiz = Mth.sqrt((float) (motion.x * motion.x + motion.z * motion.z));
        setYRot((float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI)));
        setXRot((float) (Mth.atan2(motion.y, horiz) * (180.0D / Math.PI)) - 90.0F);
    }

    private void detonate() {
        double x = getX();
        double y = getY();
        double z = getZ();
        int type = getBombletType();
        switch (type) {
            case 1 -> {
                new ExplosionNT(level(), this, x + 0.5D, y + 1.5D, z + 0.5D, 4.0F)
                        .overrideResolution(12)
                        .addAttrib(ExplosionNT.ExAttrib.NODROP)
                        .addAttrib(ExplosionNT.ExAttrib.FIRE)
                        .explode();
                ExplosionThermo.setEntitiesOnFire(level(), x, y, z, 6);
            }
            case 2 -> {
                level().playSound(null, x, y, z, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.HOSTILE, 5.0F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
                EntityMist mist = new EntityMist(level())
                        .setChlorine()
                        .setArea(15.0F, 7.5F)
                        .setDuration(200);
                mist.setPos(x - getDeltaMovement().x, y - getDeltaMovement().y, z - getDeltaMovement().z);
                level().addFreshEntity(mist);
            }
            case 4 -> {
                // Mini-nuke / muke (legacy ExplosionNukeSmall.PARAMS_MEDIUM)
                ExplosionNukeSmall.explode(level(), x, y, z, ExplosionNukeSmall.PARAMS_MEDIUM);
            }
            default -> new ExplosionNT(level(), this, x + 0.5D, y + 1.5D, z + 0.5D, 4.0F)
                    .overrideResolution(12)
                    .addAttrib(ExplosionNT.ExAttrib.NODROP)
                    .explode();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_TYPE, tag.getByte("type"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putByte("type", entityData.get(DATA_TYPE));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 250.0D * 250.0D;
    }
}
