package com.hbm.client.render.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Custom translucent / additive RenderTypes for Torex billboards.
 * Uses a large vertex buffer — thousands of cloudlets must not flush every 256 verts.
 */
public final class HbmRenderTypes extends RenderType {
    private static final int BIG_BUFFER = 256 * 1024;

    private static final ResourceLocation CLOUDLET =
            new ResourceLocation("hbm", "textures/particle/particle_base.png");
    private static final ResourceLocation FLASH =
            new ResourceLocation("hbm", "textures/particle/flare.png");

    /** Cached so {@code endBatch(type)} can flush the same instance we drew into. */
    public static final RenderType TOREX_CLOUD = createCloud(CLOUDLET);
    public static final RenderType TOREX_FLASH = createFlash(FLASH);

    private HbmRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    @Deprecated
    public static RenderType torexCloud(ResourceLocation texture) {
        return TOREX_CLOUD;
    }

    @Deprecated
    public static RenderType torexFlash(ResourceLocation texture) {
        return TOREX_FLASH;
    }

    private static RenderType createCloud(ResourceLocation texture) {
        return create("hbm_torex_cloud", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, BIG_BUFFER,
                false, true,
                CompositeState.builder()
                        .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        // Write depth so water/sky behind the mushroom fail the depth test
                        // (cloudlets are still back-to-front sorted for blending).
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .createCompositeState(false));
    }

    private static RenderType createFlash(ResourceLocation texture) {
        return create("hbm_torex_flash", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, BIG_BUFFER,
                false, true,
                CompositeState.builder()
                        .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setWriteMaskState(COLOR_WRITE)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .createCompositeState(false));
    }
}
