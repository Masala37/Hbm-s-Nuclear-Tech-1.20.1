package com.hbm.client.screen;

import com.hbm.inventory.menu.CatalyticReformerMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class CatalyticReformerScreen extends AbstractContainerScreen<CatalyticReformerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_catalytic_reformer.png");

    public CatalyticReformerScreen(CatalyticReformerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 238;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 5;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title,
                this.imageWidth / 2 - this.font.width(this.title) / 2, this.titleLabelY, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int maxEnergy = Math.max(this.menu.getMaxEnergy(), 1);
        int energyH = Math.min(52, this.menu.getEnergy() * 52 / maxEnergy);
        if (energyH > 0) {
            graphics.blit(TEXTURE, x + 17, y + 70 - energyH, 176, 52 - energyH, 16, energyH);
        }
        var be = this.menu.getBlockEntity();
        drawTank(graphics, x + 35, y + 18, 16, 52, be.getOil().getFluid(),
                be.getOil().getFluidAmount(), be.getOil().getCapacity());
        drawTank(graphics, x + 107, y + 18, 16, 52, be.getOutput1().getFluid(),
                be.getOutput1().getFluidAmount(), be.getOutput1().getCapacity());
        drawTank(graphics, x + 125, y + 18, 16, 52, be.getOutput2().getFluid(),
                be.getOutput2().getFluidAmount(), be.getOutput2().getCapacity());
        drawTank(graphics, x + 143, y + 18, 16, 52, be.getOutput3().getFluid(),
                be.getOutput3().getFluidAmount(), be.getOutput3().getCapacity());
    }

    private void drawTank(GuiGraphics graphics, int x, int y, int width, int height, FluidStack fluid, int amount,
                          int cap) {
        if (cap <= 0 || amount <= 0) {
            return;
        }
        int h = amount * height / cap;
        if (fluid.isEmpty() || h <= 0) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation still = extensions.getStillTexture(fluid);
        int color = extensions.getTintColor(fluid);
        if (still == null) {
            graphics.fill(x, y + height - h, x + width, y + height, color);
            return;
        }
        TextureAtlasSprite sprite = this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        if (a <= 0f) {
            a = 1f;
        }
        graphics.setColor(r, g, b, a);
        graphics.blit(x, y + height - h, 0, width, h, sprite);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        var be = this.menu.getBlockEntity();
        if (isHovering(17, 18, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(
                    this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        } else if (isHovering(35, 18, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, be.getOil().getFluid(), be.getOil().getFluidAmount(),
                    be.getOil().getCapacity(), mouseX, mouseY);
        } else if (isHovering(107, 18, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, be.getOutput1().getFluid(), be.getOutput1().getFluidAmount(),
                    be.getOutput1().getCapacity(), mouseX, mouseY);
        } else if (isHovering(125, 18, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, be.getOutput2().getFluid(), be.getOutput2().getFluidAmount(),
                    be.getOutput2().getCapacity(), mouseX, mouseY);
        } else if (isHovering(143, 18, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, be.getOutput3().getFluid(), be.getOutput3().getFluidAmount(),
                    be.getOutput3().getCapacity(), mouseX, mouseY);
        }
    }

    private void tooltipTank(GuiGraphics graphics, FluidStack fluid, int amount, int cap, int mouseX, int mouseY) {
        String name = fluid.isEmpty() ? "Empty" : fluid.getDisplayName().getString();
        graphics.renderTooltip(this.font, Component.literal(name + ": " + amount + " / " + cap + " mB"),
                mouseX, mouseY);
    }
}
