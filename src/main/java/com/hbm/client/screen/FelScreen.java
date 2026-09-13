package com.hbm.client.screen;

import com.hbm.blockentity.machine.FelBlockEntity;
import com.hbm.inventory.menu.FelMenu;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.lib.RefStrings;
import com.hbm.network.FelControlPacket;
import com.hbm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class FelScreen extends AbstractContainerScreen<FelMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/machine/gui_fel.png");

    public FelScreen(FelMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 203;
        this.imageHeight = 169;
        this.inventoryLabelY = this.imageHeight - 98;
        this.titleLabelY = 7;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = 90 + this.imageWidth / 2 - this.font.width(this.title) / 2;
        graphics.drawString(this.font, this.title, titleX, 7, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
        if (this.menu.isOn() && this.menu.missingValidSilex()) {
            graphics.drawString(this.font, "ERR.",
                    55 + this.imageWidth / 2 - this.font.width(this.title) / 2, 9, 0xFF0000, false);
        } else if (this.menu.isOn()) {
            graphics.drawString(this.font, "LIVE",
                    54 + this.imageWidth / 2 - this.font.width(this.title) / 2, 9, 0x00FF00, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isOn()) {
            graphics.blit(TEXTURE, x + 142, y + 41, 203, 0, 29, 17);
        }
        int k = (int) this.menu.getBlockEntity().getPowerScaled(114);
        if (k > 0) {
            graphics.blit(TEXTURE, x + 182, y + 27 + 113 - k, 203, 17 + 113 - k, 16, k);
        }
        FelBlockEntity fel = this.menu.getBlockEntity();
        if (fel.beamVisible()) {
            int color = beamColor(fel);
            graphics.fill(x + 113, y + 31, x + 135, y + 32, color | 0xFF000000);
            graphics.fill(x, y + 31, x + 4, y + 32, color | 0xFF000000);
        }
    }

    private int beamColor(FelBlockEntity fel) {
        EnumWavelengths mode = fel.getMode();
        if (mode == EnumWavelengths.VISIBLE) {
            long time = fel.getLevel() == null ? 0 : fel.getLevel().getGameTime();
            return Mth.hsvToRgb((time % 50) / 50.0F, 0.5F, 1.0F);
        }
        return mode.guiColor;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(142, 41, 29, 17, (int) mouseX, (int) mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ModMessages.CHANNEL.sendToServer(new FelControlPacket(this.menu.getBlockEntity().getBlockPos()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(182, 27, 16, 113, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(
                    this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
    }
}
