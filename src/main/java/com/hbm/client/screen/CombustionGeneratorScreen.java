package com.hbm.client.screen;

import com.hbm.inventory.menu.CombustionGeneratorMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CombustionGeneratorScreen extends AbstractContainerScreen<CombustionGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_combustion_generator.png");

    public CombustionGeneratorScreen(CombustionGeneratorMenu menu, Inventory inv, Component title) {
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

        int burnTotal = this.menu.getBurnTimeTotal();
        if (burnTotal > 0) {
            int burn = this.menu.getBurnTime();
            int h = Math.max(1, burn * 14 / burnTotal);
            graphics.blit(TEXTURE, x + 56, y + 36 + 14 - h, 176, 17 + 14 - h, 14, h);
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
