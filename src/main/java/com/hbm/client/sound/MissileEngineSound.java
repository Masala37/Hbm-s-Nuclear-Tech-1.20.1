package com.hbm.client.sound;

import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.registry.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * Follows a missile in flight with looping rocket-flame audio (client-only).
 */
public class MissileEngineSound extends AbstractTickableSoundInstance {
    private final EntityMissileBaseNT missile;

    public MissileEngineSound(EntityMissileBaseNT missile) {
        super(ModSounds.ROCKET_FLAME.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.missile = missile;
        this.looping = true;
        this.volume = 2.0F;
        this.pitch = 0.85F;
        // NONE: keep audible while tracking a fast high-altitude missile.
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.x = missile.getX();
        this.y = missile.getY();
        this.z = missile.getZ();
        this.delay = 0;
    }

    @Override
    public void tick() {
        if (missile.isRemoved() || !missile.isAlive()) {
            stop();
            return;
        }
        this.x = missile.getX();
        this.y = missile.getY();
        this.z = missile.getZ();
        this.volume = missile.tickCount < 40 ? 2.5F : 1.75F;
    }
}
