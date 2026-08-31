package com.hbm.client.render;

import com.hbm.HbmNuclearTechMod;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Draws forge:obj bakes as entities (no block-face shade, no back-face cull).
 * Forge 1.20.1 / 47.4 stores RegisterAdditional bakes under the plain {@link ResourceLocation}.
 */
public final class ObjModelRenderer {
    private static final Set<ResourceLocation> MISSING_WARNED = ConcurrentHashMap.newKeySet();
    private static final Direction[] CULL_FACES = Direction.values();

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

        BakedModel model = models.getModel(modelId);
        if (model != null && model != missing) {
            return model;
        }

        model = models.getModel(standalone(modelId));
        if (model != null && model != missing) {
            return model;
        }

        model = models.getModel(new ModelResourceLocation(modelId, ""));
        if (model != null && model != missing) {
            return model;
        }

        if (MISSING_WARNED.add(modelId)) {
            HbmNuclearTechMod.LOGGER.warn("Missing baked OBJ model: {} (tried plain, #standalone, #)", modelId);
        }
        return null;
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, ResourceLocation modelId,
                              int packedLight, int packedOverlay) {
        BakedModel model = get(modelId);
        if (model == null) {
            return;
        }
        var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        RandomSource random = RandomSource.create();
        random.setSeed(42L);
        emit(pose, buffer, model.getQuads(null, null, random, ModelData.EMPTY, null), packedLight, packedOverlay);
        for (Direction side : CULL_FACES) {
            random.setSeed(42L);
            emit(pose, buffer, model.getQuads(null, side, random, ModelData.EMPTY, null), packedLight, packedOverlay);
        }
    }

    private static void emit(PoseStack pose, VertexConsumer buffer,
                             List<BakedQuad> quads, int packedLight, int packedOverlay) {
        for (BakedQuad quad : quads) {
            buffer.putBulkData(pose.last(), quad,
                    new float[] {1.0F, 1.0F, 1.0F, 1.0F},
                    1.0F, 1.0F, 1.0F,
                    new int[] {packedLight, packedLight, packedLight, packedLight},
                    packedOverlay, false);
        }
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, ResourceLocation modelId, int packedLight) {
        render(pose, buffers, modelId, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
