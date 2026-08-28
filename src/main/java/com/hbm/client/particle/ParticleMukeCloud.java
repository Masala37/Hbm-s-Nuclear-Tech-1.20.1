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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

/**
 * Legacy {@code ParticleMukeCloud} — animated explosion.png sheet (5×5).
 */
@OnlyIn(Dist.CLIENT)
public class ParticleMukeCloud extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/particle/explosion.png");

    public static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
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
            return "HBM_MUKE_CLOUD";
        }
    };

    protected final float friction;
    protected int age;
    protected int maxAge;

    public ParticleMukeCloud(ClientLevel level, double x, double y, double z,
                             double mx, double my, double mz) {
        super(level, x, y, z);
        this.xd = mx;
        this.yd = my;
        this.zd = mz;
        this.hasPhysics = true;
        this.gravity = 0.0F;

        if (my > 0.0D) {
            this.friction = 0.9F;
            if (my > 0.1D) {
                this.maxAge = 92 + level.random.nextInt(11) + (int) (my * 20.0D);
            } else {
                this.maxAge = 72 + level.random.nextInt(11);
            }
        } else if (my == 0.0D) {
            this.friction = 0.95F;
            this.maxAge = 52 + level.random.nextInt(11);
        } else {
            this.friction = 0.85F;
            this.maxAge = 122 + level.random.nextInt(31);
            this.age = 80;
        }
    }

    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.age++;
        if (this.age >= this.maxAge - 2) {
            remove();
            return;
        }
        this.yd -= 0.04D * this.gravity;
        move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // Re-bind in case BF subclass uses a different sheet via a custom render type.
        Vec3 cam = camera.getPosition();
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);

        int ageClamped = Math.min(this.age, this.maxAge);
        int texIndex = ageClamped * 25 / Math.max(1, this.maxAge);
        float f0 = 1.0F / 5.0F;
        float uMin = (texIndex % 5) * f0;
        float uMax = uMin + f0;
        float vMin = (texIndex / 5) * f0;
        float vMax = vMin + f0;

        Quaternionf rotation = new Quaternionf(camera.rotation());
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float scale = 3.0F;
        for (Vector3f corner : corners) {
            corner.rotate(rotation);
            corner.mul(scale);
            corner.add(px, py, pz);
        }

        int light = LightTexture.FULL_BRIGHT;
        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(uMax, vMax)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv2(light).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(uMax, vMin)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv2(light).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(uMin, vMin)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv2(light).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(uMin, vMax)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }

    /** Spawn using the correct texture for this cloud type. */
    public static void add(ClientLevel level, ParticleMukeCloud cloud) {
        Minecraft.getInstance().particleEngine.add(cloud);
    }
}
