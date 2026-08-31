package com.hbm.client.screen;

import com.hbm.blockentity.machine.CustomLauncherBlockEntity;
import com.hbm.client.render.missile.MissilePronter;
import com.hbm.handler.MissileStruct;
import com.hbm.inventory.menu.CompactLauncherMenu;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class CustomLauncherScreen<T extends CompactLauncherMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/gui_launch_table_small.png");
    private static final ResourceLocation GUI_UTIL =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_utility.png");

    public CustomLauncherScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
    }

    protected ResourceLocation texture() {
        return TEXTURE;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(texture(), x, y, 0, 0, this.imageWidth, this.imageHeight);

        int maxEnergy = this.menu.getMaxEnergy();
        if (maxEnergy > 0) {
            int w = this.menu.getEnergy() * 34 / maxEnergy;
            if (w > 0) {
                graphics.blit(texture(), x + 134, y + 113, 176, 96, w, 6);
            }
        }

        int maxSolid = this.menu.getMaxSolid();
        if (maxSolid > 0) {
            int h = this.menu.getSolid() * 52 / maxSolid;
            if (h > 0) {
                graphics.blit(texture(), x + 152, y + 88 - h, 176, 96 - h, 16, h);
            }
        }

        if (this.menu.missileValid()) {
            graphics.blit(texture(), x + 25, y + 35, 176, 26, 18, 18);
        }
        if (this.menu.hasDesignator()) {
            graphics.blit(texture(), x + 25, y + 71, 176, 26, 18, 18);
        }

        blitLamp(graphics, x + 121, y + 23, this.menu.liquidState());
        blitLamp(graphics, x + 139, y + 23, this.menu.oxidizerState());
        blitLamp(graphics, x + 157, y + 23, this.menu.solidState());

        CustomLauncherBlockEntity be = this.menu.getBlockEntity();
        drawTank(graphics, x + 116, y, be.getFuelTank().getFluid(), be.getFuelTank().getFluidAmount(),
                be.getTankCapacity());
        drawTank(graphics, x + 134, y, be.getOxidizerTank().getFluid(), be.getOxidizerTank().getFluidAmount(),
                be.getTankCapacity());

        renderMissilePreview(graphics);
        renderExtra(graphics, x, y);
        drawInfoPanel(graphics, x - 16, y + 36, 2);
        drawInfoPanel(graphics, x - 16, y + 52, 11);
    }

    private static void drawInfoPanel(GuiGraphics graphics, int x, int y, int type) {
        int u;
        int v;
        switch (type) {
            case 2 -> {
                u = 8;
                v = 0;
            }
            case 11 -> {
                u = 24;
                v = 32;
            }
            default -> {
                return;
            }
        }
        graphics.blit(GUI_UTIL, x, y, u, v, 16, 16);
    }

    protected void renderExtra(GuiGraphics graphics, int x, int y) {
    }

    private void blitLamp(GuiGraphics graphics, int x, int y, int state) {
        if (state == 1) {
            graphics.blit(texture(), x, y, 176, 0, 6, 8);
        } else if (state == 0) {
            graphics.blit(texture(), x, y, 182, 0, 6, 8);
        }
    }

    private void renderMissilePreview(GuiGraphics graphics) {
        ItemStack missile = this.menu.getBlockEntity().getItems()
                .getStackInSlot(CustomLauncherBlockEntity.SLOT_MISSILE);
        if (!(missile.getItem() instanceof ItemCustomMissile) || this.minecraft == null
                || !this.menu.missileValid()) {
            return;
        }
        MissileStruct struct = ItemCustomMissile.getStruct(missile);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.leftPos + 88, this.topPos + 115, 100);
        Lighting.setupForEntityInInventory();
        MultiBufferSource.BufferSource buffers = this.minecraft.renderBuffers().bufferSource();
        double height = Math.max(MissilePronter.getHeight(struct), 6.0D);
        double scale = 90.0D / height;
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        pose.translate(MissilePronter.getHeight(struct) / 2.0D * scale, 0.0D, 0.0D);
        pose.scale((float) scale, (float) scale, (float) scale);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        pose.scale(-1.0F, -1.0F, -1.0F);
        MissilePronter.prontMissile(pose, buffers, struct, LightTexture.FULL_BRIGHT);
        buffers.endBatch();
        Lighting.setupFor3DItems();
        pose.popPose();
    }

    private void drawTank(GuiGraphics graphics, int x, int top, FluidStack fluid, int amount, int capacity) {
        if (capacity <= 0 || amount <= 0) {
            return;
        }
        int h = amount * 34 / capacity;
        if (h <= 0) {
            return;
        }
        int y = top + 70 - h;
        if (fluid.isEmpty() || this.minecraft == null) {
            graphics.fill(x, y, x + 16, top + 70, 0xFF808080);
            return;
        }
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation still = ext.getStillTexture(fluid);
        int color = ext.getTintColor(fluid);
        TextureAtlasSprite sprite = this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        if (a <= 0f) {
            a = 1f;
        }
        graphics.setColor(r, g, b, a);
        graphics.blit(x, y, 0, 16, h, sprite);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(116, 36, 16, 34, mouseX, mouseY)) {
            FluidStack fluid = this.menu.getBlockEntity().getFuelTank().getFluid();
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(fluid.isEmpty() ? "Fuel" : fluid.getDisplayName().getString()));
            lines.add(Component.literal(fluid.getAmount() + " / " + this.menu.getBlockEntity().getTankCapacity() + " mB"));
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        } else if (isHovering(134, 36, 16, 34, mouseX, mouseY)) {
            FluidStack fluid = this.menu.getBlockEntity().getOxidizerTank().getFluid();
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(fluid.isEmpty() ? "Oxidizer" : fluid.getDisplayName().getString()));
            lines.add(Component.literal(fluid.getAmount() + " / " + this.menu.getBlockEntity().getTankCapacity() + " mB"));
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        } else if (isHovering(152, 36, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal("Solid Fuel: " + this.menu.getSolid() + "l"),
                    mouseX, mouseY);
        } else if (isHovering(134, 113, 34, 6, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        } else if (isHovering(-16, 36, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, sizeHint(), mouseX, mouseY);
        } else if (isHovering(-16, 52, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Detonator can only trigger center block."),
                    mouseX, mouseY);
        }
        renderExtraTooltip(graphics, mouseX, mouseY);
    }

    protected List<Component> sizeHint() {
        return List.of(
                Component.literal("Only accepts custom missiles"),
                Component.literal("of size 10 and 10/15."));
    }

    protected void renderExtraTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = (this.imageWidth - this.font.width(this.title)) / 2;
        graphics.drawString(this.font, this.title, titleX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
