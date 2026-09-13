package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.RtgBlockEntity;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.energy.EnergyConnect;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * 1.7.10 {@code RenderRTG}: Gen body plus Connector stubs toward energy neighbors.
 */
public class RenderRtg implements BlockEntityRenderer<RtgBlockEntity> {
    public static final ResourceLocation GEN =
            new ResourceLocation(RefStrings.MODID, "block/rtg_gen");
    public static final ResourceLocation CONNECTOR =
            new ResourceLocation(RefStrings.MODID, "block/rtg_connector");

    public RenderRtg(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RtgBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        ObjModelRenderer.render(pose, buffers, GEN, light, packedOverlay);

        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();
        if (level != null) {
            if (EnergyConnect.canConnect(level, pos.relative(Direction.EAST), Direction.EAST)) {
                ObjModelRenderer.render(pose, buffers, CONNECTOR, light, packedOverlay);
            }
            if (EnergyConnect.canConnect(level, pos.relative(Direction.WEST), Direction.WEST)) {
                pose.mulPose(Axis.YP.rotationDegrees(180.0F));
                ObjModelRenderer.render(pose, buffers, CONNECTOR, light, packedOverlay);
                pose.mulPose(Axis.YP.rotationDegrees(-180.0F));
            }
            if (EnergyConnect.canConnect(level, pos.relative(Direction.NORTH), Direction.NORTH)) {
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                ObjModelRenderer.render(pose, buffers, CONNECTOR, light, packedOverlay);
                pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
            }
            if (EnergyConnect.canConnect(level, pos.relative(Direction.SOUTH), Direction.SOUTH)) {
                pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
                ObjModelRenderer.render(pose, buffers, CONNECTOR, light, packedOverlay);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
        }

        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(RtgBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
