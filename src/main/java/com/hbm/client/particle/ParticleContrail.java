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

@OnlyIn(Dist.CLIENT)
public class ParticleContrail extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/particle/contrail.png");

    public static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
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
            return "HBM_CONTRAIL";
        }
    };

    private final float trailScale;
    private int age;
    private final int maxAge;

    public ParticleContrail(ClientLevel level, double x, double y, double z,
                             float red, float green, float blue, float scale) {
        super(level, x, y, z);
        this.maxAge = 100 + random.nextInt(40);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.trailScale = scale;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.age++;
        if (this.age >= this.maxAge) {
            remove();
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
        float scale = (alpha + 0.5F) * this.trailScale;

        for (int i = 0; i < 6; i++) {
            float mod = urandom.nextFloat() * 0.2F + 0.2F;
            float r = this.rCol + mod;
            float g = this.gCol + mod;
            float b = this.bCol + mod;
            float ox = (float) (urandom.nextGaussian() * 0.5D * this.trailScale);
            float oy = (float) (urandom.nextGaussian() * 0.5D * this.trailScale);
            float oz = (float) (urandom.nextGaussian() * 0.5D * this.trailScale);

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
                    .color(r, g, b, alpha).uv2(light).endVertex();
            buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(1.0F, 0.0F)
                    .color(r, g, b, alpha).uv2(light).endVertex();
            buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(0.0F, 0.0F)
                    .color(r, g, b, alpha).uv2(light).endVertex();
            buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(0.0F, 1.0F)
                    .color(r, g, b, alpha).uv2(light).endVertex();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }
}
