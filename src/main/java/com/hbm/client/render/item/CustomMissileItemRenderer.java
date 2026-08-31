package com.hbm.client.render.item;

import com.hbm.client.render.missile.MissilePronter;
import com.hbm.handler.MissileStruct;
import com.hbm.items.weapon.ItemCustomMissile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy {@code ItemRenderMissile} for assembled {@code missile_custom}.
 */
public class CustomMissileItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static CustomMissileItemRenderer instance;

    public CustomMissileItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    public static CustomMissileItemRenderer get() {
        if (instance == null) {
            instance = new CustomMissileItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                               MultiBufferSource buffers, int packedLight, int packedOverlay) {
        MissileStruct missile = ItemCustomMissile.getStruct(stack);
        if (missile == null || !missile.isComplete()) {
            return;
        }
        pose.pushPose();
        if (context == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();
        } else {
            pose.translate(0.5F, 0.5F, 0.5F);
        }
        applyTransforms(context, pose, missile);
        MissilePronter.prontMissile(pose, buffers, missile, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }

    private static void applyTransforms(ItemDisplayContext context, PoseStack pose, MissileStruct missile) {
        switch (context) {
            case GUI -> {
                double height = MissilePronter.getHeight(missile);
                if (height == 0.0D) {
                    height = 4.0D;
                }
                double size = 20.0D;
                double scale = size / height;
                pose.translate(0.0F, 1.0F, 0.5F);
                pose.scale(1.0F / 16.0F, -1.0F / 16.0F, 1.0F / 16.0F);
                pose.translate(height / 2.0D * scale, 0.0D, 0.0D);
                pose.mulPose(Axis.ZP.rotationDegrees(135.0F));
                pose.mulPose(Axis.XP.rotationDegrees(215.0F));
                pose.translate(7.0D, 14.0D, 0.0D);
                pose.scale((float) -scale, (float) -scale, (float) -scale);
                pose.mulPose(Axis.YP.rotationDegrees(-(System.currentTimeMillis() / 25L % 360L)));
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND,
                    THIRD_PERSON_RIGHT_HAND, GROUND, FIXED, HEAD -> {
                pose.scale(0.2F, 0.2F, 0.2F);
                pose.translate(2.0D, 0.0D, 0.0D);
            }
            default -> pose.scale(0.2F, 0.2F, 0.2F);
        }
    }
}
