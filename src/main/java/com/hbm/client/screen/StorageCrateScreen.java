package com.hbm.client.screen;

import com.hbm.inventory.menu.StorageCrateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Vanilla generic chest look for storage crates.
 */
public class StorageCrateScreen extends AbstractContainerScreen<StorageCrateMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    public StorageCrateScreen(StorageCrateMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 114 + menu.getRows() * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int rows = menu.getRows();
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, rows * 18 + 17);
        graphics.blit(TEXTURE, x, y + rows * 18 + 17, 0, 126, imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
