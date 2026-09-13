package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.ArcWelderBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 1.7.10 {@code RenderArcWelder}.
 */
public class RenderArcWelder implements BlockEntityRenderer<ArcWelderBlockEntity> {
    public static final ResourceLocation MODEL =
            new ResourceLocation(RefStrings.MODID, "block/arc_welder");

    public static List<ResourceLocation> allModels() {
        return List.of(MODEL);
    }

    public RenderArcWelder(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcWelderBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.tesrYaw(DummyableMeta.coreFacing(meta))));
        pose.translate(-0.5D, 0.0D, 0.0D);
        ObjModelRenderer.render(pose, buffers, MODEL, light, packedOverlay);

        ItemStack display = be.getDisplay();
        if (!display.isEmpty()) {
            pose.pushPose();
            pose.translate(0.0625D * 2.5D, 1.125D, 0.0D);
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pose.scale(1.5F, 1.5F, 1.5F);
            ItemRenderer items = Minecraft.getInstance().getItemRenderer();
            items.renderStatic(display, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, be.getLevel(), 0);
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ArcWelderBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
