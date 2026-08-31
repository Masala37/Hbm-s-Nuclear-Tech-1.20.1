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

@OnlyIn(Dist.CLIENT)
public class ParticleRBMKMush extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/particle/rbmk_mush.png");
    private static final int FRAMES = 30;

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
            return "HBM_RBMK_MUSH";
        }
    };

    private final float mushScale;
    private int age;
    private int maxAge;

    public ParticleRBMKMush(ClientLevel level, double x, double y, double z, float scale) {
        super(level, x, y, z);
        this.mushScale = scale;
        this.maxAge = 50;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.setSize(scale * 2.0F, scale * 2.0F);
    }

    @Override
    public boolean shouldCull() {
        return false;
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
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y) + this.mushScale;
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);

        int prog = Math.min(FRAMES - 1, this.age * FRAMES / Math.max(1, this.maxAge));
        float frame = 1.0F / FRAMES;
        float vMin = prog * frame;
        float vMax = (prog + 1) * frame;

        Quaternionf rotation = new Quaternionf(camera.rotation());
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (Vector3f corner : corners) {
            corner.mul(this.mushScale);
            corner.rotate(rotation);
            corner.add(px, py, pz);
        }

        int light = LightTexture.FULL_BRIGHT;
        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(1.0F, vMax)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv2(light).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(1.0F, vMin)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv2(light).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(0.0F, vMin)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv2(light).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(0.0F, vMax)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }
}
