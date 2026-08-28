package com.hbm.client.sound;

import com.hbm.entity.effect.EntityBlackHole;
import net.minecraft.client.Minecraft;

/** Client-only black hole audio. */
public final class ClientBlackHoleSounds {
    private ClientBlackHoleSounds() {
    }

    public static void play(EntityBlackHole hole) {
        Minecraft.getInstance().getSoundManager().play(new BlackHoleSound(hole));
    }
}
