package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.FelBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * 1.7.10 {@code RenderFEL}: custom yaw (not tesrYaw) plus a solid beam along −Z.
 */
public class RenderFel implements BlockEntityRenderer<FelBlockEntity> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(RefStrings.MODID, "block/fel");

    public static List<ResourceLocation> allModels() {
        return List.of(MODEL);
    }

    public RenderFel(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FelBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.felYaw(DummyableMeta.coreFacing(meta))));
        ObjModelRenderer.render(pose, buffers, MODEL, light, packedOverlay);

        int length = be.getDistance() - 3;
        if (be.beamVisible() && length > 0) {
            int color = beamColor(be);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            pose.translate(0.0D, 1.5D, -1.5D);
            LevelRenderer.renderLineBox(pose, buffers.getBuffer(RenderType.lines()),
                    -0.0625D, -0.0625D, -length - 1.0D, 0.0625D, 0.0625D, 0.0D,
                    r, g, b, 1.0F);
        }
        pose.popPose();
    }

    private static int beamColor(FelBlockEntity be) {
        EnumWavelengths mode = be.getMode();
        if (mode.renderedBeamColor == 0) {
            long time = be.getLevel() == null ? 0 : be.getLevel().getGameTime();
            return Mth.hsvToRgb(Mth.frac(time / 50.0F), 0.5F, 0.1F);
        }
        return mode.renderedBeamColor;
    }

    @Override
    public boolean shouldRenderOffScreen(FelBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
