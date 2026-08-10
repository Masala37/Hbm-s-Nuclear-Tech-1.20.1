package com.hbm.client.render.entity;

import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.entity.projectile.EntityClusterBomblet;
import com.hbm.entity.projectile.EntityFallingNuke;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.lib.RefStrings;
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
 * Tumbling mesh renderers for bomb projectiles (legacy {@code RenderShrapnel} / cluster).
 */
public final class RenderBombProjectiles {
    private static final ResourceLocation SHRAPNEL_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/entity/shrapnel.png");
    private static final ResourceLocation BOMBLET_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/block/bombs/mine_shrapnel.png");
    private static final ResourceLocation NUKE_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/block/bombs/custom.png");

    private RenderBombProjectiles() {
    }

    public static class Shrapnel extends EntityRenderer<EntityShrapnel> {
        public Shrapnel(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.15F;
        }

        @Override
        public void render(EntityShrapnel entity, float yaw, float partial, PoseStack pose,
                           MultiBufferSource buffer, int light) {
            pose.pushPose();
            float spin = (entity.tickCount + partial) * 10.0F;
            pose.mulPose(Axis.XP.rotationDegrees(180.0F));
            pose.mulPose(Axis.XP.rotationDegrees(spin));
            pose.mulPose(Axis.YP.rotationDegrees(spin));
            pose.mulPose(Axis.ZP.rotationDegrees(spin));

            // Legacy: lava/volcano ejecta scaled ×3
            float scale = entity.getShrapnelType() >= 2 ? 0.75F : 0.25F;
            pose.scale(scale, scale, scale);

            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(SHRAPNEL_TEX));
            texturedCube(consumer, pose.last().pose(), pose.last().normal(), light,
                    volcanoTint(entity));
            pose.popPose();
            super.render(entity, yaw, partial, pose, buffer, light);
        }

        private static float[] volcanoTint(EntityShrapnel entity) {
            byte type = entity.getShrapnelType();
            if (type == 4) {
                return new float[]{0.55F, 1.0F, 0.35F, 1.0F};
            }
            if (type >= 2) {
                return new float[]{1.0F, 0.45F, 0.15F, 1.0F};
            }
            return new float[]{1.0F, 1.0F, 1.0F, 1.0F};
        }

        @Override
        public ResourceLocation getTextureLocation(EntityShrapnel entity) {
            return SHRAPNEL_TEX;
        }
    }

    public static class ClusterBomblet extends EntityRenderer<EntityClusterBomblet> {
        public ClusterBomblet(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.2F;
        }

        @Override
        public void render(EntityClusterBomblet entity, float yaw, float partial, PoseStack pose,
                           MultiBufferSource buffer, int light) {
            pose.pushPose();
            float spin = (entity.tickCount + partial) * 12.0F;
            pose.mulPose(Axis.XP.rotationDegrees(spin));
            pose.mulPose(Axis.YP.rotationDegrees(spin * 0.8F));
            pose.scale(0.35F, 0.55F, 0.35F);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BOMBLET_TEX));
            texturedCube(consumer, pose.last().pose(), pose.last().normal(), light,
                    new float[]{1.0F, 1.0F, 1.0F, 1.0F});
            pose.popPose();
            super.render(entity, yaw, partial, pose, buffer, light);
        }

        @Override
        public ResourceLocation getTextureLocation(EntityClusterBomblet entity) {
            return BOMBLET_TEX;
        }
    }

    public static class BombletZeta extends EntityRenderer<EntityBombletZeta> {
        public BombletZeta(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.25F;
        }

        @Override
        public void render(EntityBombletZeta entity, float yaw, float partial, PoseStack pose,
                           MultiBufferSource buffer, int light) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
            pose.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
            float spin = (entity.tickCount + partial) * 8.0F;
            pose.mulPose(Axis.ZP.rotationDegrees(spin));
            pose.scale(0.4F, 0.7F, 0.4F);
            float[] tint = zetaTint(entity.getBombletType());
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BOMBLET_TEX));
            texturedCube(consumer, pose.last().pose(), pose.last().normal(), light, tint);
            pose.popPose();
            super.render(entity, yaw, partial, pose, buffer, light);
        }

        private static float[] zetaTint(int type) {
            return switch (type) {
                case 1 -> new float[]{1.0F, 0.45F, 0.15F, 1.0F};
                case 2 -> new float[]{0.45F, 1.0F, 0.35F, 1.0F};
                case 4 -> new float[]{0.85F, 0.95F, 0.55F, 1.0F};
                default -> new float[]{0.75F, 0.75F, 0.8F, 1.0F};
            };
        }

        @Override
        public ResourceLocation getTextureLocation(EntityBombletZeta entity) {
            return BOMBLET_TEX;
        }
    }

    public static class FallingNuke extends EntityRenderer<EntityFallingNuke> {
        public FallingNuke(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(EntityFallingNuke entity, float yaw, float partial, PoseStack pose,
                           MultiBufferSource buffer, int light) {
            pose.pushPose();
            pose.translate(0.0D, 0.25D, 0.0D);
            pose.mulPose(Axis.YP.rotationDegrees(yaw));
            pose.scale(1.5F, 0.6F, 0.6F);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(NUKE_TEX));
            texturedCube(consumer, pose.last().pose(), pose.last().normal(), light,
                    new float[]{1.0F, 1.0F, 1.0F, 1.0F});
            pose.popPose();
            super.render(entity, yaw, partial, pose, buffer, light);
        }

        @Override
        public ResourceLocation getTextureLocation(EntityFallingNuke entity) {
            return NUKE_TEX;
        }
    }

    /** Unit cube centered at origin, UVs 0–1 per face. */
    private static void texturedCube(VertexConsumer consumer, Matrix4f mat, Matrix3f norm, int light,
                                     float[] rgba) {
        float r = rgba[0], g = rgba[1], b = rgba[2], a = rgba[3];
        // +Z
        quad(consumer, mat, norm, light, r, g, b, a, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0, 0, 1);
        // -Z
        quad(consumer, mat, norm, light, r, g, b, a, 0.5F, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0, 0, -1);
        // +Y
        quad(consumer, mat, norm, light, r, g, b, a, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0, 1, 0);
        // -Y
        quad(consumer, mat, norm, light, r, g, b, a, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0, -1, 0);
        // +X
        quad(consumer, mat, norm, light, r, g, b, a, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 1, 0, 0);
        // -X
        quad(consumer, mat, norm, light, r, g, b, a, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -1, 0, 0);
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
}
