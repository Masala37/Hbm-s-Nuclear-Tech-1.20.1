package com.hbm.client.screen;

import com.hbm.blockentity.bomb.NukeTsarBlockEntity;
import com.hbm.inventory.menu.NukeTsarMenu;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class NukeTsarScreen extends AbstractContainerScreen<NukeTsarMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/tsar_bomba_schematic.png");
    private static final ResourceLocation TEXTURE_MIKE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/ivy_mike_schematic.png");

    public NukeTsarScreen(NukeTsarMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 233;
        this.inventoryLabelX = 48;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        NukeTsarBlockEntity be = this.menu.getBlockEntity();
        if (be.isFilled()) {
            graphics.blit(TEXTURE_MIKE, x + 18, y + 50, 176, 18, 16, 16);
        } else if (be.isReady()) {
            graphics.blit(TEXTURE_MIKE, x + 18, y + 50, 176, 0, 16, 16);
        }

        for (int i = 0; i < 4; i++) {
            ItemStack lens = be.getItems().getStackInSlot(i);
            if (!lens.isEmpty() && lens.is(ModItems.EXPLOSIVE_LENSES.get())) {
                switch (i) {
                    case 0 -> graphics.blit(TEXTURE_MIKE, x + 40, y + 36, 209, 1, 23, 23);
                    case 2 -> graphics.blit(TEXTURE_MIKE, x + 63, y + 36, 232, 1, 23, 23);
                    case 1 -> graphics.blit(TEXTURE_MIKE, x + 40, y + 59, 209, 24, 23, 23);
                    case 3 -> graphics.blit(TEXTURE_MIKE, x + 63, y + 59, 232, 24, 23, 23);
                    default -> {
                    }
                }
            }
        }

        ItemStack core = be.getItems().getStackInSlot(NukeTsarBlockEntity.SLOT_TSAR_CORE);
        if (!core.isEmpty() && core.is(ModItems.TSAR_CORE.get())) {
            graphics.blit(TEXTURE_MIKE, x + 91, y + 41, 176, 220, 80, 36);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
