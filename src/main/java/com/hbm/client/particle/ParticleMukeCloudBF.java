package com.hbm.client.particle;

import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Legacy {@code ParticleMukeCloudBF} — balefire explosion sheet.
 */
@OnlyIn(Dist.CLIENT)
public class ParticleMukeCloudBF extends ParticleMukeCloud {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/particle/explosion_bf.png");

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
            return "HBM_MUKE_CLOUD_BF";
        }
    };

    public ParticleMukeCloudBF(ClientLevel level, double x, double y, double z,
                               double mx, double my, double mz) {
        super(level, x, y, z, mx, my, mz);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }
}
