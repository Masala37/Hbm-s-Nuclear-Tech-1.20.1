package com.hbm.client.screen;

import com.hbm.blockentity.machine.SilexBlockEntity;
import com.hbm.inventory.menu.SilexMenu;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.lib.RefStrings;
import com.hbm.network.ModMessages;
import com.hbm.network.SilexControlPacket;
import com.hbm.registry.ModFluids;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class SilexScreen extends AbstractContainerScreen<SilexMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_silex.png");

    public SilexScreen(SilexMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 8;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title,
                (this.imageWidth / 2 - this.font.width(this.title) / 2) - 54, 8, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
        EnumWavelengths mode = this.menu.getBlockEntity().getMode();
        if (mode != EnumWavelengths.NULL) {
            Component name = Component.translatable(mode.nameKey).withStyle(mode.textColor);
            graphics.drawString(this.font, name,
                    100 + (32 - this.font.width(name) / 2), 16, 0xFFFFFF, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        SilexBlockEntity silex = this.menu.getBlockEntity();
        EnumWavelengths mode = silex.getMode();
        if (mode != EnumWavelengths.NULL) {
            int color = mode == EnumWavelengths.VISIBLE
                    ? Mth.hsvToRgb((silex.getLevel() == null ? 0 : silex.getLevel().getGameTime() % 50) / 50.0F, 0.5F, 1.0F)
                    : mode.guiColor;
            graphics.fill(x + 81, y + 53, x + 165, y + 55, color | 0xFF000000);
        }
        if (this.menu.getTankFill() > 0) {
            boolean valid = silex.getTankType() == ModFluids.PEROXIDE.source.get()
                    || SILEXRecipes.isConversionFluid(silex.getTankType())
                    || SILEXRecipes.getOutput(silex.getTankType()) != null;
            graphics.blit(TEXTURE, x + 7, y + 41, 176, valid ? 118 : 109, 54, 9);
        }
        int p = silex.getProgressScaled(69);
        if (p > 0) {
            graphics.blit(TEXTURE, x + 45, y + 82, 176, 0, p, 43);
        }
        int f = silex.getFillScaled(52);
        if (f > 0) {
            graphics.blit(TEXTURE, x + 26, y + 124 - f, 176, 109 - f, 16, f);
        }
        int i = silex.getFluidScaled(52);
        if (i > 0) {
            int v = silex.getTankType() == ModFluids.PEROXIDE.source.get() ? 43 : 50;
            graphics.blit(TEXTURE, x + 8, y + 42, 176, v, i, 7);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(10, 92, 12, 12, (int) mouseX, (int) mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ModMessages.CHANNEL.sendToServer(new SilexControlPacket(this.menu.getBlockEntity().getBlockPos()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        SilexBlockEntity silex = this.menu.getBlockEntity();
        if (isHovering(8, 42, 52, 7, mouseX, mouseY)) {
            FluidStack tank = silex.getTank().getFluid();
            String name = tank.isEmpty()
                    ? new FluidStack(silex.getTankType(), 1).getDisplayName().getString()
                    : tank.getDisplayName().getString();
            graphics.renderTooltip(this.font, Component.literal(
                    name + ": " + this.menu.getTankFill() + " / " + SilexBlockEntity.TANK_CAPACITY + " mB"),
                    mouseX, mouseY);
        } else if (isHovering(27, 72, 16, 52, mouseX, mouseY) && this.menu.getCurrentFill() > 0) {
            ItemStack current = silex.currentStack();
            String name = current.isEmpty() ? silex.getCurrentKey() : current.getHoverName().getString();
            graphics.renderTooltip(this.font, Component.literal(
                    this.menu.getCurrentFill() + "/" + SilexBlockEntity.MAX_FILL + "mB " + name),
                    mouseX, mouseY);
        } else if (isHovering(10, 92, 10, 10, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Void contents"), mouseX, mouseY);
        }
    }
}
