package com.hbm.client.render.entity;

import com.hbm.entity.bomb.PrimedBombEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

public class PrimedBombRenderer extends EntityRenderer<PrimedBombEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public PrimedBombRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(PrimedBombEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        int fuse = entity.getFuse();
        if ((fuse - partialTicks) + 1.0F < 10.0F) {
            float f = 1.0F - ((fuse - partialTicks) + 1.0F) / 10.0F;
            f = Mth.clamp(f, 0.0F, 1.0F);
            f *= f;
            f *= f;
            float scale = 1.0F + f * 0.3F;
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
        }

        blockRenderer.renderSingleBlock(entity.getBombState(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedBombEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
