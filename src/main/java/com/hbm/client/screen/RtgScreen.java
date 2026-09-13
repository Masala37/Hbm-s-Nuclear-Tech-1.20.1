package com.hbm.client.screen;

import com.hbm.blockentity.machine.RtgBlockEntity;
import com.hbm.inventory.menu.RtgMenu;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class RtgScreen extends AbstractContainerScreen<RtgMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_rtg.png");
    private static final ResourceLocation UTILITY =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_utility.png");

    public RtgScreen(RtgMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 188;
        this.titleLabelX = 13;
        this.titleLabelY = 7;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 10925486, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.hasHeat()) {
            int i = heatPixels();
            if (i > 0) {
                graphics.blit(TEXTURE, x + 124, y + 61 - i, 176, 10 + (51 - i), 16, i);
            }
        }
        if (this.menu.getEnergy() > 0) {
            int i = energyPixels();
            if (i > 0) {
                graphics.blit(TEXTURE, x + 146, y + 61 - i, 192, 10 + (51 - i), 16, i);
            }
        }
        graphics.blit(UTILITY, x - 12, y + 25, 8, 0, 16, 16);
    }

    private int heatPixels() {
        int max = this.menu.getHeatMax();
        if (max <= 0) {
            return 0;
        }
        return this.menu.getHeat() * 51 / max;
    }

    private int energyPixels() {
        int max = this.menu.getMaxEnergy();
        if (max <= 0) {
            return 0;
        }
        return (int) ((long) this.menu.getEnergy() * 51 / max);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(146, 9, 16, 51, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        } else if (isHovering(124, 9, 16, 51, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable("desc.gui.rtg.heat", this.menu.getHeat()),
                    mouseX, mouseY);
        } else if (isHovering(-12, 25, 16, 16, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("desc.gui.rtg.pellets"));
            for (ItemRTGPellet pellet : ItemRTGPellet.PELLET_LIST) {
                lines.add(Component.translatable("desc.gui.rtg.pelletPower",
                        Component.translatable(pellet.getDescriptionId()),
                        pellet.getHeat() * RtgBlockEntity.HEAT_TO_FE));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }
}
