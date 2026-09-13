package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.EPressBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
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

public class RenderEPress implements BlockEntityRenderer<EPressBlockEntity> {
    public static final ResourceLocation BODY =
            new ResourceLocation(RefStrings.MODID, "block/epress_body");
    public static final ResourceLocation HEAD =
            new ResourceLocation(RefStrings.MODID, "block/epress_head");

    public RenderEPress(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EPressBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        float yaw = DummyableMeta.tesrYaw(coreFacing(be));

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        ObjModelRenderer.render(pose, buffers, BODY, light, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.translate(0.5D, 1.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        double p = be.getRenderPress(partialTick) / (double) EPressBlockEntity.MAX_PRESS;
        pose.translate(0.0D, Mth.clamp(1.0D - p, 0.0D, 1.0D) * 0.875D, 0.0D);
        ObjModelRenderer.render(pose, buffers, HEAD, light, packedOverlay);
        pose.popPose();

        ItemStack stack = be.getSyncStack();
        if (!stack.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5D, 1.0D, 0.5D);
            pose.mulPose(Axis.YP.rotationDegrees(yaw));
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose.translate(0.0D, 0.0D, -0.0625F * 165 / 100.0F);
            ItemRenderer items = Minecraft.getInstance().getItemRenderer();
            items.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, be.getLevel(), 0);
            pose.popPose();
        }
    }

    private static int coreFacing(EPressBlockEntity be) {
        if (be.getBlockState().hasProperty(BlockDummyable.META)) {
            return DummyableMeta.coreFacing(be.getBlockState().getValue(BlockDummyable.META));
        }
        return DummyableMeta.SOUTH;
    }

    @Override
    public boolean shouldRenderOffScreen(EPressBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
