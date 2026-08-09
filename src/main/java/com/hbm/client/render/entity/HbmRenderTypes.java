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

    private HbmRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType torexCloud(ResourceLocation texture) {
        return create("hbm_torex_cloud", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, BIG_BUFFER,
                false, true,
                CompositeState.builder()
                        .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setWriteMaskState(COLOR_WRITE)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .createCompositeState(false));
    }

    /** SRC_ALPHA / ONE additive flash. */
    public static RenderType torexFlash(ResourceLocation texture) {
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
