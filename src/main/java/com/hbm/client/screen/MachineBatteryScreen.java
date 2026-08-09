package com.hbm.client.screen;

import com.hbm.inventory.menu.MachineBatteryMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MachineBatteryScreen extends AbstractContainerScreen<MachineBatteryMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/storage/gui_battery.png");

    public MachineBatteryScreen(MachineBatteryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int maxEnergy = this.menu.getMaxEnergy();
        if (maxEnergy > 0) {
            int energy = this.menu.getEnergy();
            int h = energy * 52 / maxEnergy;
            if (h > 0) {
                graphics.blit(TEXTURE, x + 62, y + 69 - h, 176, 52 - h, 52, h);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(62, 17, 52, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        }
    }
}
