package com.hbm.client.screen;

import com.hbm.blockentity.bomb.NukeGadgetBlockEntity;
import com.hbm.inventory.menu.NukeGadgetMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NukeGadgetScreen extends AbstractContainerScreen<NukeGadgetMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/gadget_schematic.png");

    public NukeGadgetScreen(NukeGadgetMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        NukeGadgetBlockEntity be = this.menu.getBlockEntity();
        if (!be.getItems().getStackInSlot(NukeGadgetBlockEntity.SLOT_LENS_1).isEmpty()) {
            graphics.blit(TEXTURE, x + 82, y + 19, 176, 0, 24, 24);
        }
        if (!be.getItems().getStackInSlot(NukeGadgetBlockEntity.SLOT_LENS_2).isEmpty()) {
            graphics.blit(TEXTURE, x + 106, y + 19, 200, 0, 24, 24);
        }
        if (!be.getItems().getStackInSlot(NukeGadgetBlockEntity.SLOT_LENS_3).isEmpty()) {
            graphics.blit(TEXTURE, x + 82, y + 43, 176, 24, 24, 24);
        }
        if (!be.getItems().getStackInSlot(NukeGadgetBlockEntity.SLOT_LENS_4).isEmpty()) {
            graphics.blit(TEXTURE, x + 106, y + 43, 200, 24, 24, 24);
        }
        if (be.isReady()) {
            graphics.blit(TEXTURE, x + 134, y + 35, 176, 48, 16, 16);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
