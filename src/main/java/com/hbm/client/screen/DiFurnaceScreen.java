package com.hbm.client.screen;

import com.hbm.blockentity.machine.DiFurnaceBlockEntity;
import com.hbm.inventory.menu.DiFurnaceMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Locale;

public class DiFurnaceScreen extends AbstractContainerScreen<DiFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_di_furnace.png");

    public DiFurnaceScreen(DiFurnaceMenu menu, Inventory inv, Component title) {
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
        if (this.menu.getFuel() > 0) {
            int i1 = this.menu.getFuel() * 52 / DiFurnaceBlockEntity.MAX_FUEL;
            graphics.blit(TEXTURE, x + 44, y + 70 - i1, 201, 53 - i1, 16, i1);
        }
        int j1 = this.menu.getProgress() * 24 / DiFurnaceBlockEntity.PROCESSING_SPEED;
        graphics.blit(TEXTURE, x + 101, y + 35, 176, 14, j1 + 1, 17);
        if (this.menu.getFuel() > 0 && (this.menu.canProcess() || j1 > 0)) {
            graphics.blit(TEXTURE, x + 63, y + 37, 176, 0, 14, 14);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (!this.menu.getCarried().isEmpty()) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            Slot slot = this.menu.getSlot(i);
            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                int face = switch (i) {
                    case 0 -> this.menu.getSideUpper();
                    case 1 -> this.menu.getSideLower();
                    default -> this.menu.getSideFuel();
                };
                String dir = Direction.from3DDataValue(face).getSerializedName().toUpperCase(Locale.ROOT);
                int oy = slot.hasItem() ? 15 : 0;
                graphics.renderTooltip(this.font,
                        Component.translatable("gui.diFurnace.acceptsFrom", dir),
                        mouseX, mouseY - oy);
                break;
            }
        }
    }
}
