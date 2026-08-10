package com.hbm.client;

import com.hbm.lib.RefStrings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Nuke HUD flash/shake (legacy {@code ModEventHandlerClient} + {@code ModEventHandlerRenderer}).
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class NukeFxClient {
    public static final int FLASH_DURATION_MS = 5000;
    public static final int SHAKE_DURATION_MS = 1500;

    public static long flashTimestamp;
    public static long shakeTimestamp;

    private NukeFxClient() {
    }

    public static void markFlash() {
        if (System.currentTimeMillis() - flashTimestamp > 1000L) {
            flashTimestamp = System.currentTimeMillis();
        }
    }

    public static void markShake() {
        if (System.currentTimeMillis() - shakeTimestamp > 1000L) {
            shakeTimestamp = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        long remaining = shakeTimestamp + SHAKE_DURATION_MS - System.currentTimeMillis();
        if (remaining <= 0) {
            return;
        }
        if (Minecraft.getInstance().player == null) {
            return;
        }
        double mult = remaining / (double) SHAKE_DURATION_MS * 2.0D;
        double t = System.currentTimeMillis();
        float yaw = (float) (Math.sin(t * 0.02D) * 1.8D * mult);
        float pitch = (float) (Math.sin(t * 0.01D + 2.0D) * 0.9D * mult);
        event.setYaw(event.getYaw() + yaw);
        event.setPitch(event.getPitch() + pitch);
    }
}
