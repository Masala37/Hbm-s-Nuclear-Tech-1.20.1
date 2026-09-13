package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.FileCabinetBlockEntity;
import com.hbm.blocks.generic.FileCabinetBlock;
import com.hbm.client.render.ObjPartModel;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** 1.7 {@code RenderFileCabinet}: cabinet + sliding drawers. */
public class RenderFileCabinet implements BlockEntityRenderer<FileCabinetBlockEntity> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(RefStrings.MODID, "models/obj/file_cabinet.obj");
    public static final ResourceLocation TEX_GREEN =
            new ResourceLocation(RefStrings.MODID, "textures/models/file_cabinet.png");
    public static final ResourceLocation TEX_STEEL =
            new ResourceLocation(RefStrings.MODID, "textures/models/file_cabinet_steel.png");

    public RenderFileCabinet(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FileCabinetBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        boolean steel = state.hasProperty(FileCabinetBlock.STEEL) && state.getValue(FileCabinetBlock.STEEL);
        Direction facing = state.hasProperty(FileCabinetBlock.FACING)
                ? state.getValue(FileCabinetBlock.FACING) : Direction.NORTH;
        ResourceLocation texture = steel ? TEX_STEEL : TEX_GREEN;
        ObjPartModel model = ObjPartModel.get(MODEL);

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(yaw(facing)));
        model.render(pose, buffers, texture, "Cabinet", packedLight, packedOverlay);

        pose.pushPose();
        pose.translate(0.0D, 0.0D, 0.6875D * be.getLower(partialTick));
        model.render(pose, buffers, texture, "LowerDrawer", packedLight, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.translate(0.0D, 0.0D, 0.6875D * be.getUpper(partialTick));
        model.render(pose, buffers, texture, "UpperDrawer", packedLight, packedOverlay);
        pose.popPose();

        pose.popPose();
    }

    /** 1.7 TESR: north 180, south 0, west 270, east 90. */
    private static float yaw(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 180.0F;
        };
    }

    @Override
    public boolean shouldRenderOffScreen(FileCabinetBlockEntity be) {
        return true;
    }
}
