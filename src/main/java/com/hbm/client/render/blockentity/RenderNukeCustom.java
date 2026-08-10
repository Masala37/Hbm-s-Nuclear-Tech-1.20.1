package com.hbm.client.render.blockentity;

import com.hbm.blockentity.bomb.NukeCustomBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * @deprecated Prefer {@link AssembledNukeRenderer}; kept as a thin Boy-length alias.
 */
@Deprecated
public class RenderNukeCustom extends AssembledNukeRenderer<NukeCustomBlockEntity> {
    public RenderNukeCustom(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
