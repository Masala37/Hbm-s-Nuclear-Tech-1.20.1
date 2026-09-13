package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.BroadcasterBlockEntity;
import com.hbm.blocks.machine.PinkCloudBroadcasterBlock;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Legacy {@code RenderDecoBlock} + {@code ModelBroadcaster}.
 */
public class RenderBroadcaster implements BlockEntityRenderer<BroadcasterBlockEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/models/broadcaster.png");

    private final ModelPart model;

    public RenderBroadcaster(BlockEntityRendererProvider.Context context) {
        this.model = createModel();
    }

    static ModelPart createModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 0).mirror(true)
                .addBox(-7.0F, 14.0F, -4.0F, 14.0F, 10.0F, 8.0F), PartPose.ZERO);
        root.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(4, 21).mirror(true)
                .addBox(-5.0F, 11.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(0, 18).mirror(true)
                .addBox(-4.5F, 0.0F, -0.5F, 1.0F, 11.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("shape4", CubeListBuilder.create().texOffs(4, 18).mirror(true)
                .addBox(2.0F, 12.0F, -0.5F, 3.0F, 2.0F, 1.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }

    @Override
    public void render(BroadcasterBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        pose.pushPose();
        pose.translate(0.5D, 1.5D, 0.5D);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));

        BlockState state = be.getBlockState();
        Direction facing = state.hasProperty(PinkCloudBroadcasterBlock.FACING)
                ? state.getValue(PinkCloudBroadcasterBlock.FACING)
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
    public boolean shouldRenderOffScreen(BroadcasterBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
