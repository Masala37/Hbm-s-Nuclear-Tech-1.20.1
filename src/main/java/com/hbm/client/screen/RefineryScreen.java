package com.hbm.client.screen;

import com.hbm.inventory.menu.RefineryMenu;
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

public class RefineryScreen extends AbstractContainerScreen<RefineryMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_refinery.png");

    public RefineryScreen(RefineryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 210;
        this.imageHeight = 231;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = this.imageWidth / 2 - 17;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, this.imageWidth, this.imageHeight, 0, 0, this.imageWidth, this.imageHeight, 350, 256);
        int maxEnergy = Math.max(this.menu.getMaxEnergy(), 1);
        int energyH = this.menu.getEnergy() * 50 / maxEnergy;
        if (energyH > 0) {
            graphics.blit(TEXTURE, x + 186, y + 69 - energyH, 16, energyH, 210, 52 - energyH, 16, energyH, 350, 256);
        }
        var be = this.menu.getBlockEntity();
        drawTank(graphics, x + 33, y + 29, 16, 101, be.getInputTank().getFluid(),
                be.getInputTank().getFluidAmount(), be.getInputTank().getCapacity());
        drawTank(graphics, x + 86, y + 43, 16, 52, be.getHeavy().getFluid(),
                be.getHeavy().getFluidAmount(), be.getHeavy().getCapacity());
        drawTank(graphics, x + 106, y + 43, 16, 52, be.getNaphtha().getFluid(),
                be.getNaphtha().getFluidAmount(), be.getNaphtha().getCapacity());
        drawTank(graphics, x + 126, y + 43, 16, 52, be.getLight().getFluid(),
                be.getLight().getFluidAmount(), be.getLight().getCapacity());
        drawTank(graphics, x + 146, y + 43, 16, 52, be.getPetroleum().getFluid(),
                be.getPetroleum().getFluidAmount(), be.getPetroleum().getCapacity());
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
        var be = this.menu.getBlockEntity();
        if (isHovering(186, 18, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(
                    this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        } else if (isHovering(30, 27, 21, 104, mouseX, mouseY)) {
            tooltipTank(graphics, be.getInputTank().getFluid(), be.getInputTank().getFluidAmount(),
                    be.getInputTank().getCapacity(), mouseX, mouseY);
        } else if (isHovering(86, 42, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, be.getHeavy().getFluid(), be.getHeavy().getFluidAmount(),
                    be.getHeavy().getCapacity(), mouseX, mouseY);
        } else if (isHovering(106, 42, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, be.getNaphtha().getFluid(), be.getNaphtha().getFluidAmount(),
                    be.getNaphtha().getCapacity(), mouseX, mouseY);
        } else if (isHovering(126, 42, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, be.getLight().getFluid(), be.getLight().getFluidAmount(),
                    be.getLight().getCapacity(), mouseX, mouseY);
        } else if (isHovering(146, 42, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, be.getPetroleum().getFluid(), be.getPetroleum().getFluidAmount(),
                    be.getPetroleum().getCapacity(), mouseX, mouseY);
        }
    }

    private void tooltipTank(GuiGraphics graphics, FluidStack fluid, int amount, int cap, int mouseX, int mouseY) {
        String name = fluid.isEmpty() ? "Empty" : fluid.getDisplayName().getString();
        graphics.renderTooltip(this.font, Component.literal(name + ": " + amount + " / " + cap + " mB"),
                mouseX, mouseY);
    }
}
