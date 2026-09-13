package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.RadioRecBlockEntity;
import com.hbm.blocks.machine.RadioRecBlock;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** Legacy {@code RenderDecoBlock} radio receiver ({@code ModelBroadcaster} + {@code ModelRadioReceiver.png}). */
public class RenderRadioRec implements BlockEntityRenderer<RadioRecBlockEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/models/radio_receiver.png");

    private final ModelPart model;

    public RenderRadioRec(BlockEntityRendererProvider.Context context) {
        this.model = RenderBroadcaster.createModel();
    }

    @Override
    public void render(RadioRecBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        pose.pushPose();
        pose.translate(0.5D, 1.5D, 0.5D);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));

        BlockState state = be.getBlockState();
        Direction facing = state.hasProperty(RadioRecBlock.FACING)
                ? state.getValue(RadioRecBlock.FACING)
                : Direction.SOUTH;
        float yaw = switch (facing) {
            case WEST -> 90.0F;
            case NORTH -> 180.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
        pose.mulPose(Axis.YP.rotationDegrees(yaw));

        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.render(pose, consumer, packedLight, packedOverlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(RadioRecBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
