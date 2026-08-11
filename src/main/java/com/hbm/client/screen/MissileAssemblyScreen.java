package com.hbm.client.screen;

import com.hbm.inventory.menu.MissileAssemblyMenu;
import com.hbm.lib.RefStrings;
import com.hbm.network.AssembleMissilePacket;
import com.hbm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

/**
 * Legacy {@code GUIMachineMissileAssembly} — 176×222, texture assemble button at (115,35).
 */
public class MissileAssemblyScreen extends AbstractContainerScreen<MissileAssemblyMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_missile_assembly.png");

    public MissileAssemblyScreen(MissileAssemblyMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(115, 35, 18, 18, (int) mouseX, (int) mouseY)) {
            if (this.minecraft != null && this.minecraft.getSoundManager() != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            ModMessages.CHANNEL.sendToServer(new AssembleMissilePacket(this.menu.getBlockEntity().getBlockPos()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.getBlockEntity().canAssemble()) {
            // Legacy ready overlay UV 176,0 size 18×18
            graphics.blit(TEXTURE, this.leftPos + 115, this.topPos + 35, 176, 0, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(115, 35, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Assemble"), mouseX, mouseY);
        }
    }
}
