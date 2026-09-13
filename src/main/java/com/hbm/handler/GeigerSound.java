package com.hbm.handler;

import com.hbm.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class GeigerSound {
    private GeigerSound() {
    }

    public static void play(Level level, double x, double y, double z, int track) {
        if (track <= 0 || level.isClientSide) {
            return;
        }
        level.playSound(null, x, y, z, ModSounds.require("item.geiger" + track), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
