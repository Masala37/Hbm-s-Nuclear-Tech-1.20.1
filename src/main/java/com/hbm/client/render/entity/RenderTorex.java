package com.hbm.client.render.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hbm.client.NukeFxClient;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.effect.EntityNukeTorex.Cloudlet;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Torex mushroom billboards. Drawn in the normal entity pass with depth write so
 * water/sky behind the cloud are occluded — without AFTER_WEATHER (that stage
 * warps the projection matrix and makes the cloud drift/spin).
 */
public class RenderTorex extends EntityRenderer<EntityNukeTorex> {
    private static final ResourceLocation CLOUDLET =
            new ResourceLocation(RefStrings.MODID, "textures/particle/particle_base.png");
    private static final ResourceLocation FLASH =
            new ResourceLocation(RefStrings.MODID, "textures/particle/flare.png");

    public RenderTorex(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityNukeTorex cloud, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        applyClientFx(cloud);

        Quaternionf orientation = new Quaternionf(this.entityRenderDispatcher.cameraOrientation());
        cloudletWrapper(cloud, partialTicks, poseStack, buffer, orientation);
        if (cloud.tickCount < 101) {
            flashWrapper(cloud, partialTicks, poseStack, buffer, orientation);
        }

        super.render(cloud, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void applyClientFx(EntityNukeTorex cloud) {
        if (cloud.tickCount < 100 && Minecraft.getInstance().level != null) {
            Minecraft.getInstance().level.setSkyFlashTime(2);
        }

        if (cloud.tickCount < 10) {
            NukeFxClient.markFlash();
        }

        if (cloud.didPlaySound && !cloud.didShake) {
            NukeFxClient.markShake();
            cloud.didShake = true;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.hurtTime = 15;
                player.hurtDuration = 15;
            }
        }
    }

    private static void cloudletWrapper(EntityNukeTorex cloud, float interp, PoseStack poseStack,
                                        MultiBufferSource buffer, Quaternionf orientation) {
        ArrayList<Cloudlet> cloudlets = new ArrayList<>(cloud.cloudlets);
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        cloudlets.sort(Comparator.comparingDouble((Cloudlet c) ->
                cam.distanceToSqr(c.posX, c.posY, c.posZ)).reversed());

        VertexConsumer consumer = buffer.getBuffer(HbmRenderTypes.TOREX_CLOUD);
        PoseStack.Pose pose = poseStack.last();
        int light = LightTexture.FULL_BRIGHT;

        double ox = cloud.getX();
        double oy = cloud.getY();
        double oz = cloud.getZ();

        for (Cloudlet cloudlet : cloudlets) {
            Vec3 vec = cloudlet.getInterpPos(interp);
            float x = (float) (vec.x - ox);
            float y = (float) (vec.y - oy);
            float z = (float) (vec.z - oz);
            tessellateCloudlet(consumer, pose, orientation, x, y, z, cloudlet, interp, light);
        }
    }

    private static void flashWrapper(EntityNukeTorex cloud, float interp, PoseStack poseStack,
                                     MultiBufferSource buffer, Quaternionf orientation) {
        VertexConsumer consumer = buffer.getBuffer(HbmRenderTypes.TOREX_FLASH);
        PoseStack.Pose pose = poseStack.last();
        int light = LightTexture.FULL_BRIGHT;

        double age = Math.min(cloud.tickCount + interp, 100);
        float alpha = (float) ((100D - age) / 100F);

        Random rand = new Random(cloud.getId());

        for (int i = 0; i < 3; i++) {
            float x = (float) (rand.nextGaussian() * 0.5F * cloud.rollerSize);
            float y = (float) (rand.nextGaussian() * 0.5F * cloud.rollerSize);
            float z = (float) (rand.nextGaussian() * 0.5F * cloud.rollerSize);
            tessellateFlash(consumer, pose, orientation, x, y + (float) cloud.coreHeight, z,
                    (float) (25 * cloud.rollerSize), alpha, light);
        }
    }

    private static void tessellateCloudlet(VertexConsumer consumer, PoseStack.Pose pose, Quaternionf orientation,
                                           float posX, float posY, float posZ, Cloudlet cloud, float interp, int light) {
        float alpha = cloud.getAlpha();
        if (cloud.type != EntityNukeTorex.TorexType.CONDENSATION) {
            alpha = Mth.clamp(alpha * 1.2F, 0F, 1F);
        }
        float scale = cloud.getScale();
        float brightness = cloud.type == EntityNukeTorex.TorexType.CONDENSATION ? 0.9F : 0.85F * cloud.colorMod;
        Vec3 color = cloud.getInterpColor(interp);
        float r = Mth.clamp((float) color.x * brightness, 0F, 1F);
        float g = Mth.clamp((float) color.y * brightness, 0F, 1F);
        float b = Mth.clamp((float) color.z * brightness, 0F, 1F);
        writeBillboard(consumer, pose, orientation, posX, posY, posZ, scale, r, g, b, alpha, light);
    }

    private static void tessellateFlash(VertexConsumer consumer, PoseStack.Pose pose, Quaternionf orientation,
                                        float posX, float posY, float posZ, float scale, float alpha, int light) {
        writeBillboard(consumer, pose, orientation, posX, posY, posZ, scale, 1F, 1F, 1F, alpha, light);
    }

    private static void writeBillboard(VertexConsumer consumer, PoseStack.Pose pose, Quaternionf orientation,
                                       float posX, float posY, float posZ, float scale,
                                       float r, float g, float b, float a, int light) {
        Vector3f[] corners = new Vector3f[] {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float[] u = {1F, 1F, 0F, 0F};
        float[] v = {1F, 0F, 0F, 1F};

        Matrix4f matrix = pose.pose();
        int lightU = light & 0xFFFF;
        int lightV = (light >> 16) & 0xFFFF;

        for (int i = 0; i < 4; i++) {
            Vector3f corner = corners[i];
            corner.rotate(orientation);
            corner.mul(scale);
            corner.add(posX, posY, posZ);
            consumer.vertex(matrix, corner.x(), corner.y(), corner.z())
                    .color(r, g, b, a)
                    .uv(u[i], v[i])
                    .uv2(lightU, lightV)
                    .endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityNukeTorex entity) {
        return CLOUDLET;
    }
}
