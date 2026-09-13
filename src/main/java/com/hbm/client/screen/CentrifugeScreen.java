package com.hbm.client.screen;

import com.hbm.blockentity.machine.CentrifugeBlockEntity;
import com.hbm.inventory.menu.CentrifugeMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CentrifugeScreen extends AbstractContainerScreen<CentrifugeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_centrifuge.png");

    public CentrifugeScreen(CentrifugeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 1000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int maxEnergy = Math.max(this.menu.getMaxEnergy(), 1);
        int energyH = this.menu.getEnergy() * 35 / maxEnergy;
        if (this.menu.getEnergy() > 0 && energyH > 0) {
            graphics.blit(TEXTURE, x + 9, y + 48 - energyH, 176, 35 - energyH, 16, energyH);
        }
        if (this.menu.getProgress() > 0) {
            int p = this.menu.getProgress() * 145 / CentrifugeBlockEntity.PROCESSING_SPEED;
            for (int i = 0; i < 4; i++) {
                int h = Math.min(p, 36);
                graphics.blit(TEXTURE, x + 65 + i * 20, y + 50 - h, 176, 71 - h, 12, h);
                p -= h;
                if (p <= 0) {
                    break;
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(9, 13, 16, 34, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(
                    this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
    }
}
