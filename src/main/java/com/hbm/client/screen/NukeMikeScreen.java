package com.hbm.client.screen;

import com.hbm.blockentity.bomb.NukeMikeBlockEntity;
import com.hbm.inventory.menu.NukeMikeMenu;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class NukeMikeScreen extends AbstractContainerScreen<NukeMikeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/ivy_mike_schematic.png");

    public NukeMikeScreen(NukeMikeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 217;
        this.titleLabelY = 4;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        NukeMikeBlockEntity be = this.menu.getBlockEntity();
        if (be.isReady() && !be.isFilled()) {
            graphics.blit(TEXTURE, x + 5, y + 35, 177, 1, 16, 16);
        }
        if (be.isReady() && be.isFilled()) {
            graphics.blit(TEXTURE, x + 5, y + 35, 177, 19, 16, 16);
        }

        ItemStack core = be.getItems().getStackInSlot(NukeMikeBlockEntity.SLOT_MIKE_CORE);
        ItemStack deut = be.getItems().getStackInSlot(NukeMikeBlockEntity.SLOT_MIKE_DEUT);
        ItemStack cool = be.getItems().getStackInSlot(NukeMikeBlockEntity.SLOT_COOLING);

        if (!core.isEmpty() && core.is(ModItems.MIKE_CORE.get())) {
            graphics.blit(TEXTURE, x + 75, y + 25, 176, 49, 80, 36);
        }
        if (!deut.isEmpty() && deut.is(ModItems.MIKE_DEUT.get())) {
            graphics.blit(TEXTURE, x + 79, y + 30, 180, 88, 58, 26);
        }
        if (!cool.isEmpty() && cool.is(ModItems.MIKE_COOLING_UNIT.get())) {
            graphics.blit(TEXTURE, x + 140, y + 30, 240, 88, 12, 26);
        }

        for (int i = 0; i < 4; i++) {
            ItemStack lens = be.getItems().getStackInSlot(i);
            if (!lens.isEmpty() && lens.is(ModItems.EXPLOSIVE_LENSES.get())) {
                switch (i) {
                    case 0 -> graphics.blit(TEXTURE, x + 24, y + 20, 209, 1, 23, 23);
                    case 2 -> graphics.blit(TEXTURE, x + 47, y + 20, 232, 1, 23, 23);
                    case 1 -> graphics.blit(TEXTURE, x + 24, y + 43, 209, 24, 23, 23);
                    case 3 -> graphics.blit(TEXTURE, x + 47, y + 43, 232, 24, 23, 23);
                    default -> {
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
