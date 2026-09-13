package com.hbm.client.screen;

import com.hbm.inventory.menu.FireboxMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class FireboxScreen extends AbstractContainerScreen<FireboxMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/machine/gui_firebox.png");

    public FireboxScreen(FireboxMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int maxHeat = Math.max(this.menu.getMaxHeat(), 1);
        int heatW = this.menu.getHeat() * 69 / maxHeat;
        if (heatW > 0) {
            graphics.blit(TEXTURE, x + 81, y + 28, 176, 0, heatW, 5);
        }
        int maxBurn = Math.max(this.menu.getMaxBurnTime(), 1);
        int burnW = this.menu.getBurnTime() * 70 / maxBurn;
        if (burnW > 0) {
            graphics.blit(TEXTURE, x + 81, y + 37, 176, 5, burnW, 5);
        }
        if (this.menu.wasOn()) {
            graphics.blit(TEXTURE, x + 25, y + 26, 176, 10, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(80, 27, 71, 7, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(
                    String.format("%,d", this.menu.getHeat()) + " / "
                            + String.format("%,d", this.menu.getMaxHeat()) + "TU"), mouseX, mouseY);
        } else if (isHovering(80, 36, 71, 7, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.literal(this.menu.getBurnHeat() + "TU/t"),
                    Component.literal((this.menu.getBurnTime() / 20) + "s")), mouseX, mouseY);
        }
    }
}
