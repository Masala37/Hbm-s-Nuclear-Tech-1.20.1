package com.hbm.client.screen;

import com.hbm.inventory.menu.NukeFleijaMenu;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class NukeFleijaScreen extends AbstractContainerScreen<NukeFleijaMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/fleija_schematic.png");

    public NukeFleijaScreen(NukeFleijaMenu menu, Inventory inv, Component title) {
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

        ItemStack[] s = new ItemStack[11];
        for (int i = 0; i < 11; i++) {
            s[i] = this.menu.getBlockEntity().getItems().getStackInSlot(i);
        }

        if (!s[0].isEmpty() && s[0].is(ModItems.FLEIJA_IGNITER.get())) {
            graphics.blit(TEXTURE, x + 7, y + 88, 176, 0, 30, 20);
        }
        if (!s[1].isEmpty() && s[1].is(ModItems.FLEIJA_IGNITER.get())) {
            graphics.blit(TEXTURE, x + 139, y + 88, 206, 0, 30, 20);
        }
        if (!s[2].isEmpty() && s[2].is(ModItems.FLEIJA_PROPELLANT.get())) {
            graphics.blit(TEXTURE, x + 57, y + 77, 176, 62, 18, 14);
        }
        if (!s[3].isEmpty() && s[3].is(ModItems.FLEIJA_PROPELLANT.get())) {
            graphics.blit(TEXTURE, x + 57, y + 91, 176, 76, 18, 14);
        }
        if (!s[4].isEmpty() && s[4].is(ModItems.FLEIJA_PROPELLANT.get())) {
            graphics.blit(TEXTURE, x + 57, y + 105, 176, 90, 18, 14);
        }
        if (!s[5].isEmpty() && s[5].is(ModItems.FLEIJA_CORE.get())) {
            graphics.blit(TEXTURE, x + 85, y + 77, 176, 20, 18, 15);
        }
        if (!s[6].isEmpty() && s[6].is(ModItems.FLEIJA_CORE.get())) {
            graphics.blit(TEXTURE, x + 103, y + 77, 194, 20, 18, 15);
        }
        if (!s[7].isEmpty() && s[7].is(ModItems.FLEIJA_CORE.get())) {
            graphics.blit(TEXTURE, x + 85, y + 92, 176, 35, 18, 12);
        }
        if (!s[8].isEmpty() && s[8].is(ModItems.FLEIJA_CORE.get())) {
            graphics.blit(TEXTURE, x + 103, y + 92, 194, 35, 18, 12);
        }
        if (!s[9].isEmpty() && s[9].is(ModItems.FLEIJA_CORE.get())) {
            graphics.blit(TEXTURE, x + 85, y + 107, 176, 47, 18, 15);
        }
        if (!s[10].isEmpty() && s[10].is(ModItems.FLEIJA_CORE.get())) {
            graphics.blit(TEXTURE, x + 103, y + 107, 194, 47, 18, 15);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
