package com.hbm.client.screen;

import com.hbm.blockentity.machine.PressBlockEntity;
import com.hbm.inventory.menu.PressMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PressScreen extends AbstractContainerScreen<PressMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_press.png");

    public PressScreen(PressMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 202;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.getBurnTime() >= 20) {
            graphics.blit(TEXTURE, x + 27, y + 36, 0, 202, 14, 14);
        }
        PressBlockEntity be = this.menu.getBlockEntity();
        double render = be.getRenderPress(partialTick);
        int k = (int) (render * 16 / PressBlockEntity.MAX_PRESS);
        if (k > 0) {
            graphics.blit(TEXTURE, x + 79, y + 35, 14, 202, 18, k);
        }
        int speedH = this.menu.getSpeed() * 14 / PressBlockEntity.MAX_SPEED;
        if (speedH > 0) {
            graphics.blit(TEXTURE, x + 27, y + 16 + (14 - speedH), 176, 14 - speedH, 14, speedH);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(25, 16, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal((this.menu.getSpeed() * 100 / PressBlockEntity.MAX_SPEED) + "%"),
                    mouseX, mouseY);
        }
        if (isHovering(25, 34, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal((this.menu.getBurnTime() / 200) + " operations left"),
                    mouseX, mouseY);
        }
    }
}
