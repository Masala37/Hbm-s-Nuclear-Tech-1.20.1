package com.hbm.client.screen;

import com.hbm.blockentity.machine.AssemblyMachineBlockEntity;
import com.hbm.blockentity.machine.PurexBlockEntity;
import com.hbm.energy.ItemChargeStorage;
import com.hbm.inventory.menu.PurexMenu;
import com.hbm.inventory.recipes.GenericMachineRecipe;
import com.hbm.inventory.recipes.GenericRecipeMatch;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class PurexScreen extends AbstractContainerScreen<PurexMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_purex.png");

    public PurexScreen(PurexMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 256;
        this.titleLabelX = 70;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = 70 - this.font.width(this.title) / 2;
        graphics.drawString(this.font, this.title, titleX, 6, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int maxEnergy = this.menu.getMaxEnergy();
        int energy = this.menu.getEnergy();
        if (maxEnergy > 0 && energy > 0) {
            int h = energy * 61 / maxEnergy;
            graphics.blit(TEXTURE, x + 152, y + 79 - h, 176, 61 - h, 16, h);
        }

        double progress = this.menu.getProgress();
        if (progress > 0.0D) {
            int w = (int) Math.ceil(70 * progress);
            graphics.blit(TEXTURE, x + 62, y + 126, 176, 61, w, 16);
        }

        GenericMachineRecipe recipe = this.menu.getBlockEntity().getRecipe();
        if (this.menu.didProcess()) {
            graphics.blit(TEXTURE, x + 51, y + 121, 195, 0, 3, 6);
            graphics.blit(TEXTURE, x + 56, y + 121, 195, 0, 3, 6);
        } else if (recipe != null) {
            graphics.blit(TEXTURE, x + 51, y + 121, 192, 0, 3, 6);
            if (this.menu.getEnergy() >= recipe.power()) {
                graphics.blit(TEXTURE, x + 56, y + 121, 192, 0, 3, 6);
            }
        }

        ItemStack icon = recipe != null ? GenericRecipeMatch.icon(recipe) : new ItemStack(ModItems.TEMPLATE_FOLDER.get());
        if (!icon.isEmpty()) {
            graphics.renderItem(icon, x + 8, y + 126);
        }

        if (recipe != null) {
            for (int i = 0; i < recipe.inputItem().size() && i < PurexBlockEntity.INPUT_SLOTS.length; i++) {
                Slot slot = this.menu.getSlot(4 + i);
                if (!slot.hasItem()) {
                    ItemStack ghost = GenericRecipeMatch.result(recipe.inputItem().get(i));
                    if (!ghost.isEmpty()) {
                        graphics.renderItem(ghost, x + slot.x, y + slot.y);
                    }
                }
            }
            graphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
            for (int i = 0; i < recipe.inputItem().size() && i < PurexBlockEntity.INPUT_SLOTS.length; i++) {
                Slot slot = this.menu.getSlot(4 + i);
                if (!slot.hasItem()) {
                    graphics.blit(TEXTURE, x + slot.x, y + slot.y, slot.x, slot.y, 16, 16);
                }
            }
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        for (int i = 0; i < 3; i++) {
            drawFluid(graphics, x + 8 + i * 18, y + 18, 16, 52, this.menu.getInputFluid(i));
        }
        drawFluid(graphics, x + 116, y + 36, 16, 52, this.menu.getOutputFluid());
    }

    private void drawFluid(GuiGraphics graphics, int x, int y, int width, int height, FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return;
        }
        int h = Math.min(height, fluid.getAmount() * height / PurexBlockEntity.TANK_CAPACITY);
        if (h <= 0) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        int color = extensions.getTintColor(fluid);
        graphics.fill(x, y + height - h, x + width, y + height, color == 0 ? 0xFF3F76E4 : color | 0xFF000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(152, 18, 16, 61, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(ItemChargeStorage.shortNumber(this.menu.getEnergy())
                            + " / " + ItemChargeStorage.shortNumber(this.menu.getMaxEnergy()) + " FE"),
                    mouseX, mouseY);
        }
        for (int i = 0; i < 3; i++) {
            if (isHovering(8 + i * 18, 18, 16, 52, mouseX, mouseY)) {
                tooltipTank(graphics, this.menu.getInputFluid(i), mouseX, mouseY);
            }
        }
        if (isHovering(116, 36, 16, 52, mouseX, mouseY)) {
            tooltipTank(graphics, this.menu.getOutputFluid(), mouseX, mouseY);
        }
        if (isHovering(7, 125, 18, 18, mouseX, mouseY)) {
            GenericMachineRecipe recipe = this.menu.getBlockEntity().getRecipe();
            if (recipe != null) {
                graphics.renderComponentTooltip(this.font, GenericRecipeMatch.tooltip(recipe), mouseX, mouseY);
            } else {
                graphics.renderTooltip(this.font,
                        Component.translatable("gui.recipe.setRecipe").withStyle(ChatFormatting.YELLOW),
                        mouseX, mouseY);
            }
        }
    }

    private void tooltipTank(GuiGraphics graphics, FluidStack fluid, int mouseX, int mouseY) {
        if (fluid == null || fluid.isEmpty()) {
            graphics.renderTooltip(this.font,
                    Component.literal("Empty: 0 / " + PurexBlockEntity.TANK_CAPACITY + " mB"),
                    mouseX, mouseY);
            return;
        }
        graphics.renderTooltip(this.font,
                Component.literal(fluid.getDisplayName().getString() + ": " + fluid.getAmount() + " / "
                        + PurexBlockEntity.TANK_CAPACITY + " mB"),
                mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(7, 125, 18, 18, (int) mouseX, (int) mouseY)) {
            PurexBlockEntity be = this.menu.getBlockEntity();
            this.minecraft.setScreen(new RecipeSelectorScreen(
                    be.getBlockPos(),
                    0,
                    be.getRecipeName(),
                    AssemblyMachineBlockEntity.grabPool(be.getItems().getStackInSlot(PurexBlockEntity.SLOT_BLUEPRINT)),
                    this,
                    RecipeSelectorScreen.PUREX));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
