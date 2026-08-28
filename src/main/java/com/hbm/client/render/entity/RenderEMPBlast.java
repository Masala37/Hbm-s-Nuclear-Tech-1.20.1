package com.hbm.client.render.entity;

import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Expanding EMP ring (legacy {@code RenderEMPBlast} using Ring.obj + EMPBlast texture).
 * Drawn as a textured horizontal annulus that scales with entity age.
 */
public class RenderEMPBlast extends EntityRenderer<EntityEMPBlast> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/entity/emp_blast.png");
    private static final int SEGMENTS = 48;

    public RenderEMPBlast(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityEMPBlast entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float scale = entity.getScale() + partialTicks;
        if (scale < 0.1F) {
            return;
        }

        float alpha = 1.0F - Mth.clamp((entity.getAge() + partialTicks) / entity.getMaxAge(), 0.0F, 1.0F);
        alpha = Mth.clamp(alpha * 1.25F, 0.0F, 0.85F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        int light = LightTexture.FULL_BRIGHT;

        float inner = scale * 0.82F;
        float outer = scale;
        float r = 0.55F;
        float g = 0.85F;
        float b = 1.0F;

        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = (float) (i * Math.PI * 2.0D / SEGMENTS);
            float a1 = (float) ((i + 1) * Math.PI * 2.0D / SEGMENTS);
            float c0 = Mth.cos(a0);
            float s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1);
            float s1 = Mth.sin(a1);

            float u0 = i / (float) SEGMENTS;
            float u1 = (i + 1) / (float) SEGMENTS;

            vertex(consumer, matrix, normal, c0 * outer, s0 * outer, 0.0F, u0, 0.0F, r, g, b, alpha, light);
            vertex(consumer, matrix, normal, c1 * outer, s1 * outer, 0.0F, u1, 0.0F, r, g, b, alpha, light);
            vertex(consumer, matrix, normal, c1 * inner, s1 * inner, 0.0F, u1, 1.0F, r, g, b, alpha, light);
            vertex(consumer, matrix, normal, c0 * inner, s0 * inner, 0.0F, u0, 1.0F, r, g, b, alpha, light);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                               float x, float y, float z, float u, float v,
                               float r, float g, float b, float a, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEMPBlast entity) {
        return TEXTURE;
    }
}
