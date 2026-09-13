package com.hbm.client.screen;

import com.hbm.inventory.menu.WoodBurnerMenu;
import com.hbm.lib.RefStrings;
import com.hbm.network.ModMessages;
import com.hbm.network.WoodBurnerControlPacket;
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

import java.util.ArrayList;
import java.util.List;

public class WoodBurnerScreen extends AbstractContainerScreen<WoodBurnerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/generators/gui_wood_burner_alt.png");

    public WoodBurnerScreen(WoodBurnerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelX = 70;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title,
                this.titleLabelX - this.font.width(this.title) / 2, this.titleLabelY, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isLiquidBurn()) {
            graphics.blit(TEXTURE, x + 16, y + 17, 176, 52, 60, 54);
            graphics.blit(TEXTURE, x + 79, y + 17, 176, 106, 36, 54);
        }

        if (this.menu.isOn()) {
            graphics.blit(TEXTURE, x + 53, y + 17, 196, 0, 16, 15);
        }

        int maxEnergy = Math.max(this.menu.getMaxEnergy(), 1);
        int p = this.menu.getEnergy() * 34 / maxEnergy;
        if (p > 0) {
            graphics.blit(TEXTURE, x + 143, y + 52 - p, 176, 52 - p, 16, p);
        }

        if (this.menu.getMaxBurnTime() > 0 && !this.menu.isLiquidBurn()) {
            int b = this.menu.getBurnTime() * 52 / this.menu.getMaxBurnTime();
            if (b > 0) {
                graphics.blit(TEXTURE, x + 17, y + 70 - b, 192, 52 - b, 4, b);
            }
        }

        if (this.menu.isLiquidBurn()) {
            int capacity = this.menu.getFluidCapacity();
            int amount = this.menu.getFluidAmount();
            if (capacity > 0 && amount > 0) {
                int h = amount * 52 / capacity;
                FluidStack fluid = this.menu.getBlockEntity().getTank().getFluid();
                if (!fluid.isEmpty() && h > 0) {
                    drawFluid(graphics, x + 80, y + 70 - h, 16, h, fluid);
                }
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

        if (isHovering(143, 18, 16, 34, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        } else if (this.menu.isLiquidBurn() && isHovering(80, 18, 16, 52, mouseX, mouseY)) {
            FluidStack fluid = this.menu.getBlockEntity().getTank().getFluid();
            List<Component> lines = new ArrayList<>(1);
            if (fluid.isEmpty() || this.menu.getFluidAmount() <= 0) {
                lines.add(Component.literal("Empty: 0 / " + this.menu.getFluidCapacity() + " mB"));
            } else {
                lines.add(Component.literal(fluid.getDisplayName().getString() + ": "
                        + this.menu.getFluidAmount() + " / " + this.menu.getFluidCapacity() + " mB"));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        } else if (!this.menu.isLiquidBurn() && isHovering(16, 17, 8, 54, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal((this.menu.getBurnTime() / 20) + "s"), mouseX, mouseY);
        } else if (isHovering(53, 17, 16, 15, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(this.menu.isOn() ? "ON" : "OFF")
                    .withStyle(this.menu.isOn() ? ChatFormatting.GREEN : ChatFormatting.RED), mouseX, mouseY);
        } else if (!this.menu.getSlot(0).hasItem() && isHovering(26, 18, 16, 16, mouseX, mouseY)
                && this.menu.getCarried().isEmpty()) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.literal("Burn time bonuses:").withStyle(ChatFormatting.GOLD),
                    Component.literal("- Logs: +300%").withStyle(ChatFormatting.YELLOW),
                    Component.literal("- Wood: +100%").withStyle(ChatFormatting.YELLOW)
            ), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(53, 17, 16, 15, (int) mouseX, (int) mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ModMessages.CHANNEL.sendToServer(new WoodBurnerControlPacket(
                    this.menu.getBlockEntity().getBlockPos(), WoodBurnerControlPacket.TOGGLE));
            return true;
        }
        if (isHovering(46, 37, 30, 14, (int) mouseX, (int) mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ModMessages.CHANNEL.sendToServer(new WoodBurnerControlPacket(
                    this.menu.getBlockEntity().getBlockPos(), WoodBurnerControlPacket.SWITCH_MODE));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
