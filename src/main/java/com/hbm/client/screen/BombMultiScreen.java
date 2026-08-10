package com.hbm.client.screen;

import com.hbm.blockentity.bomb.BombMultiBlockEntity;
import com.hbm.inventory.menu.BombMultiMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BombMultiScreen extends AbstractContainerScreen<BombMultiMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/bomb_generic.png");

    public BombMultiScreen(BombMultiMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        BombMultiBlockEntity be = this.menu.getBlockEntity();
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int type2 = be.return2type();
        int type5 = be.return5type();
        if (type2 == type5 && type2 > 0 && type2 <= 6) {
            graphics.blit(TEXTURE, this.leftPos + 124, this.topPos + 34, 176, (type2 - 1) * 18, 18, 18);
        } else if (type2 != type5) {
            graphics.blit(TEXTURE, this.leftPos + 124, this.topPos + 34, 176, 7 * 18, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
