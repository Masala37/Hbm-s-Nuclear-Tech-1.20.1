package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.blocks.machine.DummyGridOffsets;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.client.render.entity.RenderMissile;
import com.hbm.client.render.missile.MissilePronter;
import com.hbm.handler.MissileStruct;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy {@code RenderLaunchPad}: silo deck OBJ + standing missile.
 */
public class RenderLaunchPad implements BlockEntityRenderer<LaunchPadBlockEntity> {
    public static final ResourceLocation PAD_MODEL =
            new ResourceLocation(RefStrings.MODID, "block/launch_pad_silo");
    public static final ResourceLocation PAD_RUSTED_MODEL =
            new ResourceLocation(RefStrings.MODID, "block/launch_pad_silo_rusted");

    public static void renderSilo(PoseStack pose, MultiBufferSource buffers, ResourceLocation bakedModel,
                                  int packedLight, int packedOverlay) {
        ObjModelRenderer.render(pose, buffers, bakedModel, packedLight, packedOverlay);
    }

    public RenderLaunchPad(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LaunchPadBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(DummyGridOffsets.dummyableYaw(be.getFacing())));
        renderSilo(pose, buffers, PAD_MODEL, light, packedOverlay);

        ItemStack missile = be.getItems().getStackInSlot(LaunchPadBlockEntity.SLOT_MISSILE);
        if (!missile.isEmpty()) {
            pose.translate(0.0D, 1.0D, 0.0D);
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
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LaunchPadBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
