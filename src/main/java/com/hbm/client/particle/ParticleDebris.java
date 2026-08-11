package com.hbm.client.particle;

import com.hbm.wiaj.WorldInAJar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

/**
 * Legacy {@code ParticleDebris} — tumbling WorldInAJar block chunk.
 */
@OnlyIn(Dist.CLIENT)
public class ParticleDebris extends Particle {
    private static final Random RNG = new Random();

    public WorldInAJar jar;
    private final int seed;
    private float rotPitch;
    private float rotYaw;
    private float prevRotPitch;
    private float prevRotYaw;

    public ParticleDebris(ClientLevel level, double x, double y, double z,
                          double mx, double my, double mz) {
        super(level, x, y, z);
        double mult = 3.0D;
        this.xd = mx * mult;
        this.yd = my * mult;
        this.zd = mz * mult;
        this.lifetime = 100;
        this.gravity = 0.15F;
        this.hasPhysics = false;
        this.seed = System.identityHashCode(this);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age > 5) {
            this.hasPhysics = true;
        }

        RNG.setSeed(this.seed);
        this.prevRotPitch = this.rotPitch;
        this.prevRotYaw = this.rotYaw;
        this.rotPitch += RNG.nextFloat() * 10.0F;
        this.rotYaw += RNG.nextFloat() * 10.0F;

        if (Math.floorMod(this.seed, 3) == 0 && jar != null) {
            float flameScale = 1.0F * Math.max(jar.sizeY, 6) / 16.0F;
            ParticleRocketFlame fx = new ParticleRocketFlame(level, x, y, z)
                    .setScale(flameScale)
                    .setMaxAge(50);
            Minecraft.getInstance().particleEngine.add(fx);
        }

        this.yd -= this.gravity;
        move(this.xd, this.yd, this.zd);

        this.age++;
        if (this.onGround || this.age >= this.lifetime) {
            remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        if (jar == null) {
            return;
        }

        Vec3 cam = camera.getPosition();
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);
        float pitch = Mth.lerp(partialTicks, prevRotPitch, rotPitch);
        float yaw = Mth.lerp(partialTicks, prevRotYaw, rotYaw);

        PoseStack pose = new PoseStack();
        pose.translate(px, py, pz);
        pose.mulPose(Axis.YP.rotationDegrees(pitch));
        pose.mulPose(Axis.ZP.rotationDegrees(yaw));
        pose.translate(-jar.sizeX / 2.0D, -jar.sizeY / 2.0D, -jar.sizeZ / 2.0D);

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        int light = LevelRenderer.getLightColor(level, BlockPos.containing(this.x, this.y, this.z));

        for (int ix = 0; ix < jar.sizeX; ix++) {
            for (int iy = 0; iy < jar.sizeY; iy++) {
                for (int iz = 0; iz < jar.sizeZ; iz++) {
                    BlockState state = jar.getBlock(ix, iy, iz);
                    if (state.isAir()) {
                        continue;
                    }
                    pose.pushPose();
                    pose.translate(ix, iy, iz);
                    try {
                        dispatcher.renderSingleBlock(state, pose, buffers, light, OverlayTexture.NO_OVERLAY);
                    } catch (Exception ignored) {
                        // Legacy swallowed render exceptions for exotic blocks.
                    }
                    pose.popPose();
                }
            }
        }
        buffers.endBatch();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }
}
