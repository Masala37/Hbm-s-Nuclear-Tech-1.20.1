package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.DecoLootBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** 1.7 {@code RenderLoot}: stored stacks lying on the floor. */
public class RenderLoot implements BlockEntityRenderer<DecoLootBlockEntity> {
    public RenderLoot(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DecoLootBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemRenderer items = Minecraft.getInstance().getItemRenderer();
        int i = 0;
        for (DecoLootBlockEntity.Entry entry : be.items()) {
            ItemStack stack = entry.stack();
            if (stack.isEmpty()) {
                continue;
            }
            pose.pushPose();
            pose.translate(entry.x(), entry.y(), entry.z());
            pose.translate(0.25D, 0.0D, 0.25D);
            pose.scale(0.5F, 0.5F, 0.5F);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            items.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                    pose, buffers, be.getLevel(), i);
            pose.popPose();
            i++;
        }
    }

    @Override
    public boolean shouldRenderOffScreen(DecoLootBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
