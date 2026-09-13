package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.PurexBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * 1.7.10 {@code RenderPUREX}: Base, optional Frame, spinning Fan, translating Pump.
 */
public class RenderPurex implements BlockEntityRenderer<PurexBlockEntity> {
    public static final ResourceLocation BASE =
            new ResourceLocation(RefStrings.MODID, "block/purex_base");
    public static final ResourceLocation FRAME =
            new ResourceLocation(RefStrings.MODID, "block/purex_frame");
    public static final ResourceLocation FAN =
            new ResourceLocation(RefStrings.MODID, "block/purex_fan");
    public static final ResourceLocation PUMP =
            new ResourceLocation(RefStrings.MODID, "block/purex_pump");

    public static List<ResourceLocation> allModels() {
        return List.of(BASE, FRAME, FAN, PUMP);
    }

    public RenderPurex(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PurexBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.tesrYaw(DummyableMeta.coreFacing(meta))));

        ObjModelRenderer.render(pose, buffers, BASE, light, packedOverlay);
        if (be.hasFrame()) {
            ObjModelRenderer.render(pose, buffers, FRAME, light, packedOverlay);
        }

        float anim = Mth.lerp(partialTick, be.prevAnim, be.anim);
        pose.pushPose();
        pose.translate(1.5D, 1.25D, 0.0D);
        pose.mulPose(Axis.ZP.rotationDegrees(anim * 45.0F));
        pose.translate(-1.5D, -1.25D, 0.0D);
        ObjModelRenderer.render(pose, buffers, FAN, light, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.translate(sps(anim * 0.25D) * 0.5D, 0.0D, 0.0D);
        ObjModelRenderer.render(pose, buffers, PUMP, light, packedOverlay);
        pose.popPose();

        pose.popPose();
    }

    private static double sps(double x) {
        return Math.sin(Math.PI / 2.0D * Math.cos(x));
    }

    @Override
    public boolean shouldRenderOffScreen(PurexBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
