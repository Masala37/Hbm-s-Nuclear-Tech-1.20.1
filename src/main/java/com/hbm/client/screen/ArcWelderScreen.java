package com.hbm.client.screen;

import com.hbm.inventory.menu.ArcWelderMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ArcWelderScreen extends AbstractContainerScreen<ArcWelderMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_arc_welder.png");

    public ArcWelderScreen(ArcWelderMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 204;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = this.imageWidth / 2 - this.font.width(this.title) / 2 - 18;
        graphics.drawString(this.font, this.title, titleX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        int maxEnergy = Math.max(this.menu.getMaxEnergy(), 1);
        int p = this.menu.getEnergy() * 52 / maxEnergy;
        if (p > 0) {
            graphics.blit(TEXTURE, x + 152, y + 70 - p, 176, 52 - p, 16, p);
        }
        int i = this.menu.getProgress() * 33 / this.menu.getProcessTime();
        if (i > 0) {
            graphics.blit(TEXTURE, x + 72, y + 37, 192, 0, i, 14);
        }
        if (this.menu.getEnergy() >= this.menu.getConsumption()) {
            graphics.blit(TEXTURE, x + 156, y + 4, 176, 52, 9, 12);
        }
        drawTank(graphics, x + 35, y + 63, 34, 16, this.menu.getBlockEntity().getTank().getFluid(),
                this.menu.getBlockEntity().getTank().getFluidAmount(),
                this.menu.getBlockEntity().getTank().getCapacity());
    }

    private void drawTank(GuiGraphics graphics, int x, int y, int width, int height, FluidStack fluid, int amount,
                          int cap) {
        if (cap <= 0 || amount <= 0 || fluid.isEmpty()) {
            return;
        }
        int w = amount * width / cap;
        if (w <= 0) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation still = extensions.getStillTexture(fluid);
        int color = extensions.getTintColor(fluid);
        if (still == null) {
            graphics.fill(x, y, x + w, y + height, color);
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
        graphics.blit(x, y, 0, w, height, sprite);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(152, 18, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(
                    this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        } else if (isHovering(35, 63, 34, 16, mouseX, mouseY)) {
            FluidStack fluid = this.menu.getBlockEntity().getTank().getFluid();
            String name = fluid.isEmpty() ? "Empty" : fluid.getDisplayName().getString();
            graphics.renderTooltip(this.font, Component.literal(name + ": "
                    + this.menu.getBlockEntity().getTank().getFluidAmount() + " / "
                    + this.menu.getBlockEntity().getTank().getCapacity() + " mB"), mouseX, mouseY);
        } else if (isHovering(78, 67, 8, 8, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.literal("Upgrades").withStyle(ChatFormatting.YELLOW),
                    Component.literal("Speed / power / overdrive upgrades are not ported.")
            ), mouseX, mouseY);
        }
    }
}
