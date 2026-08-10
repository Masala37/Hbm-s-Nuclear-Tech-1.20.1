package com.hbm.client.render.blockentity;

import com.hbm.blockentity.bomb.CrashedBombBlockEntity;
import com.hbm.blocks.bomb.CrashedBombBlock;
import com.hbm.blocks.bomb.DudType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Seeded crash pose (legacy {@code RenderCrashedBomb} TESR).
 */
public class RenderCrashedBomb implements BlockEntityRenderer<CrashedBombBlockEntity> {
    public RenderCrashedBomb(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrashedBombBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof CrashedBombBlock)) {
            return;
        }

        DudType type = state.getValue(CrashedBombBlock.TYPE);
        RandomSource rand = RandomSource.create(be.getBlockPos().asLong());
        double yaw = rand.nextDouble() * 360.0D;
        double pitch = rand.nextDouble() * 45.0D + 45.0D;
        double roll = rand.nextDouble() * 360.0D;
        double offset = rand.nextDouble() * 2.0D - 1.0D;

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees((float) yaw));
        pose.mulPose(Axis.XP.rotationDegrees((float) pitch));
        pose.mulPose(Axis.ZP.rotationDegrees((float) roll));
        pose.translate(0.0D, 0.0D, -offset);

        if (type == DudType.NUKE) {
            pose.translate(0.0D, 0.0D, 1.25D);
        } else if (type == DudType.SALTED) {
            pose.translate(0.0D, 0.0D, 0.5D);
        }

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
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

    @Override
    public boolean shouldRenderOffScreen(CrashedBombBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
