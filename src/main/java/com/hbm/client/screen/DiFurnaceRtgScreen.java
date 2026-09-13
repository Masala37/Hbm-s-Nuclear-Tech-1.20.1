package com.hbm.client.screen;

import com.hbm.blockentity.machine.DiFurnaceRtgBlockEntity;
import com.hbm.inventory.menu.DiFurnaceRtgMenu;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiFurnaceRtgScreen extends AbstractContainerScreen<DiFurnaceRtgMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_rtg_difurnace.png");
    private static final ResourceLocation UTILITY =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_utility.png");

    public DiFurnaceRtgScreen(DiFurnaceRtgMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.getHeat() >= DiFurnaceRtgBlockEntity.MIN_HEAT) {
            graphics.blit(TEXTURE, x + 58, y + 36, 176, 31, 18, 16);
        }
        int p = this.menu.getProgress() * 24 / DiFurnaceRtgBlockEntity.TIME_REQUIRED;
        graphics.blit(TEXTURE, x + 101, y + 35, 176, 14, p + 1, 17);
        graphics.blit(UTILITY, x - 15, y + 36, 8, 0, 16, 16);
        graphics.blit(UTILITY, x - 15, y + 52, 24, 0, 16, 16);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(-15, 36, 16, 16, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("desc.gui.rtg.pellets"));
            for (ItemRTGPellet pellet : ItemRTGPellet.PELLET_LIST) {
                lines.add(Component.translatable("desc.gui.rtg.pelletHeat",
                        Component.translatable(pellet.getDescriptionId()),
                        pellet.getHeat()));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        } else if (isHovering(-15, 52, 16, 16, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            for (String line : Component.translatable("desc.gui.rtgBFurnace.desc").getString().split("\n")) {
                lines.add(Component.literal(line));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        } else if (isHovering(58, 36, 18, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.translatable("desc.gui.rtg.heat", this.menu.getHeat()),
                    mouseX, mouseY);
        } else if (this.menu.getCarried().isEmpty()) {
            for (int i = 0; i < 2; i++) {
                Slot slot = this.menu.getSlot(i);
                if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                    int face = i == 0 ? this.menu.getSideUpper() : this.menu.getSideLower();
                    String dir = Direction.from3DDataValue(face).getSerializedName().toUpperCase(Locale.ROOT);
                    int oy = slot.hasItem() ? 15 : 0;
                    graphics.renderTooltip(this.font,
                            Component.translatable("gui.diFurnace.acceptsFrom", dir),
                            mouseX, mouseY - oy);
                    break;
                }
            }
        }
    }
}
