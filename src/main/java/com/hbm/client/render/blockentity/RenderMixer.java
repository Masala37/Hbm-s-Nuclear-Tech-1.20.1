package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.MixerBlockEntity;
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
 * 1.7.10 {@code RenderMixer}: Main + spinning Mixer + scaled Fluid tinted with the output type.
 */
public class RenderMixer implements BlockEntityRenderer<MixerBlockEntity> {
    public static final ResourceLocation MAIN =
            new ResourceLocation(RefStrings.MODID, "block/mixer_main");
    public static final ResourceLocation BLADES =
            new ResourceLocation(RefStrings.MODID, "block/mixer_blades");
    public static final ResourceLocation FLUID =
            new ResourceLocation(RefStrings.MODID, "block/mixer_fluid");

    public static List<ResourceLocation> allModels() {
        return List.of(MAIN, BLADES, FLUID);
    }

    public RenderMixer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MixerBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.tesrYaw(DummyableMeta.coreFacing(meta))));
        ObjModelRenderer.render(pose, buffers, MAIN, light, packedOverlay);

        pose.pushPose();
        pose.mulPose(Axis.YN.rotationDegrees(be.spinAngle(partialTick)));
        ObjModelRenderer.render(pose, buffers, BLADES, light, packedOverlay);
        pose.popPose();

        int totalFill = be.totalFill();
        int totalMax = be.totalMax();
        if (totalFill > 0 && totalMax > 0) {
            FluidStack fluid = be.tankView(2);
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
            int color = extensions.getTintColor(fluid);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            pose.pushPose();
            pose.translate(0.0D, 1.0D, 0.0D);
            pose.scale(1.0F, (float) (totalFill / (double) totalMax * 0.99D), 1.0F);
            pose.translate(0.0D, -1.0D, 0.0D);
            ObjModelRenderer.renderColored(pose, buffers, FLUID, light, packedOverlay, r, g, b, 0.75F);
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(MixerBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
