package com.hbm.client.screen;

import com.hbm.inventory.menu.ElectricFurnaceMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/GUIElectricFurnace.png");

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 186;
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
            int h = energy * 34 / maxEnergy;
            if (h > 0) {
                graphics.blit(TEXTURE, x + 152, y + 52 - h, 176, 34 - h, 16, h);
            }
        }

        int cookTotal = this.menu.getCookTimeTotal();
        if (cookTotal > 0) {
            int progress = this.menu.getCookProgress();
            int w = progress * 24 / cookTotal;
            if (w > 0) {
                graphics.blit(TEXTURE, x + 79, y + 34, 176, 0, w, 17);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(152, 18, 16, 34, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        }
    }
}
