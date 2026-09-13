package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.FireboxBlockEntity;
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

import java.util.List;

/**
 * 1.7.10 {@code RenderFirebox}: Main, Door, InnerBurning/InnerEmpty.
 */
public class RenderFirebox implements BlockEntityRenderer<FireboxBlockEntity> {
    public static final ResourceLocation MAIN =
            new ResourceLocation(RefStrings.MODID, "block/firebox_main");
    public static final ResourceLocation DOOR =
            new ResourceLocation(RefStrings.MODID, "block/firebox_door");
    public static final ResourceLocation INNER_BURNING =
            new ResourceLocation(RefStrings.MODID, "block/firebox_inner_burning");
    public static final ResourceLocation INNER_EMPTY =
            new ResourceLocation(RefStrings.MODID, "block/firebox_inner_empty");

    public static List<ResourceLocation> allModels() {
        return List.of(MAIN, DOOR, INNER_BURNING, INNER_EMPTY);
    }

    public RenderFirebox(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FireboxBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.dummyableYaw(DummyableMeta.coreFacing(meta))));
        pose.mulPose(Axis.YP.rotationDegrees(-90.0F));

        ObjModelRenderer.render(pose, buffers, MAIN, light, packedOverlay);

        pose.pushPose();
        float door = be.getDoorAngle(partialTick);
        pose.translate(1.375D, 0.0D, 0.375D);
        pose.mulPose(Axis.YP.rotationDegrees(-door));
        pose.translate(-1.375D, 0.0D, -0.375D);
        ObjModelRenderer.render(pose, buffers, DOOR, light, packedOverlay);
        pose.popPose();

        if (be.wasOn()) {
            ObjModelRenderer.render(pose, buffers, INNER_BURNING, LightTexture.pack(15, 15), packedOverlay);
        } else {
            ObjModelRenderer.render(pose, buffers, INNER_EMPTY, light, packedOverlay);
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(FireboxBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
