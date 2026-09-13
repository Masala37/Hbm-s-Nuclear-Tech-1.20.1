package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.PressBlockEntity;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderPress implements BlockEntityRenderer<PressBlockEntity> {
    public static final ResourceLocation BODY =
            new ResourceLocation(RefStrings.MODID, "block/press_body");
    public static final ResourceLocation HEAD =
            new ResourceLocation(RefStrings.MODID, "block/press_head");

    public RenderPress(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PressBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        ObjModelRenderer.render(pose, buffers, BODY, light, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.scale(0.99F, 1.0F, 0.99F);
        double p = be.getRenderPress(partialTick) / (double) PressBlockEntity.MAX_PRESS;
        pose.translate(0.0D, Mth.clamp(1.0D - p, 0.0D, 1.0D) * 0.875D, 0.0D);
        ObjModelRenderer.render(pose, buffers, HEAD, light, packedOverlay);
        pose.popPose();

        ItemStack stack = be.getSyncStack();
        if (!stack.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5D, 1.0D - 0.0625F * 165 / 100.0F, 0.5D);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            ItemRenderer items = Minecraft.getInstance().getItemRenderer();
            items.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, be.getLevel(), 0);
            pose.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(PressBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
