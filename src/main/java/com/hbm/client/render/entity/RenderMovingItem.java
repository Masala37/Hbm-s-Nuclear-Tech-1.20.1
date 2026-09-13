package com.hbm.client.render.entity;

import com.hbm.entity.item.EntityMovingItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 1.7.10 {@code RenderMovingItem}: item lying flat on the belt.
 */
public class RenderMovingItem extends EntityRenderer<EntityMovingItem> {
    private final ItemRenderer itemRenderer;

    public RenderMovingItem(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(EntityMovingItem entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        pose.pushPose();
        RandomSource rand = RandomSource.create(entity.getId());
        pose.translate(0.0D, rand.nextDouble() * 0.0625D, 0.0D);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.translate(0.0D, -0.1875D, 0.0D);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                pose, buffer, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMovingItem entity) {
        return new ResourceLocation("minecraft", "textures/atlas/blocks.png");
    }
}
