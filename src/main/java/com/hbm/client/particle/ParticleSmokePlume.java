package com.hbm.client.particle;

import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Legacy {@code ParticleSmokePlume} — ground-hugging launch pad smoke ({@code launchSmoke}).
 * Spreads horizontally, collides with the ground, grows and lingers (~4–5s).
 */
@OnlyIn(Dist.CLIENT)
public class ParticleSmokePlume extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/particle/contrail.png");

    public static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            // Depth write ON — same fix as Torex cloudlets: without it, sky/water
            // composite through the smoke (legacy/vanilla translucent particles use false).
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_SMOKE_PLUME";
        }
    };

    private float plumeScale = 0.25F;
    private int age;
    private final int maxAge;

    public ParticleSmokePlume(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.maxAge = 80 + random.nextInt(20);
        this.hasPhysics = true;
        this.gravity = 0.0F;
        this.friction = 1.0F; // we damp manually like legacy
    }

    public void setMotion(double mx, double my, double mz) {
        this.xd = mx;
        this.yd = my;
        this.zd = mz;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        float prevScale = this.plumeScale;
        this.age++;
        if (this.age >= this.maxAge) {
            remove();
            return;
        }

        // Grow from 0.25 → ~2.25 over life (legacy).
        this.plumeScale = 0.25F + ((float) this.age / (float) this.maxAge) * 2.0F;
        double bak = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);

        // Scale growth adds a little lift; move with block collision.
        move(this.xd, this.yd + (this.plumeScale - prevScale), this.zd);

        if (this.onGround) {
            // Legacy: on vertical collision restore speed into Y so the puff billows
            // along the ground instead of vanishing into the floor.
            this.yd = bak * 0.35D;
            this.xd *= 0.88D;
            this.zd *= 0.88D;
        } else {
            this.xd *= 0.925D;
            this.yd *= 0.925D;
            this.zd *= 0.925D;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cam = camera.getPosition();
        float cx = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
        float cy = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
        float cz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);

        float alpha = 1.0F - ((float) this.age / (float) this.maxAge);
        Random urandom = new Random(this.hashCode());
        Quaternionf rotation = new Quaternionf(camera.rotation());
        int light = LightTexture.FULL_BRIGHT;

        for (int i = 0; i < 6; i++) {
            float gray = urandom.nextFloat() * 0.75F + 0.1F;
            float scale = this.plumeScale;
            float ox = (float) (urandom.nextGaussian() * 0.5D * scale);
            float oy = (float) (urandom.nextGaussian() * 0.5D * scale);
            float oz = (float) (urandom.nextGaussian() * 0.5D * scale);

            Vector3f[] corners = new Vector3f[]{
                    new Vector3f(-1.0F, -1.0F, 0.0F),
                    new Vector3f(-1.0F, 1.0F, 0.0F),
                    new Vector3f(1.0F, 1.0F, 0.0F),
                    new Vector3f(1.0F, -1.0F, 0.0F)
            };
            for (Vector3f corner : corners) {
                corner.mul(scale);
                corner.rotate(rotation);
                corner.add(cx + ox, cy + oy, cz + oz);
            }

            buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(1.0F, 1.0F)
                    .color(gray, gray, gray, alpha).uv2(light).endVertex();
            buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(1.0F, 0.0F)
                    .color(gray, gray, gray, alpha).uv2(light).endVertex();
            buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(0.0F, 0.0F)
                    .color(gray, gray, gray, alpha).uv2(light).endVertex();
            buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(0.0F, 1.0F)
                    .color(gray, gray, gray, alpha).uv2(light).endVertex();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }
}
