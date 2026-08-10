package com.hbm.entity.item;

import com.hbm.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

/**
 * Rising fireworks rocket that bursts into a colored letter (legacy {@code EntityFireworks}).
 */
public class EntityFireworks extends Entity {
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(EntityFireworks.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHAR =
            SynchedEntityData.defineId(EntityFireworks.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LETTER =
            SynchedEntityData.defineId(EntityFireworks.class, EntityDataSerializers.BOOLEAN);

    private int letterAge;
    private boolean wasLetter;

    public EntityFireworks(EntityType<? extends EntityFireworks> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public EntityFireworks(Level level, double x, double y, double z, int color, int character) {
        this(ModEntities.FIREWORKS.get(), level);
        setPos(x, y, z);
        setColor(color);
        setCharacter(character);
        setLetterMode(false);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_COLOR, 0xFF0000);
        entityData.define(DATA_CHAR, (int) 'A');
        entityData.define(DATA_LETTER, false);
    }

    public int getColor() {
        return entityData.get(DATA_COLOR);
    }

    public void setColor(int color) {
        entityData.set(DATA_COLOR, color & 0xFFFFFF);
    }

    public char getCharacter() {
        return (char) entityData.get(DATA_CHAR).intValue();
    }

    public void setCharacter(int character) {
        entityData.set(DATA_CHAR, character);
    }

    public boolean isLetterMode() {
        return entityData.get(DATA_LETTER);
    }

    public void setLetterMode(boolean letter) {
        entityData.set(DATA_LETTER, letter);
    }

    public int getLetterAge() {
        return letterAge;
    }

    private DustParticleOptions dustOptions(float size) {
        int c = getColor();
        float r = ((c >> 16) & 0xFF) / 255.0F;
        float g = ((c >> 8) & 0xFF) / 255.0F;
        float b = (c & 0xFF) / 255.0F;
        return new DustParticleOptions(new Vector3f(r, g, b), size);
    }

    private void spawnBurstParticles() {
        DustParticleOptions dust = dustOptions(2.0F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 4, 0.6D, 0.6D, 0.6D, 0.0D);
            server.sendParticles(ParticleTypes.FIREWORK, getX(), getY(), getZ(), 80, 1.75D, 1.75D, 1.75D, 0.45D);
            server.sendParticles(dust, getX(), getY(), getZ(), 55, 2.0D, 2.0D, 2.0D, 0.08D);
            return;
        }
        for (int i = 0; i < 60; i++) {
            level().addParticle(ParticleTypes.FIREWORK, getX(), getY(), getZ(),
                    random.nextGaussian() * 0.6D,
                    random.nextGaussian() * 0.6D,
                    random.nextGaussian() * 0.6D);
            level().addParticle(dust, getX(), getY(), getZ(),
                    random.nextGaussian() * 0.5D,
                    random.nextGaussian() * 0.5D,
                    random.nextGaussian() * 0.5D);
        }
        level().addParticle(ParticleTypes.FLASH, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        level().addParticle(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isLetterMode()) {
            setPos(getX(), getY() + 3.0D, getZ());
            if (level().isClientSide) {
                level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0D, -0.3D, 0.0D);
                level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0.0D, -0.2D, 0.0D);
            }

            if (!level().isClientSide && tickCount > 30) {
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_BLAST,
                        SoundSource.NEUTRAL, 20.0F, 1.0F + random.nextFloat() * 0.2F);
                setLetterMode(true);
                setDeltaMovement(0.0D, 0.0D, 0.0D);
                spawnBurstParticles();
            }
            return;
        }

        if (!wasLetter) {
            letterAge = 0;
            wasLetter = true;
            if (level().isClientSide) {
                spawnBurstParticles();
            }
        }
        letterAge++;
        if (level().isClientSide) {
            DustParticleOptions dust = dustOptions(1.5F);
            for (int i = 0; i < 14; i++) {
                level().addParticle(ParticleTypes.FIREWORK, getX(), getY(), getZ(),
                        random.nextGaussian() * 0.45D,
                        random.nextGaussian() * 0.45D,
                        random.nextGaussian() * 0.45D);
                level().addParticle(dust, getX(), getY(), getZ(),
                        random.nextGaussian() * 0.35D,
                        random.nextGaussian() * 0.35D,
                        random.nextGaussian() * 0.35D);
            }
        }
        if (!level().isClientSide && letterAge >= 30) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setCharacter(tag.getInt("char"));
        setColor(tag.getInt("color"));
        setLetterMode(tag.getBoolean("letter"));
        letterAge = tag.getInt("letterAge");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("char", getCharacter());
        tag.putInt("color", getColor());
        tag.putBoolean("letter", isLetterMode());
        tag.putInt("letterAge", letterAge);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0D * 256.0D;
    }
}
