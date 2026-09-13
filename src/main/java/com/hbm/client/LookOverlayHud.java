package com.hbm.client;

import com.hbm.blocks.ILookOverlay;
import com.hbm.lib.RefStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code ILookOverlay.printGeneric}: title plus tank/heat lines while looking at a machine.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LookOverlayHud {
    private LookOverlayHud() {
    }

    @SubscribeEvent
    public static void onCrosshair(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null || mc.options.hideGui || mc.screen != null) {
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        Block block = level.getBlockState(pos).getBlock();
        if (!(block instanceof ILookOverlay overlay)) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        overlay.printHook(level, pos, lines);
        if (lines.isEmpty()) {
            return;
        }
        render(event.getGuiGraphics(), mc.font, event.getWindow().getGuiScaledWidth(),
                event.getWindow().getGuiScaledHeight(), block.getName(), lines);
    }

    static void render(GuiGraphics graphics, Font font, int screenWidth, int screenHeight,
                       Component title, List<Component> lines) {
        int width = font.width(title);
        for (Component line : lines) {
            width = Math.max(width, font.width(line));
        }
        int x = screenWidth / 2 + 12;
        int y = screenHeight / 2 + 12;
        int height = 10 + lines.size() * 10;
        graphics.fill(x - 2, y - 2, x + width + 2, y + height, 0xC0404000);
        graphics.drawString(font, title, x, y, 0xFFFF00, true);
        int row = y + 10;
        for (Component line : lines) {
            graphics.drawString(font, line, x, row, 0xFFFFFF, true);
            row += 10;
        }
    }
}
