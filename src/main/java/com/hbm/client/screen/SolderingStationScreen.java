package com.hbm.client.screen;

import com.hbm.inventory.menu.SolderingStationMenu;
import com.hbm.lib.RefStrings;
import com.hbm.network.ModMessages;
import com.hbm.network.SolderingControlPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class SolderingStationScreen extends AbstractContainerScreen<SolderingStationMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_soldering_station.png");

    public SolderingStationScreen(SolderingStationMenu menu, Inventory inv, Component title) {
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
        if (this.menu.isCollisionPrevention()) {
            graphics.blit(TEXTURE, x + 5, y + 66, 192, 14, 10, 10);
        }
        int maxEnergy = Math.max(this.menu.getMaxEnergy(), 1);
        int p = this.menu.getEnergy() * 52 / maxEnergy;
        if (p > 0) {
            graphics.blit(TEXTURE, x + 152, y + 70 - p, 176, 52 - p, 16, p);
        }
        int i = this.menu.getProgress() * 33 / this.menu.getProcessTime();
        if (i > 0) {
            graphics.blit(TEXTURE, x + 72, y + 28, 192, 0, i, 14);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(5, 66, 10, 10, (int) mouseX, (int) mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ModMessages.CHANNEL.sendToServer(new SolderingControlPacket(this.menu.getBlockEntity().getBlockPos()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        } else if (isHovering(5, 66, 10, 10, mouseX, mouseY)) {
            boolean on = this.menu.isCollisionPrevention();
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.literal("Recipe Collision Prevention: "
                            + (on ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF")),
                    Component.literal("Prevents no-fluid recipes from being processed"),
                    Component.literal("when fluid is present.")
            ), mouseX, mouseY);
        }
    }
}
