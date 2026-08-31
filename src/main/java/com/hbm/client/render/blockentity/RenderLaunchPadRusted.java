package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.LaunchPadRustedBlockEntity;
import com.hbm.blocks.machine.DummyGridOffsets;
import com.hbm.client.render.entity.RenderMissile;
import com.hbm.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy {@code RenderLaunchPadRusted}: rusted silo + optional rusted doomsday.
 */
public class RenderLaunchPadRusted implements BlockEntityRenderer<LaunchPadRustedBlockEntity> {
    public RenderLaunchPadRusted(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LaunchPadRustedBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(DummyGridOffsets.dummyableYaw(be.getFacing())));
        RenderLaunchPad.renderSilo(pose, buffers, RenderLaunchPad.PAD_RUSTED_MODEL, light, packedOverlay);
        if (be.isMissileLoaded()) {
            pose.translate(0.0D, 1.0D, 0.0D);
            ItemStack missile = new ItemStack(ModItems.MISSILE_DOOMSDAY_RUSTED.get());
            RenderMissile.renderStanding(
                    pose, buffers,
                    RenderMissile.modelForItem(missile),
                    LightTexture.FULL_BRIGHT,
                    RenderMissile.standingScale(missile));
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LaunchPadRustedBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
