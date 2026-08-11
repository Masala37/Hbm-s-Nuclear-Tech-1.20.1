package com.hbm.client.render;

import com.hbm.HbmNuclearTechMod;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

/**
 * Shared helper for rendering forge:obj models registered via {@code ModelEvent.RegisterAdditional}
 * or blockstates (nukes / mines / missiles / silo).
 * <p>
 * Forge 1.20.1 / 47.4 stores RegisterAdditional bakes under the plain {@link ResourceLocation},
 * not {@code #standalone}. Looking up only MRL keys returns the missing model and draws nothing.
 */
public final class ObjModelRenderer {
    private ObjModelRenderer() {
    }

    public static ModelResourceLocation standalone(String path) {
        return new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, path), "standalone");
    }

    public static ModelResourceLocation standalone(ResourceLocation id) {
        return new ModelResourceLocation(id, "standalone");
    }

    @Nullable
    public static BakedModel get(ResourceLocation modelId) {
        ModelManager models = Minecraft.getInstance().getModelManager();
        BakedModel missing = models.getMissingModel();

        // Forge 47.4 RegisterAdditional: baked under plain ResourceLocation
        BakedModel model = models.getModel(modelId);
        if (model != null && model != missing) {
            return model;
        }

        // Older docs / some loaders: #standalone
        model = models.getModel(standalone(modelId));
        if (model != null && model != missing) {
            return model;
        }

        // Blockstate empty variant
        model = models.getModel(new ModelResourceLocation(modelId, ""));
        if (model != null && model != missing) {
            return model;
        }

        HbmNuclearTechMod.LOGGER.warn("Missing baked OBJ model: {} (tried plain, #standalone, #)", modelId);
        return null;
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, ResourceLocation modelId,
                              int packedLight, int packedOverlay) {
        BakedModel model = get(modelId);
        if (model == null) {
            return;
        }
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BlockState state = Blocks.AIR.defaultBlockState();
        RandomSource random = RandomSource.create(42L);
        for (RenderType type : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            dispatcher.getModelRenderer().renderModel(
                    pose.last(),
                    buffers.getBuffer(type),
                    state,
                    model,
                    1.0F, 1.0F, 1.0F,
                    packedLight,
                    packedOverlay,
                    ModelData.EMPTY,
                    type);
        }
    }
}
