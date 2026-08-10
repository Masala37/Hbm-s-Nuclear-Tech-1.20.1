package com.hbm.entity.logic;

import com.hbm.config.BombConfig;
import com.hbm.explosion.ExplosionFleija;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionSolinium;
import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * MK3 specialty dig (legacy {@code EntityNukeExplosionMK3}) — FLEIJA / Solinium only.
 */
public class EntityNukeExplosionMK3 extends Entity {
    public int age;
    public int destructionRange;
    public int speed = 1;
    public float coefficient = 1.0F;
    public float coefficient2 = 1.0F;
    /** 0 = fleija, 1 = solinium */
    public int extType;
    public boolean did;

    private double blastX;
    private double blastY;
    private double blastZ;
    private boolean originSet;

    @Nullable
    private ExplosionFleija fleija;
    @Nullable
    private ExplosionSolinium solinium;

    public EntityNukeExplosionMK3(EntityType<? extends EntityNukeExplosionMK3> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    public EntityNukeExplosionMK3(Level level) {
        this(ModEntities.NUKE_EXPLOSION_MK3.get(), level);
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

        if (!originSet) {
            blastX = getX();
            blastY = getY();
            blastZ = getZ();
            originSet = true;
        }
        setDeltaMovement(Vec3.ZERO);
        setPos(blastX, blastY, blastZ);

        if (!did) {
            int ix = (int) Math.floor(blastX);
            int iy = (int) Math.floor(blastY);
            int iz = (int) Math.floor(blastZ);
            if (extType == 0) {
                fleija = new ExplosionFleija(ix, iy, iz, level(), destructionRange, coefficient, coefficient2);
            } else {
                solinium = new ExplosionSolinium(ix, iy, iz, level(), destructionRange, coefficient, coefficient2);
            }
            did = true;
        }

        speed += 1;
        boolean done = false;
        for (int i = 0; i < speed; i++) {
            if (extType == 0 && fleija != null) {
                if (fleija.update()) {
                    done = true;
                    break;
                }
            } else if (extType == 1 && solinium != null) {
                if (solinium.update()) {
                    done = true;
                    break;
                }
            }
        }

        if (!done) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER, 10000.0F, 0.8F + random.nextFloat() * 0.2F);
            if (extType == 0) {
                ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), destructionRange * 2.0D);
            }
            // Solinium legacy uses radiation hurt — poison-ish damage stand-in until rad system
            else {
                ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), destructionRange * 1.5D, 80.0F);
            }
        } else {
            discard();
        }

        age++;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("age");
        destructionRange = tag.getInt("destructionRange");
        speed = tag.getInt("speed");
        coefficient = tag.getFloat("coefficient");
        coefficient2 = tag.getFloat("coefficient2");
        extType = tag.getInt("extType");
        did = tag.getBoolean("did");
        if (tag.contains("blastX")) {
            blastX = tag.getDouble("blastX");
            blastY = tag.getDouble("blastY");
            blastZ = tag.getDouble("blastZ");
            originSet = true;
        }
        if (did) {
            int ix = (int) Math.floor(originSet ? blastX : getX());
            int iy = (int) Math.floor(originSet ? blastY : getY());
            int iz = (int) Math.floor(originSet ? blastZ : getZ());
            if (extType == 0) {
                fleija = new ExplosionFleija(ix, iy, iz, level(), destructionRange, coefficient, coefficient2);
                fleija.readFromNbt(tag, "expl_");
            } else {
                solinium = new ExplosionSolinium(ix, iy, iz, level(), destructionRange, coefficient, coefficient2);
                solinium.readFromNbt(tag, "sol_");
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("age", age);
        tag.putInt("destructionRange", destructionRange);
        tag.putInt("speed", speed);
        tag.putFloat("coefficient", coefficient);
        tag.putFloat("coefficient2", coefficient2);
        tag.putInt("extType", extType);
        tag.putBoolean("did", did);
        if (originSet) {
            tag.putDouble("blastX", blastX);
            tag.putDouble("blastY", blastY);
            tag.putDouble("blastZ", blastZ);
        }
        if (fleija != null) {
            fleija.saveToNbt(tag, "expl_");
        }
        if (solinium != null) {
            solinium.saveToNbt(tag, "sol_");
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public EntityNukeExplosionMK3 makeSol() {
        this.extType = 1;
        return this;
    }

    public static EntityNukeExplosionMK3 statFacFleija(Level level, double x, double y, double z, int range) {
        EntityNukeExplosionMK3 entity = new EntityNukeExplosionMK3(level);
        entity.setPos(x, y, z);
        entity.destructionRange = Math.max(1, range);
        entity.speed = BombConfig.blastSpeed.get();
        entity.coefficient = 1.0F;
        entity.coefficient2 = 1.0F;
        entity.extType = 0;
        return entity;
    }
}
