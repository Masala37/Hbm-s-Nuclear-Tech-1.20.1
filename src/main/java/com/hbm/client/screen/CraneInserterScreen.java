package com.hbm.client.screen;

import com.hbm.inventory.menu.CraneInserterMenu;
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

public class CraneInserterScreen extends AbstractContainerScreen<CraneInserterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/storage/gui_crane_inserter.png");

    public CraneInserterScreen(CraneInserterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 185;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = this.imageWidth / 2 - 18;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isDestroyer()) {
            graphics.blit(TEXTURE, this.leftPos + 151, this.topPos + 34, 176, 0, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(151, 34, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Destroy overflow: "
                    + (this.menu.isDestroyer()
                    ? ChatFormatting.GREEN + "ON"
                    : ChatFormatting.RED + "OFF")), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(151, 34, 18, 18, (int) mouseX, (int) mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ModMessages.CHANNEL.sendToServer(new CraneControlPacket(
                    this.menu.getBlockEntity().getBlockPos(), CraneControlPacket.INSERTER_DESTROYER));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
