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
import org.lwjgl.opengl.GL11;

/**
 * Legacy {@code ParticleMukeWave} — expanding horizontal shock ring ({@code shockwave.png}).
 */
@OnlyIn(Dist.CLIENT)
public class ParticleMukeWave extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/particle/shockwave.png");

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
            return "HBM_MUKE_WAVE";
        }
    };

    private float waveScale = 45.0F;
    private int age;
    private int maxAge;

    public ParticleMukeWave(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.maxAge = 25;
        this.hasPhysics = false;
        this.gravity = 0.0F;
    }

    public ParticleMukeWave setup(float scale, int maxAge) {
        this.waveScale = scale;
        this.maxAge = Math.max(1, maxAge);
        return this;
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
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);

        float life = (this.age + partialTicks) / (float) this.maxAge;
        float alpha = 1.0F - life;
        float scale = (1.0F - (float) Math.pow(Math.E, -(this.age + partialTicks) * 0.125D)) * this.waveScale;

        int light = LightTexture.FULL_BRIGHT;
        // Horizontal ring on XZ (legacy flat quad).
        buffer.vertex(px - scale, py - 0.25F, pz - scale).uv(1.0F, 1.0F)
                .color(1.0F, 1.0F, 1.0F, alpha).uv2(light).endVertex();
        buffer.vertex(px - scale, py - 0.25F, pz + scale).uv(1.0F, 0.0F)
                .color(1.0F, 1.0F, 1.0F, alpha).uv2(light).endVertex();
        buffer.vertex(px + scale, py - 0.25F, pz + scale).uv(0.0F, 0.0F)
                .color(1.0F, 1.0F, 1.0F, alpha).uv2(light).endVertex();
        buffer.vertex(px + scale, py - 0.25F, pz - scale).uv(0.0F, 1.0F)
                .color(1.0F, 1.0F, 1.0F, alpha).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }
}
