package com.hbm.client.screen;

import com.hbm.blockentity.bomb.NukeCustomBlockEntity;
import com.hbm.blocks.bomb.NukeCustomYield;
import com.hbm.inventory.menu.NukeCustomMenu;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class NukeCustomScreen extends AbstractContainerScreen<NukeCustomMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/gun_bomb_schematic.png");

    public NukeCustomScreen(NukeCustomMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        NukeCustomBlockEntity be = this.menu.getBlockEntity();
        be.recalculate();

        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Light every armed stage socket (legacy only showed the dominant one — empty dark
        // bays looked broken when lower stages were also loaded).
        if (be.getTnt() > 0.0F) {
            graphics.blit(TEXTURE, this.leftPos + 16, this.topPos + 89, 176, 0, 18, 18);
        }
        if (be.getNuke() > 0.0F) {
            graphics.blit(TEXTURE, this.leftPos + 34, this.topPos + 89, 176, 18, 18, 18);
        }
        if (be.getHydro() > 0.0F) {
            graphics.blit(TEXTURE, this.leftPos + 52, this.topPos + 89, 176, 36, 18, 18);
        }
        if (be.getAmat() > 0.0F) {
            graphics.blit(TEXTURE, this.leftPos + 70, this.topPos + 89, 176, 54, 18, 18);
        }
        if (be.getDirty() > 0.0F
                && be.getNuke() > 0.0F
                && be.getAmat() == 0.0F
                && be.getSchrab() == 0.0F
                && be.getEuph() == 0.0F) {
            graphics.blit(TEXTURE, this.leftPos + 88, this.topPos + 89, 176, 72, 18, 18);
        }
        if (be.getSchrab() > 0.0F) {
            graphics.blit(TEXTURE, this.leftPos + 106, this.topPos + 89, 176, 90, 18, 18);
        }
        if (be.getEuph() > 0.0F) {
            graphics.blit(TEXTURE, this.leftPos + 142, this.topPos + 89, 176, 108, 18, 18);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        NukeCustomBlockEntity be = this.menu.getBlockEntity();
        be.recalculate();

        List<Component> tip = stageTooltip(be, mouseX, mouseY);
        if (tip != null) {
            graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    private List<Component> stageTooltip(NukeCustomBlockEntity be, int mouseX, int mouseY) {
        if (hover(16, 89, mouseX, mouseY)) {
            return List.of(
                    Component.literal("Conventional Explosives (Level " + fmt(be.getTnt()) + "/"
                            + fmt(Math.min(be.getTnt(), NukeCustomYield.MAX_TNT)) + ")").withStyle(ChatFormatting.YELLOW),
                    Component.literal("Caps at " + NukeCustomYield.MAX_TNT),
                    Component.literal("N²-like above level 75"),
                    Component.literal("\"Goes boom\"").withStyle(ChatFormatting.ITALIC)
            );
        }
        if (hover(34, 89, mouseX, mouseY)) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("Nuclear (Level " + fmt(be.getNukeRaw()) + "/" + fmt(be.getNukeAdj()) + ")")
                    .withStyle(ChatFormatting.YELLOW));
            tip.add(Component.literal("Requires TNT level 16"));
            if (be.getTnt() < 16.0F && be.getNukeRaw() > 0.0F) {
                tip.add(Component.literal("Inactive — need more conventional explosives")
                        .withStyle(ChatFormatting.RED));
            }
            tip.add(Component.literal("Caps at " + NukeCustomYield.MAX_NUKE));
            tip.add(Component.literal("Has fallout"));
            tip.add(Component.literal("\"Now I am become death, destroyer of worlds.\"")
                    .withStyle(ChatFormatting.ITALIC));
            return tip;
        }
        if (hover(52, 89, mouseX, mouseY)) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("Thermonuclear (Level " + fmt(be.getHydroRaw()) + "/" + fmt(be.getHydroAdj()) + ")")
                    .withStyle(ChatFormatting.YELLOW));
            tip.add(Component.literal("Requires nuclear level 100"));
            if (be.getNuke() < 100.0F && be.getHydroRaw() > 0.0F) {
                tip.add(Component.literal("Inactive — need nuclear level 100")
                        .withStyle(ChatFormatting.RED));
            }
            tip.add(Component.literal("Caps at " + NukeCustomYield.MAX_HYDRO));
            tip.add(Component.literal("Reduces added fallout by salted stage by 75%"));
            tip.add(Component.literal("\"And for my next trick, I'll make\"")
                    .withStyle(ChatFormatting.ITALIC));
            tip.add(Component.literal("the island of Elugelab disappear!\"")
                    .withStyle(ChatFormatting.ITALIC));
            return tip;
        }
        if (hover(70, 89, mouseX, mouseY)) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("Antimatter (Level " + fmt(be.getAmatRaw()) + "/" + fmt(be.getAmatAdj()) + ")")
                    .withStyle(ChatFormatting.YELLOW));
            tip.add(Component.literal("Requires nuclear level 50"));
            if (be.getNuke() < 50.0F && be.getAmatRaw() > 0.0F) {
                tip.add(Component.literal("Inactive — need nuclear level 50")
                        .withStyle(ChatFormatting.RED));
            }
            tip.add(Component.literal("Caps at " + NukeCustomYield.MAX_AMAT));
            tip.add(Component.literal("\"Antimatter, Balefire, whatever.\"")
                    .withStyle(ChatFormatting.ITALIC));
            return tip;
        }
        if (hover(88, 89, mouseX, mouseY)) {
            return List.of(
                    Component.literal("Salted (Level " + fmt(be.getDirty()) + "/"
                            + fmt(Math.min(be.getDirty(), 100.0F)) + ")").withStyle(ChatFormatting.YELLOW),
                    Component.literal("Extends fallout of nuclear and"),
                    Component.literal("thermonuclear stages"),
                    Component.literal("Caps at 100"),
                    Component.literal("\"Not to be confused with tablesalt.\"")
                            .withStyle(ChatFormatting.ITALIC)
            );
        }
        if (hover(106, 89, mouseX, mouseY)) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal("Schrabidium (Level " + fmt(be.getSchrabRaw()) + "/" + fmt(be.getSchrabAdj()) + ")")
                    .withStyle(ChatFormatting.YELLOW));
            tip.add(Component.literal("Requires nuclear level 50"));
            if (be.getNuke() < 50.0F && be.getSchrabRaw() > 0.0F) {
                tip.add(Component.literal("Inactive — need nuclear level 50")
                        .withStyle(ChatFormatting.RED));
            }
            tip.add(Component.literal("Caps at " + NukeCustomYield.MAX_SCHRAB));
            tip.add(Component.literal("\"For the hundredth time,\"")
                    .withStyle(ChatFormatting.ITALIC));
            tip.add(Component.literal("you can't bypass these caps!\"")
                    .withStyle(ChatFormatting.ITALIC));
            return tip;
        }
        if (hover(142, 89, mouseX, mouseY)) {
            return List.of(
                    Component.literal("Ice cream (Level unknown)").withStyle(ChatFormatting.YELLOW),
                    Component.literal("\"Probably not ice cream but the label came off.\"")
                            .withStyle(ChatFormatting.ITALIC)
            );
        }
        return null;
    }

    private boolean hover(int x, int y, int mouseX, int mouseY) {
        return mouseX >= this.leftPos + x && mouseX < this.leftPos + x + 18
                && mouseY >= this.topPos + y && mouseY < this.topPos + y + 18;
    }

    private static String fmt(float v) {
        if (v == (int) v) {
            return Integer.toString((int) v);
        }
        return String.format("%.1f", v);
    }
}
