package com.hbm.client.render.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class RenderTorex extends EntityRenderer<EntityNukeTorex> {
    private static final ResourceLocation CLOUDLET =
            new ResourceLocation(RefStrings.MODID, "textures/particle/particle_base.png");
    private static final ResourceLocation FLASH =
            new ResourceLocation(RefStrings.MODID, "textures/particle/flare.png");

    private static long flashTimestamp;
    private static long shakeTimestamp;

    public RenderTorex(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityNukeTorex cloud, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        if (cloud.tickCount < 100 && Minecraft.getInstance().level != null) {
            Minecraft.getInstance().level.setSkyFlashTime(2);
        }

        cloudletWrapper(cloud, partialTicks, poseStack, buffer);

        if (cloud.tickCount < 101) {
            flashWrapper(cloud, partialTicks, poseStack, buffer);
        }

        if (cloud.tickCount < 10 && System.currentTimeMillis() - flashTimestamp > 1000L) {
            flashTimestamp = System.currentTimeMillis();
        }

        if (cloud.didPlaySound && !cloud.didShake && System.currentTimeMillis() - shakeTimestamp > 1000L) {
            shakeTimestamp = System.currentTimeMillis();
            cloud.didShake = true;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.hurtTime = 15;
                player.hurtDuration = 15;
            }
        }

        poseStack.popPose();
        super.render(cloud, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void cloudletWrapper(EntityNukeTorex cloud, float interp, PoseStack poseStack, MultiBufferSource buffer) {
        ArrayList<Cloudlet> cloudlets = new ArrayList<>(cloud.cloudlets);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            cloudlets.sort(Comparator.comparingDouble((Cloudlet c) ->
                    player.distanceToSqr(c.posX, c.posY, c.posZ)).reversed());
        }

        VertexConsumer consumer = buffer.getBuffer(HbmRenderTypes.torexCloud(CLOUDLET));
        PoseStack.Pose pose = poseStack.last();
        Quaternionf orientation = new Quaternionf(this.entityRenderDispatcher.cameraOrientation());
        int light = LightTexture.FULL_BRIGHT;

        for (Cloudlet cloudlet : cloudlets) {
            Vec3 vec = cloudlet.getInterpPos(interp);
            float x = (float) (vec.x - cloud.getX());
            float y = (float) (vec.y - cloud.getY());
            float z = (float) (vec.z - cloud.getZ());
            tessellateCloudlet(consumer, pose, orientation, x, y, z, cloudlet, interp, light);
        }
    }

    private void flashWrapper(EntityNukeTorex cloud, float interp, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(HbmRenderTypes.torexFlash(FLASH));
        PoseStack.Pose pose = poseStack.last();
        Quaternionf orientation = new Quaternionf(this.entityRenderDispatcher.cameraOrientation());
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
        float scale = cloud.getScale();
        float brightness = cloud.type == EntityNukeTorex.TorexType.CONDENSATION ? 0.9F : 0.75F * cloud.colorMod;
        Vec3 color = cloud.getInterpColor(interp);
        float r = (float) color.x * brightness;
        float g = (float) color.y * brightness;
        float b = (float) color.z * brightness;
        writeBillboard(consumer, pose, orientation, posX, posY, posZ, scale, r, g, b, alpha, light);
    }

    private static void tessellateFlash(VertexConsumer consumer, PoseStack.Pose pose, Quaternionf orientation,
                                        float posX, float posY, float posZ, float scale, float alpha, int light) {
        writeBillboard(consumer, pose, orientation, posX, posY, posZ, scale, 1F, 1F, 1F, alpha, light);
    }

    private static void writeBillboard(VertexConsumer consumer, PoseStack.Pose pose, Quaternionf orientation,
                                       float posX, float posY, float posZ, float scale,
                                       float r, float g, float b, float a, int light) {
        // Match legacy ActiveRenderInfo corner order / UVs
        Vector3f[] corners = new Vector3f[] {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float[] u = {1F, 1F, 0F, 0F};
        float[] v = {1F, 0F, 0F, 1F};

        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        for (int i = 0; i < 4; i++) {
            Vector3f corner = corners[i];
            corner.rotate(orientation);
            corner.mul(scale);
            corner.add(posX, posY, posZ);
            consumer.vertex(matrix, corner.x(), corner.y(), corner.z())
                    .color(r, g, b, a)
                    .uv(u[i], v[i])
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(normal, 0.0F, 1.0F, 0.0F)
                    .endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityNukeTorex entity) {
        return CLOUDLET;
    }
}
