package com.hbm.client.render.entity;

import com.hbm.client.render.ObjModelRenderer;
import com.hbm.entity.missile.EntityMissileAntiBallistic;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Same yaw/pitch pose as {@link RenderMissile} on the ABM mesh.
 */
public class RenderMissileAntiBallistic extends EntityRenderer<EntityMissileAntiBallistic> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(RefStrings.MODID, "block/missile_abm");

    public RenderMissileAntiBallistic(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityMissileAntiBallistic entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        pose.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(pitch));
        pose.mulPose(Axis.YP.rotationDegrees(-(yaw - 90.0F)));
        ObjModelRenderer.render(pose, buffers, MODEL, LightTexture.FULL_BRIGHT,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMissileAntiBallistic entity) {
        return new ResourceLocation(RefStrings.MODID, "textures/block/missile/missile_abm.png");
    }
}
