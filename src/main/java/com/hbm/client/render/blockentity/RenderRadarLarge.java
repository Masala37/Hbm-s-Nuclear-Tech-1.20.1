package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.RadarLargeBlockEntity;
import com.hbm.client.render.ObjPartModel;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RenderRadarLarge implements BlockEntityRenderer<RadarLargeBlockEntity> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(RefStrings.MODID, "models/obj/radar_large.obj");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/models/machines/radar_large.png");

    public RenderRadarLarge(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RadarLargeBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        float rot = be.prevRotation + (be.rotation - be.prevRotation) * partialTick;
        render(pose, buffers, packedLight, packedOverlay, rot);
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay,
                              float dishYaw) {
        ObjPartModel model = ObjPartModel.get(MODEL);
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        model.render(pose, buffers, TEXTURE, "Radar", packedLight, packedOverlay);
        pose.mulPose(Axis.YP.rotationDegrees(-dishYaw));
        model.render(pose, buffers, TEXTURE, "Dish", packedLight, packedOverlay);
        pose.popPose();
    }

    public static void renderItem(PoseStack pose, MultiBufferSource buffers) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.scale(0.5F, 0.5F, 0.5F);
        ObjPartModel model = ObjPartModel.get(MODEL);
        model.render(pose, buffers, TEXTURE, "Radar", LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
        pose.mulPose(Axis.YP.rotationDegrees((float) (System.currentTimeMillis() % 3600L * 0.1D)));
        model.render(pose, buffers, TEXTURE, "Dish", LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(RadarLargeBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
