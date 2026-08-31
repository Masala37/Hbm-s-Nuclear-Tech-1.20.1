package com.hbm.client.screen;

import com.hbm.blockentity.machine.LaunchPadRustedBlockEntity;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.client.render.entity.RenderMissile;
import com.hbm.inventory.menu.LaunchPadRustedMenu;
import com.hbm.lib.RefStrings;
import com.hbm.network.LaunchPadRustedPacket;
import com.hbm.network.ModMessages;
import com.hbm.registry.ModItems;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

public class LaunchPadRustedScreen extends AbstractContainerScreen<LaunchPadRustedMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/gui_launch_pad_rusted.png");

    public LaunchPadRustedScreen(LaunchPadRustedMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 236;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 4;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(26, 36, 16, 16, (int) mouseX, (int) mouseY)) {
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            ModMessages.CHANNEL.sendToServer(new LaunchPadRustedPacket(this.menu.getBlockEntity().getBlockPos()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        LaunchPadRustedBlockEntity pad = this.menu.getBlockEntity();
        boolean hasCodes = pad.getItems().getStackInSlot(LaunchPadRustedBlockEntity.SLOT_CODE)
                .is(ModItems.LAUNCH_CODE.get());
        boolean hasKey = pad.getItems().getStackInSlot(LaunchPadRustedBlockEntity.SLOT_KEY)
                .is(ModItems.LAUNCH_KEY.get());
        if (hasCodes) {
            graphics.blit(TEXTURE, x + 121, y + 32, 192, 0, 6, 8);
        }
        if (hasKey) {
            graphics.blit(TEXTURE, x + 139, y + 32, 192, 0, 6, 8);
        }
        if (hasCodes && hasKey && pad.isMissileLoaded()) {
            Random rand = new Random(pad.getBlockPos().getX() * 131_071 + pad.getBlockPos().getZ());
            int launchCodes = rand.nextInt(100_000_000);
            for (int i = 0; i < 8; i++) {
                int magnitude = (int) Math.pow(10, i);
                int digit = (launchCodes % (magnitude * 10)) / magnitude;
                graphics.blit(TEXTURE, x + 109 + 6 * i, y + 85, 192 + 6 * digit, 8, 6, 8);
            }
        }
        if (pad.isMissileLoaded() && this.minecraft != null) {
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(this.leftPos + 70, this.topPos + 120, 100);
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            pose.scale(0.875F, 0.875F, 0.875F);
            pose.scale(-8.0F, -8.0F, -8.0F);
            Lighting.setupForEntityInInventory();
            MultiBufferSource.BufferSource buffers = this.minecraft.renderBuffers().bufferSource();
            ItemStack missile = new ItemStack(ModItems.MISSILE_DOOMSDAY_RUSTED.get());
            ObjModelRenderer.render(pose, buffers, RenderMissile.modelForItem(missile),
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            buffers.endBatch();
            Lighting.setupFor3DItems();
            pose.popPose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(26, 36, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    Component.literal("Release Missile").withStyle(ChatFormatting.YELLOW),
                    Component.literal("Missile is locked in launch position,"),
                    Component.literal("releasing may cause damage to the missile."),
                    Component.literal("Damaged missile can not be put back"),
                    Component.literal("into launching position.")
            ), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = (this.imageWidth - this.font.width(this.title)) / 2;
        graphics.drawString(this.font, this.title, titleX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
