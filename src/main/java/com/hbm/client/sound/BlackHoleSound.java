package com.hbm.client.sound;

import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.registry.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * Follows a live singularity. Uses the legacy meteorite falling loop as a rumble bed
 * (1.7.10 {@code EntityBlackHole} itself had no dedicated loop).
 */
public class BlackHoleSound extends AbstractTickableSoundInstance {
    private final EntityBlackHole hole;

    public BlackHoleSound(EntityBlackHole hole) {
        super(ModSounds.BLACK_HOLE.get(), SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
        this.hole = hole;
        this.looping = true;
        this.volume = 2.0F;
        this.pitch = 0.55F;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.x = hole.getX();
        this.y = hole.getY();
        this.z = hole.getZ();
        this.delay = 0;
    }

    @Override
    public void tick() {
        if (hole.isRemoved() || !hole.isAlive()) {
            stop();
            return;
        }
        this.x = hole.getX();
        this.y = hole.getY();
        this.z = hole.getZ();
        float size = Math.max(0.5F, hole.getHoleSize());
        this.volume = 1.5F + size;
    }
}
