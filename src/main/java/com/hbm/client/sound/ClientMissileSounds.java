package com.hbm.client.sound;

import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;

/** Client-only missile audio helpers. */
public final class ClientMissileSounds {
    private ClientMissileSounds() {
    }

    /** Legacy takeoff + looping rocket flame. Pad smoke is handled by LaunchPad client tick. */
    public static void playLaunch(EntityMissileBaseNT missile) {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.MISSILE_TAKEOFF.get(), 1.0F, 1.0F));

        double x = missile.getX();
        double y = missile.getY();
        double z = missile.getZ();
        if (mc.level != null) {
            mc.level.playLocalSound(x, y, z, ModSounds.MISSILE_TAKEOFF.get(),
                    SoundSource.PLAYERS, 4.0F, 1.0F, false);
        }
        mc.getSoundManager().play(new MissileEngineSound(missile));
    }

    public static void playEngine(EntityMissileBaseNT missile) {
        Minecraft.getInstance().getSoundManager().play(new MissileEngineSound(missile));
    }
}
