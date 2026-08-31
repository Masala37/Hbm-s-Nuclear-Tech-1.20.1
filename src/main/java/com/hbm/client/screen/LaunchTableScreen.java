package com.hbm.client.screen;

import com.hbm.inventory.menu.LaunchTableMenu;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.lib.RefStrings;
import com.hbm.network.LaunchTablePadSizePacket;
import com.hbm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class LaunchTableScreen extends CustomLauncherScreen<LaunchTableMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/gui_launch_table.png");

    public LaunchTableScreen(LaunchTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected ResourceLocation texture() {
        return TEXTURE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isHovering(7, 98, 18, 18, (int) mouseX, (int) mouseY)) {
                sendSize(PartSize.SIZE_10);
                return true;
            }
            if (isHovering(25, 98, 18, 18, (int) mouseX, (int) mouseY)) {
                sendSize(PartSize.SIZE_15);
                return true;
            }
            if (isHovering(43, 98, 18, 18, (int) mouseX, (int) mouseY)) {
                sendSize(PartSize.SIZE_20);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendSize(PartSize size) {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        ModMessages.CHANNEL.sendToServer(
                new LaunchTablePadSizePacket(this.menu.getBlockEntity().getBlockPos(), size.ordinal()));
    }

    @Override
    protected void renderExtra(GuiGraphics graphics, int x, int y) {
        PartSize pad = this.menu.getPadSize();
        switch (pad) {
            case SIZE_10 -> graphics.blit(texture(), x + 7, y + 98, 176, 8, 18, 18);
            case SIZE_15 -> graphics.blit(texture(), x + 25, y + 98, 194, 8, 18, 18);
            case SIZE_20 -> graphics.blit(texture(), x + 43, y + 98, 212, 8, 18, 18);
            default -> {
            }
        }
    }

    @Override
    protected List<Component> sizeHint() {
        return List.of(
                Component.literal("Accepts custom missiles"),
                Component.literal("of all sizes, as long as the"),
                Component.literal("correct size setting is selected."));
    }

    @Override
    protected void renderExtraTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(7, 98, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Size 10 & 10/15"), mouseX, mouseY);
        } else if (isHovering(25, 98, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Size 15 & 15/20"), mouseX, mouseY);
        } else if (isHovering(43, 98, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Size 20"), mouseX, mouseY);
        }
    }
}
