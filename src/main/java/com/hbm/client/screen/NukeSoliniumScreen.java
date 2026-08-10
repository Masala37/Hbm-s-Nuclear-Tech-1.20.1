package com.hbm.client.screen;

import com.hbm.inventory.menu.NukeSoliniumMenu;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class NukeSoliniumScreen extends AbstractContainerScreen<NukeSoliniumMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/solinium_schematic.png");

    public NukeSoliniumScreen(NukeSoliniumMenu menu, Inventory inv, Component title) {
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

        ItemStack[] s = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            s[i] = this.menu.getBlockEntity().getItems().getStackInSlot(i);
        }

        if (!s[0].isEmpty() && s[0].is(ModItems.SOLINIUM_IGNITER.get())) {
            graphics.blit(TEXTURE, x + 24, y + 84, 0, 222, 22, 14);
        }
        if (!s[1].isEmpty() && s[1].is(ModItems.SOLINIUM_PROPELLANT.get())) {
            graphics.blit(TEXTURE, x + 46, y + 84, 22, 222, 18, 14);
        }
        if (!s[2].isEmpty() && s[2].is(ModItems.SOLINIUM_PROPELLANT.get())) {
            graphics.blit(TEXTURE, x + 76, y + 84, 52, 222, 18, 14);
        }
        if (!s[3].isEmpty() && s[3].is(ModItems.SOLINIUM_IGNITER.get())) {
            graphics.blit(TEXTURE, x + 94, y + 84, 70, 222, 22, 14);
        }
        if (!s[4].isEmpty() && s[4].is(ModItems.SOLINIUM_CORE.get())) {
            graphics.blit(TEXTURE, x + 64, y + 84, 40, 222, 12, 28);
        }
        if (!s[5].isEmpty() && s[5].is(ModItems.SOLINIUM_IGNITER.get())) {
            graphics.blit(TEXTURE, x + 24, y + 98, 0, 236, 22, 14);
        }
        if (!s[6].isEmpty() && s[6].is(ModItems.SOLINIUM_PROPELLANT.get())) {
            graphics.blit(TEXTURE, x + 46, y + 98, 22, 236, 18, 14);
        }
        if (!s[7].isEmpty() && s[7].is(ModItems.SOLINIUM_PROPELLANT.get())) {
            graphics.blit(TEXTURE, x + 76, y + 98, 52, 236, 18, 14);
        }
        if (!s[8].isEmpty() && s[8].is(ModItems.SOLINIUM_IGNITER.get())) {
            graphics.blit(TEXTURE, x + 94, y + 98, 70, 236, 22, 14);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
