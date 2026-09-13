package com.hbm.client.screen;

import com.hbm.blockentity.machine.ShredderBlockEntity;
import com.hbm.inventory.menu.ShredderMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ShredderScreen extends AbstractContainerScreen<ShredderMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_shredder.png");

    public ShredderScreen(ShredderMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 233;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int max = this.menu.getMaxEnergy();
        if (max > 0 && this.menu.getEnergy() > 0) {
            int i = this.menu.getEnergy() * 88 / max;
            graphics.blit(TEXTURE, x + 8, y + 106 - i, 176, 160 - i, 16, i);
        }
        int progress = this.menu.getProgress() * 34 / ShredderBlockEntity.PROCESSING_SPEED;
        graphics.blit(TEXTURE, x + 63, y + 89, 176, 54, progress + 1, 18);
        blitGear(graphics, x + 43, y + 71, this.menu.getGearLeft(), 176);
        blitGear(graphics, x + 79, y + 71, this.menu.getGearRight(), 194);
        boolean broken = this.menu.getGearLeft() == 0 || this.menu.getGearLeft() == 3
                || this.menu.getGearRight() == 0 || this.menu.getGearRight() == 3;
        if (broken) {
            graphics.blit(TEXTURE, x - 16, y + 36, 176, 72, 16, 16);
        }
    }

    private static void blitGear(GuiGraphics graphics, int x, int y, int gear, int u) {
        if (gear == 1) {
            graphics.blit(TEXTURE, x, y, u, 0, 18, 18);
        } else if (gear == 2) {
            graphics.blit(TEXTURE, x, y, u, 18, 18, 18);
        } else if (gear == 3) {
            graphics.blit(TEXTURE, x, y, u, 36, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(8, 18, 16, 88, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        }
        boolean broken = this.menu.getGearLeft() == 0 || this.menu.getGearLeft() == 3
                || this.menu.getGearRight() == 0 || this.menu.getGearRight() == 3;
        if (broken && isHovering(-16, 36, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal("Error: Shredder blades are broken or missing!"),
                    mouseX, mouseY);
        }
    }
}
