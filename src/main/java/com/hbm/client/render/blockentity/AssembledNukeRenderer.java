package com.hbm.client.render.blockentity;

import com.hbm.blocks.bomb.AssembledNukeBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Shared TESR/BER for assembled nukes (legacy {@code RenderNuke*}).
 * Centers on the origin cell, applies per-type yaw, optional Boy-length {@code -2} local X,
 * then draws the unrotated forge:obj baked model.
 */
public class AssembledNukeRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    public AssembledNukeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof AssembledNukeBlock)) {
            return;
        }

        Direction facing = state.getValue(AssembledNukeBlock.FACING);
        AssembledNukeRenderProfile profile = AssembledNukeRenderProfile.of(state.getBlock());
        float yaw = profile.baseYaw() + profile.facingYaw(facing);
        double offsetX = profile.localOffsetX();

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        if (offsetX != 0.0D) {
            pose.translate(offsetX, 0.0D, 0.0D);
        }

        // Blockstates keep an unrotated model; BER owns facing (matches Custom).
        BlockState unrotated = state.setValue(AssembledNukeBlock.FACING, Direction.NORTH);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(unrotated);
        for (RenderType renderType : model.getRenderTypes(unrotated, RandomSource.create(42L), ModelData.EMPTY)) {
            dispatcher.getModelRenderer().renderModel(
                    pose.last(),
                    buffers.getBuffer(renderType),
                    unrotated,
                    model,
                    1.0F, 1.0F, 1.0F,
                    packedLight,
                    packedOverlay,
                    ModelData.EMPTY,
                    renderType);
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(T be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
