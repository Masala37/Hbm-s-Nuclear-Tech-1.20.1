package com.hbm.client.screen;

import com.hbm.inventory.menu.NukeBoyMenu;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class NukeBoyScreen extends AbstractContainerScreen<NukeBoyMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/lil_boy_schematic.png");

    public NukeBoyScreen(NukeBoyMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.getBlockEntity().isReady()) {
            graphics.blit(TEXTURE, x + 142, y + 90, 176, 0, 16, 16);
        }

        ItemStack s0 = this.menu.getBlockEntity().getItems().getStackInSlot(0);
        ItemStack s1 = this.menu.getBlockEntity().getItems().getStackInSlot(1);
        ItemStack s2 = this.menu.getBlockEntity().getItems().getStackInSlot(2);
        ItemStack s3 = this.menu.getBlockEntity().getItems().getStackInSlot(3);
        ItemStack s4 = this.menu.getBlockEntity().getItems().getStackInSlot(4);

        if (!s0.isEmpty() && s0.is(ModItems.BOY_SHIELDING.get())) {
            graphics.blit(TEXTURE, x + 27, y + 87, 176, 16, 21, 22);
        }
        if (!s1.isEmpty() && s1.is(ModItems.BOY_TARGET.get())) {
            graphics.blit(TEXTURE, x + 27, y + 89, 176, 38, 21, 18);
        }
        if (!s2.isEmpty() && s2.is(ModItems.BOY_BULLET.get())) {
            graphics.blit(TEXTURE, x + 74, y + 94, 176, 57, 19, 8);
        }
        if (!s3.isEmpty() && s3.is(ModItems.BOY_PROPELLANT.get())) {
            graphics.blit(TEXTURE, x + 92, y + 95, 176, 66, 12, 6);
        }
        if (!s4.isEmpty() && s4.is(ModItems.BOY_IGNITER.get())) {
            graphics.blit(TEXTURE, x + 107, y + 91, 176, 75, 16, 14);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
