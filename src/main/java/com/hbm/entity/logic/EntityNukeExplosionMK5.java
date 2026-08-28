package com.hbm.entity.logic;

import com.hbm.api.explosion.IExplosionRay;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityFalloutRain;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionNukeRayBatched;
import com.hbm.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Multi-tick nuclear dig entity (legacy EntityNukeExplosionMK5).
 * Spawns fallout rain when the dig finishes. Radiation deferred.
 */
public class EntityNukeExplosionMK5 extends Entity {
    public int strength;
    public int speed;
    public int length;
    public boolean fallout = true;
    private int falloutAdd;

    private double blastX;
    private double blastY;
    private double blastZ;
    private boolean originSet;

    private IExplosionRay explosion;
    private boolean flashed;
    /** When true, skip port-added vanilla flash/smoke on first tick (legacy MK5 has none). */
    private boolean suppressFlashFx;

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

        if (!originSet) {
            blastX = getX();
            blastY = getY();
            blastZ = getZ();
            originSet = true;
        }
        setDeltaMovement(Vec3.ZERO);
        setPos(blastX, blastY, blastZ);

        if (!flashed && !suppressFlashFx && level() instanceof ServerLevel server) {
            flashed = true;
            float scale = Math.max(2.0F, length / 8.0F);
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1.0D, getZ(), 8, scale * 0.25D, scale * 0.25D, scale * 0.25D, 0.0D);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 2.0D, getZ(), 40, scale, scale * 0.5D, scale, 0.05D);
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                    16.0F, 0.5F + random.nextFloat() * 0.2F);
        } else if (!flashed && suppressFlashFx) {
            flashed = true;
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
            // Match config budget directly (legacy mk5BlastTime default 50).
            int budget = Math.max(1, BombConfig.mk5.get());
            explosion.cacheChunksTick(budget);
            explosion.destructionTick(budget);
        } else {
            if (fallout && BombConfig.falloutRange.get() > 0) {
                EntityFalloutRain rain = new EntityFalloutRain(level());
                rain.setPos(getX(), getY(), getZ());
                int scale = (int) (this.length * 2.5D + falloutAdd) * BombConfig.falloutRange.get() / 100;
                rain.setScale(Math.max(1, scale));
                level().addFreshEntity(rain);
                com.hbm.handler.radiation.ChunkRadiationManager.INSTANCE.incrementRad(
                        level(), (int) getX(), (int) getY(), (int) getZ(), Math.max(10.0F, scale * 0.5F));
            }
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
        this.flashed = tag.getBoolean("flashed");
        this.suppressFlashFx = tag.getBoolean("suppressFlashFx");
        this.fallout = !tag.contains("fallout") || tag.getBoolean("fallout");
        this.falloutAdd = tag.getInt("falloutAdd");
        if (tag.contains("blastX")) {
            this.blastX = tag.getDouble("blastX");
            this.blastY = tag.getDouble("blastY");
            this.blastZ = tag.getDouble("blastZ");
            this.originSet = true;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("strength", strength);
        tag.putInt("speed", speed);
        tag.putInt("length", length);
        tag.putBoolean("flashed", flashed);
        tag.putBoolean("suppressFlashFx", suppressFlashFx);
        tag.putBoolean("fallout", fallout);
        tag.putInt("falloutAdd", falloutAdd);
        if (originSet) {
            tag.putDouble("blastX", blastX);
            tag.putDouble("blastY", blastY);
            tag.putDouble("blastZ", blastZ);
        }
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

    public static EntityNukeExplosionMK5 statFacNoRad(Level level, int r, double x, double y, double z) {
        EntityNukeExplosionMK5 mk5 = statFac(level, r, x, y, z);
        mk5.fallout = false;
        return mk5;
    }

    public EntityNukeExplosionMK5 moreFallout(int fallout) {
        this.falloutAdd = fallout;
        return this;
    }

    /** Skip port-only vanilla flash particles (legacy MK5 is silent/invisible aside from dig). */
    public EntityNukeExplosionMK5 suppressFlashFx() {
        this.suppressFlashFx = true;
        return this;
    }
}
