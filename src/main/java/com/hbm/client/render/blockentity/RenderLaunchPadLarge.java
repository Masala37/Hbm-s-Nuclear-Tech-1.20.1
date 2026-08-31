package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.blockentity.machine.LaunchPadLargeBlockEntity;
import com.hbm.blocks.machine.DummyGridOffsets;
import com.hbm.client.render.ObjPartModel;
import com.hbm.client.render.entity.RenderMissile;
import com.hbm.client.render.missile.MissilePronter;
import com.hbm.handler.LaunchPadFormFactor;
import com.hbm.handler.MissileStruct;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy {@code RenderLaunchPadLarge}: pad + form-factor erector + standing missile.
 */
public class RenderLaunchPadLarge implements BlockEntityRenderer<LaunchPadLargeBlockEntity> {
    public static final ResourceLocation PAD_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/launchpad/pad.png");

    public RenderLaunchPadLarge(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LaunchPadLargeBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        ObjPartModel model = ObjPartModel.get(ObjPartModel.erectorObj());

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(DummyGridOffsets.dummyableYaw(be.getFacing())));
        model.render(pose, buffers, PAD_TEX, "Pad", light, packedOverlay);

        ItemStack missile = be.getItems().getStackInSlot(LaunchPadBlockEntity.SLOT_MISSILE);
        LaunchPadFormFactor form = be.formFactor >= 0 && be.formFactor < LaunchPadFormFactor.values().length
                ? LaunchPadFormFactor.values()[be.formFactor]
                : (missile.isEmpty() ? null : LaunchPadFormFactor.of(missile));

        if (form != null) {
            ResourceLocation tex = new ResourceLocation(RefStrings.MODID,
                    "textures/models/launchpad/" + form.texture + ".png");
            float erectorAngle = be.prevErector + (be.erector - be.prevErector) * partialTick;
            float erectorLift = be.prevLift + (be.lift - be.prevLift) * partialTick;

            pose.pushPose();
            model.render(pose, buffers, tex, form.padPart, light, packedOverlay);
            if (!missile.isEmpty() && be.erected) {
                model.render(pose, buffers, tex, form.ropePart, light, packedOverlay);
            }
            pose.translate(0.0D, form.offsetY, -form.offsetZ);
            pose.mulPose(Axis.XP.rotationDegrees(-erectorAngle));
            pose.translate(0.0D, -form.offsetY, form.offsetZ);
            model.render(pose, buffers, tex, form.pivotPart, light, packedOverlay);
            pose.translate(0.0D, erectorLift, 0.0D);
            model.render(pose, buffers, tex, form.erectorPart, light, packedOverlay);

            boolean showMissile = !missile.isEmpty() && (be.erected || be.readyToLoad);
            if (be.erected) {
                pose.popPose();
                pose.pushPose();
            }
            if (showMissile) {
                pose.translate(0.0D, 2.0D, 0.0D);
                renderMissile(pose, buffers, missile);
            }
            pose.popPose();
        }
        pose.popPose();
    }

    public static void renderItem(PoseStack pose, MultiBufferSource buffers) {
        ObjPartModel model = ObjPartModel.get(ObjPartModel.erectorObj());
        LaunchPadFormFactor form = LaunchPadFormFactor.ATLAS;
        ResourceLocation atlas = new ResourceLocation(RefStrings.MODID,
                "textures/models/launchpad/" + form.texture + ".png");
        model.render(pose, buffers, PAD_TEX, "Pad", LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        model.render(pose, buffers, atlas, form.padPart, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        model.render(pose, buffers, atlas, form.erectorPart, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        model.render(pose, buffers, atlas, form.pivotPart, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    private static void renderMissile(PoseStack pose, MultiBufferSource buffers, ItemStack missile) {
        if (missile.getItem() instanceof ItemCustomMissile) {
            MissileStruct struct = ItemCustomMissile.getStruct(missile);
            MissilePronter.prontMissile(pose, buffers, struct, LightTexture.FULL_BRIGHT);
        } else {
            RenderMissile.renderStanding(
                    pose, buffers,
                    RenderMissile.modelForItem(missile),
                    LightTexture.FULL_BRIGHT,
                    RenderMissile.standingScale(missile));
        }
    }

    @Override
    public boolean shouldRenderOffScreen(LaunchPadLargeBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
