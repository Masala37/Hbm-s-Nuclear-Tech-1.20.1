package com.hbm.entity.projectile;

import com.hbm.explosion.ExplosionNT;
import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Multi-bomb / chaos cluster submunition (stand-in for legacy catapult cluster bullets).
 */
public class EntityClusterBomblet extends ThrowableProjectile {
    public EntityClusterBomblet(EntityType<? extends EntityClusterBomblet> type, Level level) {
        super(type, level);
    }

    public EntityClusterBomblet(Level level) {
        this(ModEntities.CLUSTER_BOMBLET.get(), level);
    }

    public EntityClusterBomblet(Level level, double x, double y, double z) {
        this(level);
        setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 120) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide || tickCount < 3) {
            return;
        }
        new ExplosionNT(level(), this, getX(), getY(), getZ(), 2.5F)
                .overrideResolution(8)
                .addAttrib(ExplosionNT.ExAttrib.NODROP)
                .explode();
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (result.getEntity() instanceof LivingEntity living) {
            living.hurt(damageSources().thrown(this, getOwner()), 8.0F);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
