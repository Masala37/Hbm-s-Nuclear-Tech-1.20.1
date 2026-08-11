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

import java.awt.Color;

/**
 * Legacy {@code ParticleExplosionSmall} — rotating HSB fire cloud using {@code particle_base}.
 */
@OnlyIn(Dist.CLIENT)
public class ParticleExplosionSmall extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/particle/particle_base.png");

    public static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
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
            return "HBM_EXPLOSION_SMALL";
        }
    };

    private final float hue;
    private final float baseScale;
    private final int spinSign;
    private int age;
    private int maxAge;
    private float rotation;
    private float rotationO;

    public ParticleExplosionSmall(ClientLevel level, double x, double y, double z,
                                  float scale, float speedMult) {
        super(level, x, y, z);
        this.maxAge = 25 + random.nextInt(10);
        this.baseScale = scale * 0.9F + random.nextFloat() * 0.2F;
        this.hasPhysics = false;
        this.gravity = random.nextFloat() * -0.01F;
        this.xd = random.nextGaussian() * speedMult;
        this.zd = random.nextGaussian() * speedMult;
        this.yd = 0.0D;
        this.hue = 20.0F + random.nextFloat() * 20.0F;
        this.spinSign = (this.hashCode() % 2 == 0) ? 1 : -1;
        Color color = Color.getHSBColor(this.hue / 255.0F, 1.0F, 1.0F);
        this.rCol = color.getRed() / 255.0F;
        this.gCol = color.getGreen() / 255.0F;
        this.bCol = color.getBlue() / 255.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.rotationO = this.rotation;
        this.age++;
        if (this.age >= this.maxAge) {
            remove();
            return;
        }
        float ageScaled = (float) this.age / (float) this.maxAge;
        this.rotation += (1.0F - ageScaled) * 5.0F * this.spinSign;
        this.yd -= this.gravity;
        this.xd *= 0.65D;
        this.zd *= 0.65D;
        move(this.xd, this.yd, this.zd);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cam = camera.getPosition();
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);

        double ageScaled = (this.age + partialTicks) / (double) this.maxAge;
        Color color = Color.getHSBColor(
                this.hue / 255.0F,
                Math.max(1.0F - (float) ageScaled * 2.0F, 0.0F),
                Mth.clamp(1.25F - (float) ageScaled * 2.0F, this.hue * 0.01F - 0.1F, 1.0F));
        float r = color.getRed() / 255.0F;
        float g = color.getGreen() / 255.0F;
        float b = color.getBlue() / 255.0F;
        float a = (float) Math.pow(1.0D - Math.min(ageScaled, 1.0D), 0.25D) * 0.5F;

        float scale = (float) ((0.25D + 1.0D - Math.pow(1.0D - ageScaled, 4.0D)
                + (this.age + partialTicks) * 0.02D) * this.baseScale);

        Quaternionf rotation = new Quaternionf(camera.rotation());
        rotation.rotateZ(Mth.lerp(partialTicks, this.rotationO, this.rotation) * ((float) Math.PI / 180.0F));

        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (Vector3f corner : corners) {
            corner.mul(scale);
            corner.rotate(rotation);
            corner.add(px, py, pz);
        }

        int light = LightTexture.FULL_BRIGHT;
        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(1.0F, 1.0F)
                .color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(1.0F, 0.0F)
                .color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(0.0F, 0.0F)
                .color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(0.0F, 1.0F)
                .color(r, g, b, a).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }
}
