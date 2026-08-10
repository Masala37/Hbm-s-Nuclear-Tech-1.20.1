package com.hbm.client.render.entity;

import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.effect.EntityCloudSolinium;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Expanding colored sphere for FLEIJA / Solinium clouds.
 */
public final class RenderNukeCloud {
    private static final ResourceLocation TEX =
            new ResourceLocation(RefStrings.MODID, "textures/particle/flare.png");
    private static final int STACKS = 14;
    private static final int SLICES = 20;

    private RenderNukeCloud() {
    }

    public static class Fleija extends EntityRenderer<EntityCloudFleija> {
        public Fleija(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(EntityCloudFleija entity, float yaw, float partial, PoseStack pose,
                           MultiBufferSource buffer, int light) {
            renderCloud(entity, entity.age + partial, entity.getMaxAge(), pose, buffer,
                    0.0F, 1.0F, 1.0F, true);
            super.render(entity, yaw, partial, pose, buffer, light);
        }

        @Override
        public ResourceLocation getTextureLocation(EntityCloudFleija entity) {
            return TEX;
        }
    }

    public static class Solinium extends EntityRenderer<EntityCloudSolinium> {
        public Solinium(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(EntityCloudSolinium entity, float yaw, float partial, PoseStack pose,
                           MultiBufferSource buffer, int light) {
            renderCloud(entity, entity.age + partial, entity.getMaxAge(), pose, buffer,
                    0.153F, 1.0F, 0.855F, false);
            super.render(entity, yaw, partial, pose, buffer, light);
        }

        @Override
        public ResourceLocation getTextureLocation(EntityCloudSolinium entity) {
            return TEX;
        }
    }

    public static class Rainbow extends EntityRenderer<EntityCloudFleijaRainbow> {
        public Rainbow(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(EntityCloudFleijaRainbow entity, float yaw, float partial, PoseStack pose,
                           MultiBufferSource buffer, int light) {
            float t = (entity.age + partial) * 0.15F;
            float r = 0.5F + 0.5F * Mth.sin(t);
            float g = 0.5F + 0.5F * Mth.sin(t + 2.094F);
            float b = 0.5F + 0.5F * Mth.sin(t + 4.188F);
            renderCloud(entity, entity.age + partial, entity.getMaxAge(), pose, buffer, r, g, b, true);
            super.render(entity, yaw, partial, pose, buffer, light);
        }

        @Override
        public ResourceLocation getTextureLocation(EntityCloudFleijaRainbow entity) {
            return TEX;
        }
    }

    private static void renderCloud(Entity entity, float age, int maxAge, PoseStack poseStack,
                                    MultiBufferSource buffer, float r, float g, float b, boolean shockwave) {
        float ageScale = age / Math.max(1.0F, maxAge);
        float baseScale = age * 2.0F;
        float scale = ageScale * 1.2F;
        if (scale > 1.0F) {
            scale = Math.max(1.0F - (scale - 1.0F) * 5.0F, 0.0F);
        }
        scale *= 2.0F * baseScale;
        if (scale < 0.05F) {
            return;
        }

        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEX));

        drawSphere(consumer, matrix, normal, scale, r, g, b, 0.95F);
        float outer = scale;
        for (int i = 0; i < 3; i++) {
            outer *= 1.05F;
            drawSphere(consumer, matrix, normal, outer, r * 0.35F, g * 0.35F, b * 0.35F, 0.25F);
        }
        if (shockwave) {
            float shock = 5.0F * baseScale;
            float shockTint = (1.0F - ageScale) * 0.75F;
            drawSphere(consumer, matrix, normal, shock, shockTint, shockTint, shockTint, 0.35F);
        }
        poseStack.popPose();
    }

    private static void drawSphere(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                   float radius, float r, float g, float b, float a) {
        for (int stack = 0; stack < STACKS; stack++) {
            float phi0 = (float) (Math.PI * stack / STACKS);
            float phi1 = (float) (Math.PI * (stack + 1) / STACKS);
            float y0 = Mth.cos(phi0) * radius;
            float y1 = Mth.cos(phi1) * radius;
            float ring0 = Mth.sin(phi0) * radius;
            float ring1 = Mth.sin(phi1) * radius;
            for (int slice = 0; slice < SLICES; slice++) {
                float th0 = (float) (Math.PI * 2 * slice / SLICES);
                float th1 = (float) (Math.PI * 2 * (slice + 1) / SLICES);
                float x00 = Mth.cos(th0) * ring0;
                float z00 = Mth.sin(th0) * ring0;
                float x01 = Mth.cos(th1) * ring0;
                float z01 = Mth.sin(th1) * ring0;
                float x10 = Mth.cos(th0) * ring1;
                float z10 = Mth.sin(th0) * ring1;
                float x11 = Mth.cos(th1) * ring1;
                float z11 = Mth.sin(th1) * ring1;
                vertex(consumer, matrix, normal, x00, y0, z00, r, g, b, a);
                vertex(consumer, matrix, normal, x10, y1, z10, r, g, b, a);
                vertex(consumer, matrix, normal, x11, y1, z11, r, g, b, a);
                vertex(consumer, matrix, normal, x01, y0, z01, r, g, b, a);
            }
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                               float x, float y, float z, float r, float g, float b, float a) {
        float len = Mth.sqrt(x * x + y * y + z * z);
        float nx = len > 1.0E-4F ? x / len : 0.0F;
        float ny = len > 1.0E-4F ? y / len : 1.0F;
        float nz = len > 1.0E-4F ? z / len : 0.0F;
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(0.5F, 0.5F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}
