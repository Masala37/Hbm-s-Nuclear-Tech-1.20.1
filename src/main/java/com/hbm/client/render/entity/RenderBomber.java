package com.hbm.client.render.entity;

import com.hbm.entity.logic.EntityBomber;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Multi-box dark metal plane (fuselage + wings + tail) for bomber flybys.
 */
public class RenderBomber extends EntityRenderer<EntityBomber> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/block/iron_block.png");

    public RenderBomber(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 2.0F;
    }

    @Override
    public void render(EntityBomber entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        // Prefer live motion for orientation (custom Entity yaw sync is unreliable).
        Vec3 motion = entity.getDeltaMovement();
        float yaw;
        if (motion.horizontalDistanceSqr() > 1.0E-6D) {
            yaw = (float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI));
        } else {
            yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        }
        // Legacy RenderBomber: glRotatef(yaw - 90, 0, 1, 0) with nose along +X after rotate
        pose.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin((entity.tickCount + partialTicks) * 0.05F) * 5.0F));

        VertexConsumer consumer = buffers.getBuffer(RenderType.entitySolid(TEXTURE));
        Matrix4f mat = pose.last().pose();
        Matrix3f norm = pose.last().normal();

        // Long fuselage (nose = +X)
        box(consumer, mat, norm, packedLight, 0.35F, 0.35F, 0.35F, 1.0F,
                -6.0F, -0.4F, -0.55F, 6.0F, 0.55F, 0.55F);
        // Cockpit bulge
        box(consumer, mat, norm, packedLight, 0.25F, 0.28F, 0.32F, 1.0F,
                4.2F, 0.2F, -0.35F, 5.6F, 0.85F, 0.35F);
        // Main wings
        box(consumer, mat, norm, packedLight, 0.3F, 0.3F, 0.3F, 1.0F,
                -1.0F, -0.15F, -5.5F, 1.5F, 0.15F, 5.5F);
        // Engines under wings
        box(consumer, mat, norm, packedLight, 0.22F, 0.22F, 0.22F, 1.0F,
                -0.2F, -0.55F, -3.8F, 1.8F, -0.05F, -2.6F);
        box(consumer, mat, norm, packedLight, 0.22F, 0.22F, 0.22F, 1.0F,
                -0.2F, -0.55F, 2.6F, 1.8F, -0.05F, 3.8F);
        // Vertical stabilizer
        box(consumer, mat, norm, packedLight, 0.28F, 0.28F, 0.28F, 1.0F,
                -5.8F, 0.4F, -0.12F, -4.4F, 2.2F, 0.12F);
        // Horizontal tail
        box(consumer, mat, norm, packedLight, 0.28F, 0.28F, 0.28F, 1.0F,
                -5.6F, 0.9F, -1.8F, -4.6F, 1.15F, 1.8F);

        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffers, packedLight);
    }

    private static void box(VertexConsumer consumer, Matrix4f mat, Matrix3f norm, int light,
                            float r, float g, float b, float a,
                            float x0, float y0, float z0, float x1, float y1, float z1) {
        // +Z
        quad(consumer, mat, norm, light, r, g, b, a, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0, 0, 1);
        // -Z
        quad(consumer, mat, norm, light, r, g, b, a, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, 0, 0, -1);
        // +Y
        quad(consumer, mat, norm, light, r, g, b, a, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, 0, 1, 0);
        // -Y
        quad(consumer, mat, norm, light, r, g, b, a, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0, -1, 0);
        // +X
        quad(consumer, mat, norm, light, r, g, b, a, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, 1, 0, 0);
        // -X
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
    public ResourceLocation getTextureLocation(EntityBomber entity) {
        return TEXTURE;
    }
}
