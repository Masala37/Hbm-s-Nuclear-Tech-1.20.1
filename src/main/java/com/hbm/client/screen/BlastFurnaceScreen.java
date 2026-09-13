package com.hbm.client.screen;

import com.hbm.blockentity.machine.BlastFurnaceBlockEntity;
import com.hbm.inventory.menu.BlastFurnaceMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BlastFurnaceScreen extends AbstractContainerScreen<BlastFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_blast_furnace.png");

    public BlastFurnaceScreen(BlastFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int fuel = (int) Math.round((double) this.menu.getFuel() * 26.0D / BlastFurnaceBlockEntity.MAX_FUEL);
        int prog = (int) Math.round(this.menu.getProgress() * (88.0D - fuel));
        graphics.blit(TEXTURE, x + 62, y + 106 - prog - fuel, 176, 102 - prog - fuel, 56, prog);
        graphics.blit(TEXTURE, x + 62, y + 106 - fuel, 176, 128 - fuel, 56, fuel);
        if (this.menu.isProgressing()) {
            graphics.blit(TEXTURE, x + 81, y + 64, 176, 0, 14, 14);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(79, 62, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal("Speed: " + (int) (this.menu.getSpeed() * 100) + "%"),
                    mouseX, mouseY);
        }
    }
}
