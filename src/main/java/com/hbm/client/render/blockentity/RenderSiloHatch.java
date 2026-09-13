package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.SiloHatchBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.blocks.generic.SiloHatchLogic;
import com.hbm.client.render.ObjPartModel;
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
 * 1.7 {@code RenderDoorGeneric} silo hatch: Frame static, Hatch lifts then pitches.
 */
public class RenderSiloHatch implements BlockEntityRenderer<SiloHatchBlockEntity> {
    public static final ResourceLocation SMALL_MODEL =
            new ResourceLocation(RefStrings.MODID, "models/obj/silo_hatch.obj");
    public static final ResourceLocation LARGE_MODEL =
            new ResourceLocation(RefStrings.MODID, "models/obj/silo_hatch_large.obj");
    public static final ResourceLocation SMALL_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/doors/silo_hatch.png");
    public static final ResourceLocation LARGE_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/doors/silo_hatch_large.png");
    public static final ResourceLocation SMALL =
            new ResourceLocation(RefStrings.MODID, "block/silo_hatch");
    public static final ResourceLocation LARGE =
            new ResourceLocation(RefStrings.MODID, "block/silo_hatch_large");

    public static List<ResourceLocation> allModels() {
        return List.of(SMALL, LARGE);
    }

    public RenderSiloHatch(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SiloHatchBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        boolean large = be.large();
        float openTicks = be.renderOpenTicks();
        float originZ = SiloHatchLogic.hatchOriginZ(large);
        ObjPartModel model = ObjPartModel.get(large ? LARGE_MODEL : SMALL_MODEL);
        ResourceLocation texture = large ? LARGE_TEX : SMALL_TEX;

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.tesrYaw(DummyableMeta.coreFacing(meta))));

        model.render(pose, buffers, texture, "Frame", light, packedOverlay, true);

        pose.pushPose();
        pose.translate(0.0D, SiloHatchLogic.HATCH_ORIGIN_Y, originZ);
        pose.mulPose(Axis.XP.rotationDegrees(SiloHatchLogic.hatchPitch(openTicks)));
        pose.translate(0.0D, -SiloHatchLogic.HATCH_ORIGIN_Y + SiloHatchLogic.hatchLift(openTicks), -originZ);
        model.render(pose, buffers, texture, "Hatch", light, packedOverlay, true);
        pose.popPose();

        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(SiloHatchBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
