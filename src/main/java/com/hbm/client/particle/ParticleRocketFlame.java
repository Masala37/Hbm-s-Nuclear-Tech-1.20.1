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
 * Legacy {@code ParticleRocketFlame} — additive orange exhaust using {@code particle_base}.
 */
@OnlyIn(Dist.CLIENT)
public class ParticleRocketFlame extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/particle/particle_base.png");

    public static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            // Depth write ON so sky/water behind exhaust fail the depth test
            // (same occlusion fix as Torex / ParticleSmokePlume).
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_ROCKET_FLAME";
        }
    };

    private float flameScale = 1.0F;
    private int age;
    private int maxAge;

    public ParticleRocketFlame(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.maxAge = 60 + random.nextInt(20);
        this.hasPhysics = false;
        this.gravity = 0.0F;
    }

    public ParticleRocketFlame setScale(float scale) {
        this.flameScale = scale;
        return this;
    }

    public ParticleRocketFlame setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
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
        this.age++;
        if (this.age >= this.maxAge) {
            remove();
            return;
        }
        this.xd *= 0.91D;
        this.yd *= 0.91D;
        this.zd *= 0.91D;
        move(this.xd, this.yd, this.zd);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cam = camera.getPosition();
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);

        float life = (float) this.age / (float) this.maxAge;
        Random urandom = new Random(this.hashCode());
        Quaternionf rotation = new Quaternionf(camera.rotation());

        for (int i = 0; i < 10; i++) {
            float add = urandom.nextFloat() * 0.3F;
            float dark = 1.0F - Math.min(life / 0.25F, 1.0F);
            float r = 1.0F * dark + add;
            float g = 0.6F * dark + add;
            float b = add;
            float a = (float) Math.pow(1.0F - Math.min(life, 1.0F), 0.5) * 0.75F;

            float spread = (float) Math.pow(life * 4.0F, 1.5) + 1.0F;
            spread *= this.flameScale;
            float scale = (urandom.nextFloat() * 0.5F + 0.1F + life * 2.0F) * this.flameScale;

            float ox = (float) ((urandom.nextGaussian() - 1.0D) * 0.2F * spread);
            float oy = (float) ((urandom.nextGaussian() - 1.0D) * 0.5F * spread);
            float oz = (float) ((urandom.nextGaussian() - 1.0D) * 0.2F * spread);

            Vector3f[] corners = new Vector3f[]{
                    new Vector3f(-1.0F, -1.0F, 0.0F),
                    new Vector3f(-1.0F, 1.0F, 0.0F),
                    new Vector3f(1.0F, 1.0F, 0.0F),
                    new Vector3f(1.0F, -1.0F, 0.0F)
            };
            for (Vector3f corner : corners) {
                corner.mul(scale);
                corner.rotate(rotation);
                corner.add(px + ox, py + oy, pz + oz);
            }

            int light = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
            buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(1.0F, 1.0F)
                    .color(r, g, b, a).uv2(light).endVertex();
            buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(1.0F, 0.0F)
                    .color(r, g, b, a).uv2(light).endVertex();
            buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(0.0F, 0.0F)
                    .color(r, g, b, a).uv2(light).endVertex();
            buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(0.0F, 1.0F)
                    .color(r, g, b, a).uv2(light).endVertex();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }
}
