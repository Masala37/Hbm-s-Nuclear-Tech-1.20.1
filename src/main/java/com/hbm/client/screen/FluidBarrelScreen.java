package com.hbm.client.screen;

import com.hbm.inventory.menu.FluidBarrelMenu;
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

public class FluidBarrelScreen extends AbstractContainerScreen<FluidBarrelMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/storage/gui_barrel.png");

    public FluidBarrelScreen(FluidBarrelMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int capacity = this.menu.getFluidCapacity();
        int amount = this.menu.getFluidAmount();
        if (capacity > 0 && amount > 0) {
            int h = amount * 52 / capacity;
            FluidStack fluid = this.menu.getBlockEntity().getTank().getFluid();
            if (!fluid.isEmpty() && h > 0) {
                drawFluid(graphics, x + 71, y + 69 - h, 34, h, fluid);
            } else if (h > 0) {
                graphics.fill(x + 71, y + 69 - h, x + 71 + 34, y + 69, 0xFF3F76E4);
            }
        }
    }

    private void drawFluid(GuiGraphics graphics, int x, int y, int width, int height, FluidStack fluid) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation still = extensions.getStillTexture(fluid);
        int color = extensions.getTintColor(fluid);
        if (still == null) {
            graphics.fill(x, y, x + width, y + height, color);
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
        graphics.blit(x, y, 0, width, height, sprite);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(71, 17, 34, 52, mouseX, mouseY)) {
            FluidStack fluid = this.menu.getBlockEntity().getTank().getFluid();
            Component line;
            if (fluid.isEmpty() || this.menu.getFluidAmount() <= 0) {
                line = Component.literal("Empty: 0 / " + this.menu.getFluidCapacity() + " mB");
            } else {
                line = Component.literal(fluid.getDisplayName().getString() + ": "
                        + this.menu.getFluidAmount() + " / " + this.menu.getFluidCapacity() + " mB");
            }
            graphics.renderTooltip(this.font, line, mouseX, mouseY);
        }
    }
}
