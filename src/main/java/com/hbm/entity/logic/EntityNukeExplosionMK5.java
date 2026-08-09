package com.hbm.entity.logic;

import com.hbm.api.explosion.IExplosionRay;
import com.hbm.config.BombConfig;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionNukeRayBatched;
import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * Multi-tick nuclear dig entity (legacy EntityNukeExplosionMK5).
 * Fallout / radiation / chunkloading intentionally omitted for this slice.
 */
public class EntityNukeExplosionMK5 extends Entity {
    public int strength;
    public int speed;
    public int length;

    private IExplosionRay explosion;

    public EntityNukeExplosionMK5(EntityType<? extends EntityNukeExplosionMK5> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    public EntityNukeExplosionMK5(Level level) {
        this(ModEntities.NUKE_EXPLOSION_MK5.get(), level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        if (strength == 0) {
            discard();
            return;
        }

        ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), this.length * 2.0D);

        if (explosion == null) {
            explosion = new ExplosionNukeRayBatched(
                    level(),
                    (int) getX(),
                    (int) getY(),
                    (int) getZ(),
                    strength,
                    speed,
                    length);
        }

        if (!explosion.isComplete()) {
            explosion.cacheChunksTick(BombConfig.mk5.get());
            explosion.destructionTick(BombConfig.mk5.get());
        } else {
            discard();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (explosion != null) {
            explosion.cancel();
        }
        super.remove(reason);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.strength = tag.getInt("strength");
        this.speed = tag.getInt("speed");
        this.length = tag.getInt("length");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("strength", strength);
        tag.putInt("speed", speed);
        tag.putInt("length", length);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static EntityNukeExplosionMK5 statFac(Level level, int r, double x, double y, double z) {
        if (r == 0) {
            r = 25;
        }
        r *= 2;

        EntityNukeExplosionMK5 mk5 = new EntityNukeExplosionMK5(level);
        mk5.strength = r;
        mk5.speed = (int) Math.ceil(100_000.0D / mk5.strength);
        mk5.length = mk5.strength / 2;
        mk5.setPos(x, y, z);
        return mk5;
    }
}
