package com.hbm.client.screen;

import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.client.render.entity.RenderMissile;
import com.hbm.inventory.menu.LaunchPadMenu;
import com.hbm.lib.RefStrings;
import com.hbm.network.LaunchPadPacket;
import com.hbm.network.ModMessages;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

/**
 * Legacy {@code GUILaunchPadLarge} — 176×236, gauges, status, missile preview.
 */
public class LaunchPadScreen extends AbstractContainerScreen<LaunchPadMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/gui_launch_pad_large.png");

    public LaunchPadScreen(LaunchPadMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 236;
        // Legacy: inventory label at ySize - 96 + 2
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 4;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // QoL: click status panel to launch when ready (legacy uses redstone only).
        if (button == 0 && isHovering(8, 98, 52, 18, (int) mouseX, (int) mouseY)) {
            ModMessages.CHANNEL.sendToServer(new LaunchPadPacket(this.menu.getBlockEntity().getBlockPos()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int maxEnergy = this.menu.getMaxEnergy();
        if (maxEnergy > 0) {
            int h = this.menu.getEnergy() * 52 / maxEnergy;
            if (h > 0) {
                graphics.blit(TEXTURE, x + 107, y + 88 - h, 176, 52 - h, 16, h);
            }
        }

        drawTank(graphics, x + 125, y, this.menu.getBlockEntity().getFuelTank().getFluid(),
                this.menu.getFuelAmount(), LaunchPadBlockEntity.TANK_CAPACITY);
        drawTank(graphics, x + 143, y, this.menu.getBlockEntity().getOxidizerTank().getFluid(),
                this.menu.getOxidizerAmount(), LaunchPadBlockEntity.TANK_CAPACITY);

        // Legacy checkmark lamps — solid/pre-fueled (fuelCap 0) always show green fuel lamps
        boolean missileOk = this.menu.getBlockEntity().isMissileValid();
        int fuelCost = this.menu.getBlockEntity().getRequiredFuelAmount();
        if (missileOk) {
            int uv = this.menu.getEnergy() >= LaunchPadBlockEntity.LAUNCH_COST ? 192 : 198;
            graphics.blit(TEXTURE, x + 112, y + 23, uv, 0, 6, 8);
        }
        if (fuelCost <= 0 && missileOk) {
            graphics.blit(TEXTURE, x + 130, y + 23, 192, 0, 6, 8);
            graphics.blit(TEXTURE, x + 148, y + 23, 192, 0, 6, 8);
        } else {
            if (this.menu.getFuelAmount() >= fuelCost && fuelCost > 0) {
                graphics.blit(TEXTURE, x + 130, y + 23, 192, 0, 6, 8);
            } else if (this.menu.getFuelAmount() > 0) {
                graphics.blit(TEXTURE, x + 130, y + 23, 198, 0, 6, 8);
            }
            if (this.menu.getOxidizerAmount() >= fuelCost && fuelCost > 0) {
                graphics.blit(TEXTURE, x + 148, y + 23, 192, 0, 6, 8);
            } else if (this.menu.getOxidizerAmount() > 0) {
                graphics.blit(TEXTURE, x + 148, y + 23, 198, 0, 6, 8);
            }
        }

        renderMissilePreview(graphics);
        renderStatusText(graphics);
    }

    private void renderMissilePreview(GuiGraphics graphics) {
        ItemStack missile = this.menu.getBlockEntity().getItems().getStackInSlot(LaunchPadBlockEntity.SLOT_MISSILE);
        if (missile.isEmpty() || this.minecraft == null) {
            return;
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        // Legacy: guiLeft+70, guiTop+120, z=100; YP 90; scale; YP 75
        pose.translate(this.leftPos + 70, this.topPos + 120, 100);
        double scale = 1.75D;
        if (missile.getItem() instanceof com.hbm.items.weapon.MissileItem mi) {
            scale = switch (mi.getTier()) {
                case TIER0 -> 2.25D; // Micro — legacy TYPE_TIER0 pad preview
                case TIER2 -> 1.375D;
                default -> 1.75D;
            };
        } else if (RenderMissile.isStrongItem(missile)) {
            scale = 1.375D;
        }
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        pose.scale((float) (-8.0D * scale), (float) (-8.0D * scale), (float) (-8.0D * scale));
        pose.mulPose(Axis.YP.rotationDegrees(75.0F));

        Lighting.setupForEntityInInventory();
        MultiBufferSource.BufferSource buffers = this.minecraft.renderBuffers().bufferSource();
        int light = 0xF000F0;
        ObjModelRenderer.render(pose, buffers, RenderMissile.modelForItem(missile), light, OverlayTexture.NO_OVERLAY);
        buffers.endBatch();
        Lighting.setupFor3DItems();
        pose.popPose();
    }

    private void renderStatusText(GuiGraphics graphics) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.leftPos + 34, this.topPos + 107, 0);

        int padState = this.menu.getPadState();
        String text;
        int color;
        float scale;
        if (padState == LaunchPadBlockEntity.STATE_READY) {
            text = "Ready";
            color = 0x00FF00;
            scale = 0.8F;
        } else if (padState == LaunchPadBlockEntity.STATE_LOADING) {
            text = "Loading...";
            color = 0xFF8000;
            scale = 0.6F;
        } else {
            text = "Not ready";
            color = 0xFF0000;
            scale = 0.5F;
        }
        pose.scale(scale, scale, 1.0F);
        int w = this.font.width(text);
        graphics.drawString(this.font, text, -w / 2, -this.font.lineHeight / 2, color, false);
        pose.popPose();
    }

    private void drawTank(GuiGraphics graphics, int x, int top, FluidStack fluid, int amount, int capacity) {
        if (capacity <= 0 || amount <= 0) {
            return;
        }
        int h = amount * 52 / capacity;
        if (h <= 0) {
            return;
        }
        int y = top + 88 - h;
        if (fluid.isEmpty()) {
            graphics.fill(x, y, x + 16, top + 88, 0xFF808080);
            return;
        }
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation still = ext.getStillTexture(fluid);
        int color = ext.getTintColor(fluid);
        if (still == null || this.minecraft == null) {
            graphics.fill(x, y, x + 16, top + 88, color | 0xFF000000);
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
        graphics.blit(x, y, 0, 16, h, sprite);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(107, 36, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"),
                    mouseX, mouseY);
        } else if (isHovering(125, 36, 16, 52, mouseX, mouseY)) {
            FluidStack fluid = this.menu.getBlockEntity().getFuelTank().getFluid();
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(fluid.isEmpty() ? "Ethanol" : fluid.getDisplayName().getString()));
            lines.add(Component.literal(this.menu.getFuelAmount() + " / " + LaunchPadBlockEntity.TANK_CAPACITY + " mB"));
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        } else if (isHovering(143, 36, 16, 52, mouseX, mouseY)) {
            FluidStack fluid = this.menu.getBlockEntity().getOxidizerTank().getFluid();
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(fluid.isEmpty() ? "Peroxide" : fluid.getDisplayName().getString()));
            lines.add(Component.literal(this.menu.getOxidizerAmount() + " / " + LaunchPadBlockEntity.TANK_CAPACITY + " mB"));
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        } else if (isHovering(8, 98, 52, 18, mouseX, mouseY) && this.menu.hasTarget()) {
            graphics.renderTooltip(this.font,
                    Component.literal("Target: " + this.menu.getTargetX() + ", "
                            + this.menu.getTargetY() + ", " + this.menu.getTargetZ()),
                    mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Legacy: title centered at y=4
        int titleX = (this.imageWidth - this.font.width(this.title)) / 2;
        graphics.drawString(this.font, this.title, titleX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
