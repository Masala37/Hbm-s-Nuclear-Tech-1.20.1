package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.RadarNTBlockEntity;
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

public class RenderRadar implements BlockEntityRenderer<RadarNTBlockEntity> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(RefStrings.MODID, "models/obj/radar.obj");
    public static final ResourceLocation BASE_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/machines/radar_base.png");
    public static final ResourceLocation DISH_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/machines/radar_dish.png");

    public RenderRadar(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RadarNTBlockEntity be, float partialTick, PoseStack pose,
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
        model.render(pose, buffers, BASE_TEX, "Base", packedLight, packedOverlay);
        pose.mulPose(Axis.YP.rotationDegrees(-dishYaw));
        pose.translate(-0.125D, 0.0D, 0.0D);
        model.render(pose, buffers, DISH_TEX, "Dish", packedLight, packedOverlay);
        pose.popPose();
    }

    public static void renderItem(PoseStack pose, MultiBufferSource buffers) {
        ObjPartModel model = ObjPartModel.get(MODEL);
        pose.pushPose();
        model.render(pose, buffers, BASE_TEX, "Base", LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
        pose.translate(-0.125D, 0.0D, 0.0D);
        model.render(pose, buffers, DISH_TEX, "Dish", LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(RadarNTBlockEntity be) {
        return true;
    }
}
