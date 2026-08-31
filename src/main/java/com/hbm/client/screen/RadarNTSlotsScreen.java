package com.hbm.client.screen;

import com.hbm.blockentity.machine.RadarNTBlockEntity;
import com.hbm.handler.RadarRules;
import com.hbm.inventory.menu.RadarNTMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class RadarNTSlotsScreen extends AbstractContainerScreen<RadarNTMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/machine/gui_radar_link.png");

    public RadarNTSlotsScreen(RadarNTMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 184;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - this.leftPos;
        int y = (int) mouseY - this.topPos;
        if (x >= 5 && x < 13 && y >= 5 && y < 13) {
            onClose();
            if (minecraft != null) {
                minecraft.setScreen(new RadarNTScreen(this.menu.getBlockEntity().getBlockPos()));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        int hx = mouseX - this.leftPos;
        int hy = mouseY - this.topPos;
        if (hx >= 5 && hx < 13 && hy >= 5 && hy < 13) {
            graphics.renderComponentTooltip(font, dollar("radar.toggleGui"), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        RadarNTBlockEntity radar = this.menu.getBlockEntity();
        if (radar.getEnergy().getEnergyStored() > 0) {
            int i = (int) ((long) radar.getEnergy().getEnergyStored() * 160 / RadarRules.MAX_POWER);
            graphics.blit(TEXTURE, x + 8, y + 64, 0, 185, i, 16);
        }
    }

    private static List<Component> dollar(String key) {
        String raw = I18n.get(key);
        String[] parts = raw.split("\\$");
        List<Component> list = new ArrayList<>(parts.length);
        for (String part : parts) {
            list.add(Component.literal(part));
        }
        return list;
    }
}
