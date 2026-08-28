package com.hbm.client.particle;

import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

/**
 * Legacy {@code ParticleMukeFlash} — additive flare burst that seeds stem/ground/mush clouds at age 15.
 */
@OnlyIn(Dist.CLIENT)
public class ParticleMukeFlash extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/particle/flare.png");

    public static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
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
            return "HBM_MUKE_FLASH";
        }
    };

    private final boolean balefire;
    private int age;
    private final int maxAge = 20;

    public ParticleMukeFlash(ClientLevel level, double x, double y, double z, boolean balefire) {
        super(level, x, y, z);
        this.balefire = balefire;
        this.hasPhysics = false;
        this.gravity = 0.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.age++;
        if (this.age == 15) {
            spawnClouds();
        }
        if (this.age >= this.maxAge) {
            remove();
        }
    }

    private void spawnClouds() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = this.level;
        RandomSource rand = level.random;

        // Stem
        for (double d = 0.0D; d <= 1.8D; d += 0.1D) {
            mc.particleEngine.add(cloud(level, this.x, this.y, this.z,
                    rand.nextGaussian() * 0.05D, d + rand.nextGaussian() * 0.02D, rand.nextGaussian() * 0.05D));
        }
        // Ground
        for (int i = 0; i < 100; i++) {
            mc.particleEngine.add(cloud(level, this.x, this.y + 0.5D, this.z,
                    rand.nextGaussian() * 0.5D, rand.nextInt(5) == 0 ? 0.02D : 0.0D, rand.nextGaussian() * 0.5D));
        }
        // Mush
        for (int i = 0; i < 75; i++) {
            double mx = rand.nextGaussian() * 0.5D;
            double mz = rand.nextGaussian() * 0.5D;
            if (mx * mx + mz * mz > 1.5D) {
                mx *= 0.5D;
                mz *= 0.5D;
            }
            double my = 1.8D + (rand.nextDouble() * 3.0D - 1.5D) * (0.75D - (mx * mx + mz * mz)) * 0.5D;
            mc.particleEngine.add(cloud(level, this.x, this.y, this.z,
                    mx, my + rand.nextGaussian() * 0.02D, mz));
        }
    }

    private ParticleMukeCloud cloud(ClientLevel level, double x, double y, double z,
                                    double mx, double my, double mz) {
        if (this.balefire) {
            return new ParticleMukeCloudBF(level, x, y, z, mx, my, mz);
        }
        return new ParticleMukeCloud(level, x, y, z, mx, my, mz);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cam = camera.getPosition();
        float dX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
        float dY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
        float dZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);

        float alpha = 1.0F - ((this.age + partialTicks) / (float) this.maxAge);
        float scale = (this.age + partialTicks) * 3.0F + 1.0F;
        int light = LightTexture.FULL_BRIGHT;

        // Legacy draws 24 additive billboards with seeded offsets (camera axes approximated via camera rotation).
        var rotation = camera.rotation();
        RandomSource seeded = RandomSource.create();
        for (int i = 0; i < 24; i++) {
            seeded.setSeed(i * 31L + 1L);
            float pX = (float) (dX + seeded.nextDouble() * 15.0D - 7.5D);
            float pY = (float) (dY + seeded.nextDouble() * 7.5D - 3.75D);
            float pZ = (float) (dZ + seeded.nextDouble() * 15.0D - 7.5D);

            org.joml.Vector3f[] corners = new org.joml.Vector3f[]{
                    new org.joml.Vector3f(-1.0F, -1.0F, 0.0F),
                    new org.joml.Vector3f(-1.0F, 1.0F, 0.0F),
                    new org.joml.Vector3f(1.0F, 1.0F, 0.0F),
                    new org.joml.Vector3f(1.0F, -1.0F, 0.0F)
            };
            for (org.joml.Vector3f corner : corners) {
                corner.rotate(rotation);
                corner.mul(scale);
                corner.add(pX, pY, pZ);
            }
            float r = 1.0F;
            float g = 0.9F;
            float b = 0.75F;
            float a = alpha * 0.5F;
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
