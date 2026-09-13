package com.hbm.client.screen;

import com.hbm.blockentity.machine.GasCentBlockEntity;
import com.hbm.inventory.menu.GasCentMenu;
import com.hbm.inventory.recipes.GasCentrifugeRecipes.PseudoFluidType;
import com.hbm.lib.RefStrings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class GasCentScreen extends AbstractContainerScreen<GasCentMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_centrifuge_gas.png");

    public GasCentScreen(GasCentMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 206;
        this.imageHeight = 204;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 1000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int maxEnergy = Math.max(this.menu.getMaxEnergy(), 1);
        int energyH = this.menu.getEnergy() * 52 / maxEnergy;
        if (this.menu.getEnergy() > 0 && energyH > 0) {
            graphics.blit(TEXTURE, x + 182, y + 69 - energyH, 206, 52 - energyH, 16, energyH);
        }

        int progressW = this.menu.getProgress() * 36 / GasCentBlockEntity.PROCESSING_SPEED;
        if (progressW > 0) {
            graphics.blit(TEXTURE, x + 70, y + 35, 206, 52, progressW, 13);
        }

        drawTank(graphics, x + 16, y + 16, 6, 52, this.menu.getInputFill());
        drawTank(graphics, x + 32, y + 16, 6, 52, this.menu.getInputFill());
        drawTank(graphics, x + 138, y + 16, 6, 52, this.menu.getOutputFill());
        drawTank(graphics, x + 154, y + 16, 6, 52, this.menu.getOutputFill());
    }

    private void drawTank(GuiGraphics graphics, int x, int y, int width, int height, int fill) {
        int h = Math.min(height, fill * height / GasCentBlockEntity.PSEUDO_CAPACITY);
        if (h <= 0) {
            return;
        }
        FluidStack tank = this.menu.getTankFluid();
        int color = 0xFFD1CEBE;
        if (!tank.isEmpty()) {
            int tint = IClientFluidTypeExtensions.of(tank.getFluid()).getTintColor(tank);
            color = tint == 0 ? color : tint | 0xFF000000;
        }
        graphics.fill(x, y + height - h, x + width, y + height, color);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(182, 17, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(
                    this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
        if (isHovering(15, 15, 24, 55, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, tankTooltip(this.menu.getInputType(), this.menu.getInputFill()),
                    mouseX, mouseY);
        }
        if (isHovering(137, 15, 25, 55, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, tankTooltip(this.menu.getOutputType(), this.menu.getOutputFill()),
                    mouseX, mouseY);
        }
        if (isHovering(-12, 16, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("desc.gui.gasCent.enrichment")), mouseX, mouseY);
        }
        if (isHovering(-12, 32, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("desc.gui.gasCent.output")), mouseX, mouseY);
        }
    }

    private static List<Component> tankTooltip(PseudoFluidType type, int fill) {
        List<Component> lines = new ArrayList<>();
        Component name = type.displayName();
        if (type.highSpeed()) {
            name = name.copy().withStyle(ChatFormatting.DARK_RED);
        }
        lines.add(name);
        lines.add(Component.literal(fill + " / " + GasCentBlockEntity.PSEUDO_CAPACITY + " mB"));
        return lines;
    }
}
