package com.hbm.client;

import com.hbm.items.armor.HazmatArmorItem;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Full-screen yellow-hazmat visor (legacy {@code ArmorHazmat.renderHelmetOverlay}).
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HazmatOverlayHud {
    private static final ResourceLocation OVERLAY =
            new ResourceLocation(RefStrings.MODID, "textures/misc/overlay_hazmat.png");

    private HazmatOverlayHud() {
    }

    @SubscribeEvent
    public static void onHelmet(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HELMET.type()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof HazmatArmorItem armor) || !armor.helmetOverlay()) {
            return;
        }
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        event.getGuiGraphics().blit(OVERLAY, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.enableDepthTest();
    }
}
