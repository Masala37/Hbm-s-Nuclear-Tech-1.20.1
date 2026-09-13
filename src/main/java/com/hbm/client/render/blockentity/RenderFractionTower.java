package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.FractionTowerBlockEntity;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 1.7.10 {@code RenderFractionTower}.
 */
public class RenderFractionTower implements BlockEntityRenderer<FractionTowerBlockEntity> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(RefStrings.MODID, "block/fraction_tower");

    public static List<ResourceLocation> allModels() {
        return List.of(MODEL);
    }

    public RenderFractionTower(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FractionTowerBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        ObjModelRenderer.render(pose, buffers, MODEL, light, packedOverlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(FractionTowerBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
