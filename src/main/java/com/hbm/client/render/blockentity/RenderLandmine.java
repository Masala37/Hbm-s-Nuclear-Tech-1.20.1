package com.hbm.client.render.blockentity;

import com.hbm.blockentity.bomb.LandmineBlockEntity;
import com.hbm.blocks.bomb.LandmineBlock;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Legacy {@code RenderLandmine}: center, yaw, type scale/offset, AP biome skins.
 */
public class RenderLandmine implements BlockEntityRenderer<LandmineBlockEntity> {
    private static final ModelResourceLocation AP_GRASS = blockstate("mine_ap");
    private static final ResourceLocation AP_DESERT = id("block/mine_ap_desert");
    private static final ResourceLocation AP_SNOW = id("block/mine_ap_snow");
    private static final ResourceLocation AP_STONE = id("block/mine_ap_stone");

    public RenderLandmine(BlockEntityRendererProvider.Context context) {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(RefStrings.MODID, path);
    }

    private static ModelResourceLocation blockstate(String path) {
        return new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, path), "");
    }

    @Override
    public void render(LandmineBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof LandmineBlock landmine)) {
            return;
        }

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));

        switch (landmine.getMineType()) {
            case AP -> {
                pose.scale(0.375F, 0.375F, 0.375F);
                pose.translate(0.0D, -0.0625D * 3.5D, 0.0D);
                model = resolveApModel(be.getLevel(), be.getBlockPos(), model);
            }
            case HE -> pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case SHRAP -> {
                pose.scale(0.375F, 0.375F, 0.375F);
                pose.translate(0.0D, -0.0625D * 3.5D, 0.0D);
            }
            case FAT -> pose.scale(0.25F, 0.25F, 0.25F);
            case NAVAL -> pose.translate(0.0D, 0.5D, 0.0D);
        }

        for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(42L), ModelData.EMPTY)) {
            dispatcher.getModelRenderer().renderModel(
                    pose.last(),
                    buffers.getBuffer(renderType),
                    state,
                    model,
                    1.0F, 1.0F, 1.0F,
                    packedLight,
                    packedOverlay,
                    ModelData.EMPTY,
                    renderType);
        }
        pose.popPose();
    }

    private static BakedModel resolveApModel(Level level, BlockPos pos, BakedModel fallback) {
        if (level == null) {
            return fallback;
        }

        ResourceLocation skin = null;
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        if (surface > pos.getY() + 2) {
            skin = AP_STONE;
        } else {
            Biome biome = level.getBiome(pos).value();
            if (biome.coldEnoughToSnow(pos)) {
                skin = AP_SNOW;
            } else if (biome.getBaseTemperature() >= 1.5F) {
                skin = AP_DESERT;
            }
        }

        if (skin == null) {
            BakedModel grass = Minecraft.getInstance().getModelManager().getModel(AP_GRASS);
            return grass != null && grass != Minecraft.getInstance().getModelManager().getMissingModel()
                    ? grass : fallback;
        }

        BakedModel model = ObjModelRenderer.get(skin);
        return model != null ? model : fallback;
    }

    @Override
    public boolean shouldRenderOffScreen(LandmineBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
