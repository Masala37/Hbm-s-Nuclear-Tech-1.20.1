package com.hbm.client.render.entity;

import com.hbm.entity.projectile.EntityRubble;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tumbling block-chunk renderer for {@link EntityRubble} (legacy {@code RenderRubble} stand-in).
 */
public class RenderRubble extends EntityRenderer<EntityRubble> {
    private final BlockRenderDispatcher blockRenderer;

    public RenderRubble(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.3F;
    }

    @Override
    public void render(EntityRubble entity, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffer, int light) {
        BlockState state = entity.getBlockState();
        pose.pushPose();
        pose.translate(0.0D, 0.15D, 0.0D);
        float spin = (entity.tickCount + partial) * 10.0F;
        pose.mulPose(Axis.XP.rotationDegrees(spin));
        pose.mulPose(Axis.YP.rotationDegrees(spin * 0.7F));
        pose.mulPose(Axis.ZP.rotationDegrees(spin * 0.4F));
        pose.scale(0.55F, 0.55F, 0.55F);
        pose.translate(-0.5D, -0.5D, -0.5D);
        blockRenderer.renderSingleBlock(state, pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, yaw, partial, pose, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRubble entity) {
        return new ResourceLocation("minecraft", "textures/block/stone.png");
    }
}
