package com.hbm.client.render.blockentity;

import api.hbm.entity.RadarEntry;
import com.hbm.blockentity.machine.RadarScreenBlockEntity;
import com.hbm.blocks.machine.DummyGridOffsets;
import com.hbm.blocks.machine.RadarScreenBlock;
import com.hbm.client.render.ObjPartModel;
import com.hbm.client.render.entity.HbmRenderTypes;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class RenderRadarScreen implements BlockEntityRenderer<RadarScreenBlockEntity> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(RefStrings.MODID, "models/obj/radar_screen.obj");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/models/machines/radar_screen.png");

    public RenderRadarScreen(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RadarScreenBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.hasProperty(RadarScreenBlock.FACING)
                ? state.getValue(RadarScreenBlock.FACING)
                : Direction.NORTH;
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(DummyGridOffsets.dummyableYaw(facing)));

        ObjPartModel.get(MODEL).render(pose, buffers, TEXTURE, "Plane", packedLight, packedOverlay);

        Matrix4f mat = pose.last().pose();
        Matrix3f nrm = pose.last().normal();
        if (be.linked) {
            double offset = ((be.getLevel() != null ? be.getLevel().getGameTime() % 56 : 0) + partialTick) / 30.0D;
            VertexConsumer sweep = buffers.getBuffer(HbmRenderTypes.RADAR_SWEEP);
            sweep.vertex(mat, 0.38F, (float) (2.0D - offset), 1.375F).color(0, 255, 0, 0).endVertex();
            sweep.vertex(mat, 0.38F, (float) (2.0D - offset), -0.375F).color(0, 255, 0, 0).endVertex();
            sweep.vertex(mat, 0.38F, (float) (2.0D - offset - 0.125D), -0.375F).color(0, 255, 0, 50).endVertex();
            sweep.vertex(mat, 0.38F, (float) (2.0D - offset - 0.125D), 1.375F).color(0, 255, 0, 50).endVertex();

            if (!be.entries.isEmpty()) {
                VertexConsumer blips = buffers.getBuffer(HbmRenderTypes.RADAR_SCREEN_BLIPS);
                for (RadarEntry entry : be.entries) {
                    double sX = (entry.posX - be.refX) / ((double) be.range + 1) * 0.875D;
                    double sZ = (entry.posZ - be.refZ) / ((double) be.range + 1) * 0.875D;
                    double size = 0.0625D;
                    float u0 = 216.0F / 256.0F;
                    float u1 = 224.0F / 256.0F;
                    float v0 = entry.blipLevel * 8.0F / 256.0F;
                    float v1 = (entry.blipLevel * 8.0F + 8.0F) / 256.0F;
                    float y0 = (float) (1.0D - sZ - size);
                    float y1 = (float) (1.0D - sZ + size);
                    float z0 = (float) (0.5D - sX - size);
                    float z1 = (float) (0.5D - sX + size);
                    quad(blips, mat, nrm, packedLight, packedOverlay, y1, z1, u0, v1, y1, z0, u1, v1, y0, z0, u1, v0, y0, z1, u0, v0);
                }
            }
        } else {
            int snow = 118 + (be.getLevel() != null ? be.getLevel().random.nextInt(81) : 0);
            VertexConsumer snowBuf = buffers.getBuffer(HbmRenderTypes.RADAR_SCREEN_BLIPS);
            float u0 = 216.0F / 256.0F;
            float u1 = 256.0F / 256.0F;
            float v0 = snow / 256.0F;
            float v1 = (snow + 40.0F) / 256.0F;
            quad(snowBuf, mat, nrm, packedLight, packedOverlay,
                    1.875F, 1.375F, u0, v1,
                    1.875F, -0.375F, u1, v1,
                    0.125F, -0.375F, u1, v0,
                    0.125F, 1.375F, u0, v0);
        }
        pose.popPose();
    }

    private static void quad(VertexConsumer buf, Matrix4f mat, Matrix3f nrm, int light, int overlay,
                             float y0, float z0, float u0, float v0,
                             float y1, float z1, float u1, float v1,
                             float y2, float z2, float u2, float v2,
                             float y3, float z3, float u3, float v3) {
        buf.vertex(mat, 0.38F, y0, z0).color(255, 255, 255, 255).uv(u0, v0)
                .overlayCoords(overlay).uv2(light).normal(nrm, 1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(mat, 0.38F, y1, z1).color(255, 255, 255, 255).uv(u1, v1)
                .overlayCoords(overlay).uv2(light).normal(nrm, 1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(mat, 0.38F, y2, z2).color(255, 255, 255, 255).uv(u2, v2)
                .overlayCoords(overlay).uv2(light).normal(nrm, 1.0F, 0.0F, 0.0F).endVertex();
        buf.vertex(mat, 0.38F, y3, z3).color(255, 255, 255, 255).uv(u3, v3)
                .overlayCoords(overlay).uv2(light).normal(nrm, 1.0F, 0.0F, 0.0F).endVertex();
    }

    public static void renderItem(PoseStack pose, MultiBufferSource buffers) {
        pose.pushPose();
        pose.translate(0.0D, 0.0D, -0.5D);
        ObjPartModel.get(MODEL).render(pose, buffers, TEXTURE, "Plane",
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(RadarScreenBlockEntity be) {
        return true;
    }
}
