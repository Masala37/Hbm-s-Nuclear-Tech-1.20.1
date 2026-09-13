package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.DieselGeneratorBlockEntity;
import com.hbm.blocks.machine.DieselGeneratorBlock;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 1.7.10 {@code RenderDieselGen}: Generator body plus shaking Engine when fueled.
 */
public class RenderDieselGen implements BlockEntityRenderer<DieselGeneratorBlockEntity> {
    public static final ResourceLocation GENERATOR =
            new ResourceLocation(RefStrings.MODID, "block/dieselgen_generator");
    public static final ResourceLocation ENGINE =
            new ResourceLocation(RefStrings.MODID, "block/dieselgen_engine");

    public static List<ResourceLocation> allModels() {
        return List.of(GENERATOR, ENGINE);
    }

    public RenderDieselGen(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DieselGeneratorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(yaw(be.getBlockState().getValue(DieselGeneratorBlock.FACING))));
        ObjModelRenderer.render(pose, buffers, GENERATOR, light, packedOverlay);

        if (be.hasFuelForRender()) {
            double swingSide = Math.sin(System.currentTimeMillis() / 50.0D) * 0.005D;
            double swingFront = Math.sin(System.currentTimeMillis() / 25.0D) * 0.005D;
            pose.translate(swingFront, 0.0D, swingSide);
        }
        ObjModelRenderer.render(pose, buffers, ENGINE, light, packedOverlay);
        pose.popPose();
    }

    /**
     * 1.7 metadata: south 270, east 0, north 90, west 180.
     */
    private static float yaw(Direction facing) {
        return switch (facing) {
            case SOUTH -> 270.0F;
            case EAST -> 0.0F;
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            default -> 90.0F;
        };
    }

    @Override
    public boolean shouldRenderOffScreen(DieselGeneratorBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
