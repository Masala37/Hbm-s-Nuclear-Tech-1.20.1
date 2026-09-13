package com.hbm.client.screen;

import com.hbm.inventory.menu.MachineSirenMenu;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MachineSirenScreen extends AbstractContainerScreen<MachineSirenMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_siren.png");

    public MachineSirenScreen(MachineSirenMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        TrackType type = this.menu.getBlockEntity().getCurrentType();
        if (type == TrackType.NULL) {
            return;
        }
        int color = 0xFF000000 | type.getColor();
        graphics.drawString(this.font, type.getTrackTitle(), 46, 28, color, false);
        graphics.drawString(this.font, "Type: " + type.getType().name(), 46, 40, color, false);
        graphics.drawString(this.font, "Volume: " + type.getVolume(), 46, 52, color, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
