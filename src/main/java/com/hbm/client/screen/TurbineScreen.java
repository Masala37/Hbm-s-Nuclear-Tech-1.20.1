package com.hbm.client.screen;

import com.hbm.inventory.menu.TurbineMenu;
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

public class TurbineScreen extends AbstractContainerScreen<TurbineMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_turbine.png");

    public TurbineScreen(TurbineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int maxEnergy = Math.max(this.menu.getMaxEnergy(), 1);
        int energyH = this.menu.getEnergy() * 34 / maxEnergy;
        if (energyH > 0) {
            graphics.blit(TEXTURE, x + 123, y + 69 - energyH, 176, 34 - energyH, 7, energyH);
        }
        drawTank(graphics, x + 62, y + 17, 16, 52, this.menu.getBlockEntity().getInput().getFluid(),
                this.menu.getInputAmount(), this.menu.getInputCap());
        drawTank(graphics, x + 134, y + 17, 16, 52, this.menu.getBlockEntity().getOutput().getFluid(),
                this.menu.getOutputAmount(), this.menu.getOutputCap());
    }

    private void drawTank(GuiGraphics graphics, int x, int y, int width, int height, FluidStack fluid, int amount, int cap) {
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
        if (isHovering(123, 35, 7, 34, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(
                    this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        } else if (isHovering(62, 17, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, this.menu.getBlockEntity().getInput().getFluid(),
                    this.menu.getInputAmount(), this.menu.getInputCap(), mouseX, mouseY);
        } else if (isHovering(134, 17, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, this.menu.getBlockEntity().getOutput().getFluid(),
                    this.menu.getOutputAmount(), this.menu.getOutputCap(), mouseX, mouseY);
        }
    }

    private void tooltipTank(GuiGraphics graphics, FluidStack fluid, int amount, int cap, int mouseX, int mouseY) {
        String name = fluid.isEmpty() ? "Empty" : fluid.getDisplayName().getString();
        graphics.renderTooltip(this.font, Component.literal(name + ": " + amount + " / " + cap + " mB"),
                mouseX, mouseY);
    }
}
