package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.PumpjackBlockEntity;
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
 * 1.7.10 {@code RenderPumpjack}. Cable quads skipped.
 */
public class RenderPumpjack implements BlockEntityRenderer<PumpjackBlockEntity> {
    public static final ResourceLocation BASE =
            new ResourceLocation(RefStrings.MODID, "block/pumpjack_base");
    public static final ResourceLocation ROTOR =
            new ResourceLocation(RefStrings.MODID, "block/pumpjack_rotor");
    public static final ResourceLocation HEAD =
            new ResourceLocation(RefStrings.MODID, "block/pumpjack_head");
    public static final ResourceLocation CARRIAGE =
            new ResourceLocation(RefStrings.MODID, "block/pumpjack_carriage");

    public static List<ResourceLocation> allModels() {
        return List.of(BASE, ROTOR, HEAD, CARRIAGE);
    }

    public RenderPumpjack(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PumpjackBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.tesrYaw(DummyableMeta.coreFacing(meta))));
        float rotation = be.prevRot + (be.rot - be.prevRot) * partialTick;
        ObjModelRenderer.render(pose, buffers, BASE, light, packedOverlay);

        pose.pushPose();
        pose.translate(0.0D, 1.5D, -5.5D);
        pose.mulPose(Axis.XP.rotationDegrees(rotation - 90.0F));
        pose.translate(0.0D, -1.5D, 5.5D);
        ObjModelRenderer.render(pose, buffers, ROTOR, light, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.translate(0.0D, 3.5D, -3.5D);
        pose.mulPose(Axis.XP.rotationDegrees((float) (Math.toDegrees(Math.sin(Math.toRadians(rotation))) * 0.25D)));
        pose.translate(0.0D, -3.5D, 3.5D);
        ObjModelRenderer.render(pose, buffers, HEAD, light, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.translate(0.0D, -Math.sin(Math.toRadians(rotation)), 0.0D);
        ObjModelRenderer.render(pose, buffers, CARRIAGE, light, packedOverlay);
        pose.popPose();

        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(PumpjackBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
