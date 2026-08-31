package com.hbm.client.screen;

import com.hbm.blockentity.machine.MissileAssemblyBlockEntity;
import com.hbm.client.render.missile.MissilePronter;
import com.hbm.entity.missile.MissileAssemblyRecipes;
import com.hbm.handler.MissileStruct;
import com.hbm.inventory.menu.MissileAssemblyMenu;
import com.hbm.lib.RefStrings;
import com.hbm.network.AssembleMissilePacket;
import com.hbm.network.ModMessages;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy {@code GUIMachineMissileAssembly} — 176×222, status lamps, assemble button, spinning preview.
 */
public class MissileAssemblyScreen extends AbstractContainerScreen<MissileAssemblyMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_missile_assembly.png");

    public MissileAssemblyScreen(MissileAssemblyMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(115, 35, 18, 18, (int) mouseX, (int) mouseY)) {
            if (this.minecraft != null && this.minecraft.getSoundManager() != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            ModMessages.CHANNEL.sendToServer(new AssembleMissilePacket(this.menu.getBlockEntity().getBlockPos()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        ItemStack chip = slot(MissileAssemblyBlockEntity.SLOT_CHIP);
        ItemStack warhead = slot(MissileAssemblyBlockEntity.SLOT_WARHEAD);
        ItemStack fuselage = slot(MissileAssemblyBlockEntity.SLOT_FUSELAGE);
        ItemStack fins = slot(MissileAssemblyBlockEntity.SLOT_FINS);
        ItemStack thruster = slot(MissileAssemblyBlockEntity.SLOT_THRUSTER);
        ItemStack output = slot(MissileAssemblyBlockEntity.SLOT_OUTPUT);

        if (MissileAssemblyRecipes.fuselageState(fuselage) == 1) {
            graphics.blit(TEXTURE, this.leftPos + 49, this.topPos + 23, 194, 0, 6, 8);
        }
        if (MissileAssemblyRecipes.warheadState(warhead, fuselage, thruster) == 1) {
            graphics.blit(TEXTURE, this.leftPos + 31, this.topPos + 23, 194, 0, 6, 8);
        }
        if (MissileAssemblyRecipes.chipState(chip) == 1) {
            graphics.blit(TEXTURE, this.leftPos + 13, this.topPos + 23, 194, 0, 6, 8);
        }
        int stability = MissileAssemblyRecipes.stabilityState(fins, fuselage);
        if (stability == 1) {
            graphics.blit(TEXTURE, this.leftPos + 67, this.topPos + 23, 194, 0, 6, 8);
        } else if (stability == 0) {
            graphics.blit(TEXTURE, this.leftPos + 67, this.topPos + 23, 200, 0, 6, 8);
        }
        if (MissileAssemblyRecipes.thrusterState(thruster, fuselage) == 1) {
            graphics.blit(TEXTURE, this.leftPos + 85, this.topPos + 23, 194, 0, 6, 8);
        }
        if (MissileAssemblyRecipes.canBuild(chip, warhead, fuselage, fins, thruster, output)) {
            graphics.blit(TEXTURE, this.leftPos + 115, this.topPos + 35, 176, 0, 18, 18);
        }
        renderMissilePreview(graphics, warhead, fuselage, fins, thruster);
    }

    private ItemStack slot(int index) {
        return this.menu.getSlot(index).getItem();
    }

    private void renderMissilePreview(GuiGraphics graphics, ItemStack warhead, ItemStack fuselage,
                                        ItemStack fins, ItemStack thruster) {
        if (this.minecraft == null) {
            return;
        }
        MissileStruct missile = new MissileStruct(warhead, fuselage, fins, thruster);
        if (missile.warhead == null && missile.fuselage == null
                && missile.fins == null && missile.thruster == null) {
            return;
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.leftPos + 88, this.topPos + 98, 100);
        pose.mulPose(Axis.YP.rotationDegrees(-(System.currentTimeMillis() / 10L % 360L)));
        double size = 8.0D * 18.0D;
        double height = Math.max(MissilePronter.getHeight(missile), 6.0D);
        double scale = size / height;
        pose.translate(MissilePronter.getHeight(missile) / 2.0D * scale, 0.0D, 0.0D);
        pose.scale((float) scale, (float) scale, (float) scale);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        pose.scale(-1.0F, -1.0F, -1.0F);
        Lighting.setupForEntityInInventory();
        MultiBufferSource.BufferSource buffers = this.minecraft.renderBuffers().bufferSource();
        MissilePronter.prontMissile(pose, buffers, missile, LightTexture.FULL_BRIGHT);
        buffers.endBatch();
        Lighting.setupFor3DItems();
        pose.popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
