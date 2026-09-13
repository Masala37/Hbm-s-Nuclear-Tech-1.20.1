package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.GasCentBlockEntity;
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
 * 1.7.10 {@code RenderCentrifuge} gascent path: extra yaw 180, Centrifuge + Flag.
 */
public class RenderGasCent implements BlockEntityRenderer<GasCentBlockEntity> {
    public static final ResourceLocation CENTRIFUGE =
            new ResourceLocation(RefStrings.MODID, "block/gascent_centrifuge");
    public static final ResourceLocation FLAG =
            new ResourceLocation(RefStrings.MODID, "block/gascent_flag");

    public static List<ResourceLocation> allModels() {
        return List.of(CENTRIFUGE, FLAG);
    }

    public RenderGasCent(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GasCentBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.tesrYaw(DummyableMeta.coreFacing(meta))));
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        ObjModelRenderer.render(pose, buffers, CENTRIFUGE, light, packedOverlay);
        ObjModelRenderer.render(pose, buffers, FLAG, light, packedOverlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(GasCentBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
