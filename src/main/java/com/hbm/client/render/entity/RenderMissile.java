package com.hbm.client.render.entity;

import com.hbm.entity.missile.EntityMissileBaseNT;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Elongated rocket mesh for missiles.
 */
public class RenderMissile extends EntityRenderer<EntityMissileBaseNT> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/block/iron_block.png");

    public RenderMissile(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(EntityMissileBaseNT entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        pose.mulPose(Axis.XP.rotationDegrees(entity.getXRot() - 90.0F));

        VertexConsumer consumer = buffers.getBuffer(RenderType.entitySolid(TEXTURE));
        Matrix4f mat = pose.last().pose();
        Matrix3f norm = pose.last().normal();

        // Body
        box(consumer, mat, norm, packedLight, 0.55F, 0.55F, 0.6F, 1.0F,
                -0.35F, 0.0F, -0.35F, 0.35F, 3.2F, 0.35F);
        // Nose cone
        box(consumer, mat, norm, packedLight, 0.7F, 0.25F, 0.25F, 1.0F,
                -0.22F, 3.0F, -0.22F, 0.22F, 4.0F, 0.22F);
        // Fins
        box(consumer, mat, norm, packedLight, 0.4F, 0.4F, 0.45F, 1.0F,
                -0.9F, 0.0F, -0.08F, 0.9F, 0.7F, 0.08F);
        box(consumer, mat, norm, packedLight, 0.4F, 0.4F, 0.45F, 1.0F,
                -0.08F, 0.0F, -0.9F, 0.08F, 0.7F, 0.9F);

        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffers, packedLight);
    }

    private static void box(VertexConsumer consumer, Matrix4f mat, Matrix3f norm, int light,
                            float r, float g, float b, float a,
                            float x0, float y0, float z0, float x1, float y1, float z1) {
        quad(consumer, mat, norm, light, r, g, b, a, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0, 0, 1);
        quad(consumer, mat, norm, light, r, g, b, a, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, 0, 0, -1);
        quad(consumer, mat, norm, light, r, g, b, a, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, 0, 1, 0);
        quad(consumer, mat, norm, light, r, g, b, a, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0, -1, 0);
        quad(consumer, mat, norm, light, r, g, b, a, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, 1, 0, 0);
        quad(consumer, mat, norm, light, r, g, b, a, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, -1, 0, 0);
    }

    private static void quad(VertexConsumer consumer, Matrix4f mat, Matrix3f norm, int light,
                             float r, float g, float b, float a,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float nx, float ny, float nz) {
        consumer.vertex(mat, x0, y0, z0).color(r, g, b, a).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, nx, ny, nz).endVertex();
        consumer.vertex(mat, x1, y1, z1).color(r, g, b, a).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, nx, ny, nz).endVertex();
        consumer.vertex(mat, x2, y2, z2).color(r, g, b, a).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, nx, ny, nz).endVertex();
        consumer.vertex(mat, x3, y3, z3).color(r, g, b, a).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, nx, ny, nz).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMissileBaseNT entity) {
        return TEXTURE;
    }
}
