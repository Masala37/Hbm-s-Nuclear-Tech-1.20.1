package com.hbm.client.screen;

import com.hbm.blockentity.machine.EPressBlockEntity;
import com.hbm.energy.ItemChargeStorage;
import com.hbm.inventory.menu.EPressMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EPressScreen extends AbstractContainerScreen<EPressMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_epress.png");

    public EPressScreen(EPressMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
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
            int h = this.menu.getEnergy() * 34 / maxEnergy;
            if (h > 0) {
                graphics.blit(TEXTURE, x + 152, y + 52 - h, 176, 34 - h, 16, h);
            }
        }
        EPressBlockEntity be = this.menu.getBlockEntity();
        int k = (int) (be.getRenderPress(partialTick) * 16 / EPressBlockEntity.MAX_PRESS);
        if (k > 0) {
            graphics.blit(TEXTURE, x + 18, y + 33, 192, 0, 18, k);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(152, 18, 16, 34, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(ItemChargeStorage.shortNumber(this.menu.getEnergy())
                            + " / " + ItemChargeStorage.shortNumber(this.menu.getMaxEnergy()) + " FE"),
                    mouseX, mouseY);
        }
    }
}
