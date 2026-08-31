package com.hbm.client.sound;

import com.hbm.client.particle.ClientExplosionEffects;
import com.hbm.entity.missile.EntityMissileBaseNT;

/** Client-only missile audio helpers. */
public final class ClientMissileSounds {
    private ClientMissileSounds() {
    }

    /** 1.7.10 pad takeoff only — missiles have no in-flight engine loop. */
    public static void playLaunch(EntityMissileBaseNT missile) {
        ClientExplosionEffects.playMissileTakeoff(missile.getX(), missile.getY(), missile.getZ());
    }
}
