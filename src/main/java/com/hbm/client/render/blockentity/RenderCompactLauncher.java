package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.CompactLauncherBlockEntity;
import com.hbm.client.render.ObjPartModel;
import com.hbm.client.render.missile.MissilePronter;
import com.hbm.handler.MissileStruct;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RenderCompactLauncher implements BlockEntityRenderer<CompactLauncherBlockEntity> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(RefStrings.MODID, "models/obj/compact_launcher.obj");
    public static final ResourceLocation TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/compact_launcher.png");

    public RenderCompactLauncher(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CompactLauncherBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        renderLauncher(pose, buffers, light, packedOverlay);
        MissileStruct load = be.getLoad();
        if (load != null && load.fuselage != null) {
            pose.translate(0.0D, 1.0625D, 0.0D);
            MissilePronter.prontMissile(pose, buffers, load, LightTexture.FULL_BRIGHT);
        }
        pose.popPose();
    }

    public static void renderLauncher(PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ObjPartModel.get(MODEL).renderAll(pose, buffers, TEX, packedLight, packedOverlay);
    }

    public static void renderItem(PoseStack pose, MultiBufferSource buffers) {
        pose.pushPose();
        pose.scale(0.5F, 0.5F, 0.5F);
        ObjPartModel.get(MODEL).renderAll(pose, buffers, TEX, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CompactLauncherBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
