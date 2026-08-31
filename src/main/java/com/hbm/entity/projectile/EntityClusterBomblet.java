package com.hbm.entity.projectile;

import com.hbm.explosion.ExplosionNT;
import com.hbm.network.ExplosionSmallEffectPacket;
import com.hbm.network.ModMessages;
import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

/**
 * Chaos cluster submunition — legacy catapult cluster bullet
 * ({@code ExplosionVNT 7.5} + {@code ExplosionEffectWeapon(10, 2.5, 1)}).
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
        if (!level().isClientSide && tickCount > 1200) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide || tickCount < 3) {
            return;
        }
        double x = getX();
        double y = getY();
        double z = getZ();
        // Legacy submunition: 7.5F dig + weapon small-explosion SFX/FX.
        new ExplosionNT(level(), this, x, y, z, 7.5F)
                .overrideResolution(12)
                .addAttrib(ExplosionNT.ExAttrib.NODROP)
                .addAttrib(ExplosionNT.ExAttrib.NOSOUND)
                .addAttrib(ExplosionNT.ExAttrib.NOPARTICLE)
                .explode();
        if (level() instanceof ServerLevel server) {
            ModMessages.CHANNEL.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                            x, y, z, 200.0D, server.dimension())),
                    ExplosionSmallEffectPacket.weapon(x, y, z));
        }
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
