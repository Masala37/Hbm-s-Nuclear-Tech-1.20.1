package com.hbm.client;

import com.hbm.capability.HbmLivingProps;
import com.hbm.handler.GeigerClicks;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Hotbar Geiger HUD (legacy {@code RenderScreenOverlay.renderRadCounter}).
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class GeigerHud {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/misc/overlay_misc.png");
    private static final int BAR_LENGTH = 74;
    private static final int MAX_RAD = 1000;

    private static long lastSurvey;
    private static float prevResult;
    private static float lastResult;

    private GeigerHud() {
    }

    @SubscribeEvent
    public static void onHotbar(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        if (!hasHandheldGeiger(player)) {
            return;
        }
        render(event.getGuiGraphics(), event.getWindow().getGuiScaledHeight(), HbmLivingProps.getRadiation(player));
    }

    static boolean hasHandheldGeiger(LocalPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.GEIGER_COUNTER.get())) {
                return true;
            }
        }
        return player.getOffhandItem().is(ModItems.GEIGER_COUNTER.get());
    }

    static void render(GuiGraphics graphics, int screenHeight, float radiation) {
        float rate = lastResult - prevResult;
        long now = System.currentTimeMillis();
        if (now >= lastSurvey + 1000L) {
            lastSurvey = now;
            prevResult = lastResult;
            lastResult = radiation;
        }

        int bar = GeigerClicks.hudBarWidth(radiation, MAX_RAD, BAR_LENGTH);
        int posX = 16;
        int posY = screenHeight - 20;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(TEXTURE, posX, posY, 0, 0, 94, 18);
        graphics.blit(TEXTURE, posX + 1, posY + 1, 1, 19, bar, 16);

        if (rate >= 25.0F) {
            graphics.blit(TEXTURE, posX + BAR_LENGTH + 2, posY - 18, 36, 36, 18, 18);
        } else if (rate >= 10.0F) {
            graphics.blit(TEXTURE, posX + BAR_LENGTH + 2, posY - 18, 18, 36, 18, 18);
        } else if (rate >= 2.5F) {
            graphics.blit(TEXTURE, posX + BAR_LENGTH + 2, posY - 18, 0, 36, 18, 18);
        }

        Minecraft mc = Minecraft.getInstance();
        if (rate > 1000.0F) {
            graphics.drawString(mc.font, ">1000 RAD/s", posX, posY - 8, 0xFFFF0000, false);
        } else if (rate >= 1.0F) {
            graphics.drawString(mc.font, Math.round(rate) + " RAD/s", posX, posY - 8, 0xFFFF0000, false);
        } else if (rate > 0.0F) {
            graphics.drawString(mc.font, "<1 RAD/s", posX, posY - 8, 0xFFFF0000, false);
        }
        RenderSystem.disableBlend();
    }
}
