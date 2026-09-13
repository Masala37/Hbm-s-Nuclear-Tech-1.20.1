package com.hbm.client.screen;

import com.hbm.inventory.menu.CraneExtractorMenu;
import com.hbm.lib.RefStrings;
import com.hbm.network.CraneControlPacket;
import com.hbm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class CraneExtractorScreen extends AbstractContainerScreen<CraneExtractorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/storage/gui_crane_ejector.png");

    public CraneExtractorScreen(CraneExtractorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 212;
        this.imageHeight = 185;
        this.inventoryLabelX = 26;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isMaxEject()) {
            graphics.blit(TEXTURE, this.leftPos + 187, this.topPos + 34, 212, 0, 18, 18);
        }
        if (this.menu.isWhitelist()) {
            graphics.blit(TEXTURE, this.leftPos + 139, this.topPos + 33, 212, 18, 3, 6);
        } else {
            graphics.blit(TEXTURE, this.leftPos + 139, this.topPos + 47, 212, 18, 3, 6);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(187, 34, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Only take maximum possible: "
                    + (this.menu.isMaxEject()
                    ? ChatFormatting.GREEN + "ON"
                    : ChatFormatting.RED + "OFF")), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(187, 34, 18, 18, (int) mouseX, (int) mouseY)) {
            click();
            ModMessages.CHANNEL.sendToServer(new CraneControlPacket(
                    this.menu.getBlockEntity().getBlockPos(), CraneControlPacket.EXTRACTOR_MAX_EJECT));
            return true;
        }
        if (button == 0 && isHovering(128, 30, 14, 26, (int) mouseX, (int) mouseY)) {
            click();
            ModMessages.CHANNEL.sendToServer(new CraneControlPacket(
                    this.menu.getBlockEntity().getBlockPos(), CraneControlPacket.EXTRACTOR_WHITELIST));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
