package com.hbm.client.render.entity;

import com.hbm.client.render.missile.MissilePronter;
import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.handler.MissileStruct;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Legacy {@code RenderMissileCustom}: yaw-90, pitch around Z, undo yaw, then stacked parts.
 */
public class RenderMissileCustom extends EntityRenderer<EntityMissileCustom> {
    public RenderMissileCustom(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityMissileCustom entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        pose.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(pitch));
        pose.mulPose(Axis.YP.rotationDegrees(-(yaw - 90.0F)));
        MissileStruct struct = new MissileStruct(entity.warhead(), entity.fuselage(), entity.fins(), entity.thruster());
        MissilePronter.prontMissile(pose, buffers, struct, LightTexture.FULL_BRIGHT);
        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMissileCustom entity) {
        return new ResourceLocation("hbm", "textures/block/missile/missile_v2.png");
    }
}
