package com.hbm.client.render.entity;

import com.hbm.client.render.ObjModelRenderer;
import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Legacy {@code RenderBlackHole}: Sphere.obj core + accretion disc + polar jets.
 */
public class RenderBlackHole extends EntityRenderer<EntityBlackHole> {
    public static final ResourceLocation MODEL_SPHERE =
            new ResourceLocation(RefStrings.MODID, "block/black_hole_sphere");
    private static final ResourceLocation HOLE_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/block/black_hole.png");
    private static final int DISC_STEPS = 15;
    private static final int DISC_SEGMENTS = 16;
    private static final int JET_SEGMENTS = 12;

    public RenderBlackHole(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityBlackHole entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        float size = entity.getHoleSize();
        pose.pushPose();
        pose.scale(size, size, size);

        ObjModelRenderer.render(pose, buffers, MODEL_SPHERE,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        pose.mulPose(Axis.XP.rotationDegrees(entity.getId() % 90 - 45));
        pose.mulPose(Axis.YP.rotationDegrees(entity.getId() % 360));

        renderDisc(entity, partialTicks, pose, buffers);
        renderJets(pose, buffers);

        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffers, packedLight);
    }

    private static void renderDisc(EntityBlackHole entity, float partialTicks, PoseStack pose,
                                   MultiBufferSource buffers) {
        float glow = 0.75F;
        float spinBase = entity.tickCount + (partialTicks % 360.0F);
        // Custom RenderTypes share one BufferBuilder. getBuffer(additive) would flush an
        // empty translucent pass, so all rings drew as lightning with no depth — water
        // and clouds were already in the color buffer. Flush each pass separately.
        emitAllRings(pose, buffers.getBuffer(HbmRenderTypes.BHOLE_DISC_TRANSLUCENT),
                spinBase, glow, false);
        if (buffers instanceof MultiBufferSource.BufferSource src) {
            src.endBatch(HbmRenderTypes.BHOLE_DISC_TRANSLUCENT);
        }
        emitAllRings(pose, buffers.getBuffer(HbmRenderTypes.BHOLE_DISC_ADDITIVE),
                spinBase, glow, true);
        if (buffers instanceof MultiBufferSource.BufferSource src) {
            src.endBatch(HbmRenderTypes.BHOLE_DISC_ADDITIVE);
        }
    }

    private static void emitAllRings(PoseStack pose, VertexConsumer consumer,
                                     float spinBase, float glow, boolean glowPass) {
        for (int k = 0; k < DISC_STEPS; k++) {
            pose.pushPose();
            float spin = spinBase * -(float) Math.pow(k + 1, 1.25D);
            pose.mulPose(Axis.YP.rotationDegrees(spin));
            Matrix4f matrix = pose.last().pose();
            Matrix3f normal = pose.last().normal();
            double s = 3.0D - k * 0.175D;
            emitDiscRing(consumer, matrix, normal, k, s, glowPass, glow);
            pose.popPose();
        }
    }

    private static void emitDiscRing(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                     int iteration, double s, boolean glowPass, float glow) {
        double[] xs = new double[DISC_SEGMENTS + 1];
        double[] zs = new double[DISC_SEGMENTS + 1];
        for (int i = 0; i <= DISC_SEGMENTS; i++) {
            double a = i * Math.PI * 2.0D / DISC_SEGMENTS;
            xs[i] = Math.cos(a);
            zs[i] = Math.sin(a);
        }
        for (int i = 0; i < DISC_SEGMENTS; i++) {
            float[] innerRgb = glowPass
                    ? new float[]{1.0F, 1.0F, 1.0F, glow}
                    : colorFromIteration(iteration, 1.0F);
            // Legacy glow pass: inner is white+glow, outer is still setColorFromIteration(k, 0).
            float[] outerRgb = colorFromIteration(iteration, 0.0F);

            discVert(consumer, matrix, normal, xs[i] * s, zs[i] * s,
                    0.5F + (float) (xs[i] * 0.25F), 0.5F + (float) (zs[i] * 0.25F), innerRgb);
            discVert(consumer, matrix, normal, xs[i] * s * 2.0D, zs[i] * s * 2.0D,
                    0.5F + (float) (xs[i] * 0.5F), 0.5F + (float) (zs[i] * 0.5F), outerRgb);
            discVert(consumer, matrix, normal, xs[i + 1] * s * 2.0D, zs[i + 1] * s * 2.0D,
                    0.5F + (float) (xs[i + 1] * 0.5F), 0.5F + (float) (zs[i + 1] * 0.5F), outerRgb);
            discVert(consumer, matrix, normal, xs[i + 1] * s, zs[i + 1] * s,
                    0.5F + (float) (xs[i + 1] * 0.25F), 0.5F + (float) (zs[i + 1] * 0.25F), innerRgb);
        }
    }

    private static float[] colorFromIteration(int iteration, float alpha) {
        if (iteration < 5) {
            float g = 0.125F + iteration * (1.0F / 10.0F);
            return new float[]{1.0F, g, 0.0F, alpha};
        }
        if (iteration == 5) {
            return new float[]{1.0F, 1.0F, 0.0F, alpha};
        }
        int i = iteration - 6;
        float r = 1.0F - i * (1.0F / 9.0F);
        float g = 1.0F - i * (1.0F / 9.0F);
        float b = i * (1.0F / 5.0F);
        return new float[]{r, g, b, alpha};
    }

    private static void discVert(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                 double x, double z, float u, float v, float[] rgba) {
        consumer.vertex(matrix, (float) x, 0.0F, (float) z)
                .color(rgba[0], rgba[1], rgba[2], rgba[3])
                .uv(u, v)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static void renderJets(PoseStack pose, MultiBufferSource buffers) {
        VertexConsumer consumer = buffers.getBuffer(HbmRenderTypes.BHOLE_JETS);
        Matrix4f matrix = pose.last().pose();
        for (int j = -1; j <= 1; j += 2) {
            float[] rimX = new float[JET_SEGMENTS + 1];
            float[] rimZ = new float[JET_SEGMENTS + 1];
            for (int i = 0; i <= JET_SEGMENTS; i++) {
                double a = i * Math.PI / 6.0D * -j;
                rimX[i] = (float) (Math.cos(a) * 0.5D);
                rimZ[i] = (float) (Math.sin(a) * 0.5D);
            }
            for (int i = 0; i < JET_SEGMENTS; i++) {
                jetVert(consumer, matrix, 0.0F, 0.0F, 0.0F, 1.0F, 0.35F);
                jetVert(consumer, matrix, rimX[i], 10.0F * j, rimZ[i], 1.0F, 0.0F);
                jetVert(consumer, matrix, rimX[i + 1], 10.0F * j, rimZ[i + 1], 1.0F, 0.0F);
            }
        }
    }

    private static void jetVert(VertexConsumer consumer, Matrix4f matrix,
                                float x, float y, float z, float rgb, float alpha) {
        consumer.vertex(matrix, x, y, z)
                .color(rgb, rgb, rgb, alpha)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBlackHole entity) {
        return HOLE_TEX;
    }
}
