package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.CrystallizerBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/**
 * 1.7.10 {@code RenderCrystallizer}: Body + spinning Spinner + blended Fluid while on.
 */
public class RenderCrystallizer implements BlockEntityRenderer<CrystallizerBlockEntity> {
    public static final ResourceLocation BODY =
            new ResourceLocation(RefStrings.MODID, "block/acidizer_body");
    public static final ResourceLocation SPINNER =
            new ResourceLocation(RefStrings.MODID, "block/acidizer_spinner");
    public static final ResourceLocation FLUID =
            new ResourceLocation(RefStrings.MODID, "block/acidizer_fluid");

    public static List<ResourceLocation> allModels() {
        return List.of(BODY, SPINNER, FLUID);
    }

    public RenderCrystallizer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrystallizerBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.tesrYaw(DummyableMeta.coreFacing(meta))));
        ObjModelRenderer.render(pose, buffers, BODY, light, packedOverlay);

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(be.spinAngle(partialTick)));
        ObjModelRenderer.render(pose, buffers, SPINNER, light, packedOverlay);
        pose.popPose();

        if (be.isOn()) {
            FluidStack fluid = be.tankView();
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
            int color = extensions.getTintColor(fluid);
            float a = ((color >> 24) & 0xFF) / 255f;
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            if (a <= 0f) {
                a = 0.65f;
            }
            ObjModelRenderer.renderColored(pose, buffers, FLUID, light, packedOverlay, r, g, b, a);
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CrystallizerBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
