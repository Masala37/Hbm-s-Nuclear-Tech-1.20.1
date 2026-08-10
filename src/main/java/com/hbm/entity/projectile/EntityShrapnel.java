package com.hbm.entity.projectile;

import com.hbm.explosion.ExplosionNT;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Flying debris / volcano ejecta (legacy {@code EntityShrapnel}).
 * Type: 0 normal, 1 flame trail, 2 volcanic lava, 3 mud, 4 rad lava.
 */
public class EntityShrapnel extends ThrowableProjectile {
    private static final EntityDataAccessor<Byte> DATA_TYPE =
            SynchedEntityData.defineId(EntityShrapnel.class, EntityDataSerializers.BYTE);

    public EntityShrapnel(EntityType<? extends EntityShrapnel> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    public EntityShrapnel(Level level) {
        this(ModEntities.SHRAPNEL.get(), level);
    }

    public EntityShrapnel(Level level, double x, double y, double z) {
        this(level);
        setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_TYPE, (byte) 0);
    }

    public byte getShrapnelType() {
        return entityData.get(DATA_TYPE);
    }

    public void setTrail(boolean trail) {
        entityData.set(DATA_TYPE, trail ? (byte) 1 : (byte) 0);
    }

    public void setVolcano(boolean rad) {
        entityData.set(DATA_TYPE, rad ? (byte) 4 : (byte) 2);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && getShrapnelType() == 1) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        }
        byte type = getShrapnelType();
        // Volcano ejecta must live long enough to arc; normal shrapnel still despawns.
        if (type != 2 && type != 4 && tickCount > 200) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (tickCount <= 5) {
            return;
        }
        if (level().isClientSide) {
            return;
        }

        byte type = getShrapnelType();
        if (type == 2 || type == 4) {
            if (result instanceof BlockHitResult blockHit) {
                BlockPos pos = blockHit.getBlockPos();
                if (getDeltaMovement().y < -0.2D) {
                    BlockPos above = pos.above();
                    if (level().getBlockState(above).canBeReplaced()) {
                        Block lava = type == 2 ? ModBlocks.VOLCANIC_LAVA.get() : ModBlocks.RAD_LAVA.get();
                        level().setBlock(above, lava.defaultBlockState(), 3);
                    }
                    Block gas = ModBlocks.GAS_MONOXIDE.get();
                    for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++) {
                        for (int y = pos.getY(); y <= pos.getY() + 2; y++) {
                            for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
                                BlockPos p = new BlockPos(x, y, z);
                                if (level().getBlockState(p).isAir()) {
                                    level().setBlock(p, gas.defaultBlockState(), 3);
                                }
                            }
                        }
                    }
                }
                if (getDeltaMovement().y > 0) {
                    new ExplosionNT(level(), null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 7.0F)
                            .addAttrib(ExplosionNT.ExAttrib.NODROP)
                            .addAttrib(type == 2 ? ExplosionNT.ExAttrib.LAVA_V : ExplosionNT.ExAttrib.LAVA_R)
                            .addAttrib(ExplosionNT.ExAttrib.NOSOUND)
                            .addAttrib(ExplosionNT.ExAttrib.ALLMOD)
                            .addAttrib(ExplosionNT.ExAttrib.NOHURT)
                            .explode();
                }
            }
        } else {
            for (int i = 0; i < 5; i++) {
                level().addParticle(ParticleTypes.LAVA, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

        level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        if (hit instanceof LivingEntity living) {
            living.hurt(damageSources().thrown(this, getOwner()), 15.0F);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putByte("Type", getShrapnelType());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_TYPE, tag.getByte("Type"));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
