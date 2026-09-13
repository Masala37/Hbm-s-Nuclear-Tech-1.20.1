package com.hbm.client.screen;

import com.hbm.blockentity.machine.MixerBlockEntity;
import com.hbm.inventory.menu.MixerMenu;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.MixerRecipes.MixerRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.network.MixerControlPacket;
import com.hbm.network.ModMessages;
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

public class MixerScreen extends AbstractContainerScreen<MixerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_mixer.png");

    public MixerScreen(MixerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 204;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title,
                this.imageWidth / 2 - this.font.width(this.title) / 2, this.titleLabelY, 0x404040, false);
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
            graphics.blit(TEXTURE, x + 23, y + 75 - p, 176, 52 - p, 16, p);
        }
        if (this.menu.getProcessTime() > 0 && this.menu.getProgress() > 0) {
            int j = this.menu.getProgress() * 53 / this.menu.getProcessTime();
            graphics.blit(TEXTURE, x + 62, y + 36, 192, 0, j, 44);
        }
        drawTank(graphics, x + 43, y + 23, 7, 52, this.menu.getBlockEntity().tankView(0),
                this.menu.getBlockEntity().getInput0().getFluidAmount(), MixerBlockEntity.INPUT_CAP);
        drawTank(graphics, x + 52, y + 23, 7, 52, this.menu.getBlockEntity().tankView(1),
                this.menu.getBlockEntity().getInput1().getFluidAmount(), MixerBlockEntity.INPUT_CAP);
        drawTank(graphics, x + 117, y + 23, 16, 52, this.menu.getBlockEntity().tankView(2),
                this.menu.getBlockEntity().getOutput().getFluidAmount(), MixerBlockEntity.OUTPUT_CAP);
    }

    private void drawTank(GuiGraphics graphics, int x, int y, int width, int height, FluidStack fluid, int amount,
                          int cap) {
        if (cap <= 0 || amount <= 0 || fluid.isEmpty()) {
            return;
        }
        int h = amount * height / cap;
        if (h <= 0) {
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(62, 22, 12, 12, (int) mouseX, (int) mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ModMessages.CHANNEL.sendToServer(new MixerControlPacket(this.menu.getBlockEntity().getBlockPos()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(23, 23, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal(
                    this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        } else if (isHovering(43, 23, 7, 52, mouseX, mouseY)) {
            tankTooltip(graphics, mouseX, mouseY, this.menu.getBlockEntity().tankView(0),
                    this.menu.getBlockEntity().getInput0().getFluidAmount(), MixerBlockEntity.INPUT_CAP);
        } else if (isHovering(52, 23, 7, 52, mouseX, mouseY)) {
            tankTooltip(graphics, mouseX, mouseY, this.menu.getBlockEntity().tankView(1),
                    this.menu.getBlockEntity().getInput1().getFluidAmount(), MixerBlockEntity.INPUT_CAP);
        } else if (isHovering(117, 23, 16, 52, mouseX, mouseY)) {
            tankTooltip(graphics, mouseX, mouseY, this.menu.getBlockEntity().tankView(2),
                    this.menu.getBlockEntity().getOutput().getFluidAmount(), MixerBlockEntity.OUTPUT_CAP);
        } else if (isHovering(62, 22, 12, 12, mouseX, mouseY) && this.menu.getRecipeCount() > 1) {
            MixerRecipe[] recs = MixerRecipes.getOutput(this.menu.getBlockEntity().getOutputType());
            if (recs != null && recs.length > 1) {
                int index = Math.floorMod(this.menu.getRecipeIndex(), recs.length);
                MixerRecipe recipe = recs[index];
                List<Component> lines = new ArrayList<>();
                lines.add(Component.literal("Current recipe (" + (index + 1) + "/" + recs.length + "):")
                        .withStyle(ChatFormatting.YELLOW));
                if (recipe.input1() != null) {
                    lines.add(Component.literal("-" + recipe.input1().fluid()));
                }
                if (recipe.input2() != null) {
                    lines.add(Component.literal("-" + recipe.input2().fluid()));
                }
                if (recipe.solid() != null) {
                    lines.add(Component.literal("-" + recipe.solid().searchName()));
                }
                lines.add(Component.literal("Click to change!").withStyle(ChatFormatting.RED));
                graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
            }
        }
    }

    private void tankTooltip(GuiGraphics graphics, int mouseX, int mouseY, FluidStack fluid, int amount, int cap) {
        String name = fluid.isEmpty() ? "Empty" : fluid.getDisplayName().getString();
        graphics.renderTooltip(this.font, Component.literal(name + ": " + amount + " / " + cap + " mB"),
                mouseX, mouseY);
    }
}
