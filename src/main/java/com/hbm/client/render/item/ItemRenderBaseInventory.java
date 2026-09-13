package com.hbm.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 1.7.10 {@code ItemRenderBase} inventory / equipped pose, mapped into 1.20 item space.
 * 1.7 INVENTORY is slot pixel space (top-left, +Y down). 1.20 GUI already Y-flips and
 * scales by 16; {@link #applyInventory} undoes that then reapplies translate(8,10) →
 * X-30 → Y45 → scale(-1).
 */
public final class ItemRenderBaseInventory {
    private ItemRenderBaseInventory() {
    }

    public static void applyInventory(PoseStack pose) {
        pose.translate(0.0F, 1.0F, 0.5F);
        pose.scale(1.0F / 16.0F, -1.0F / 16.0F, 1.0F / 16.0F);
        pose.translate(8.0F, 10.0F, 0.0F);
        pose.mulPose(Axis.XP.rotationDegrees(-30.0F));
        pose.mulPose(Axis.YP.rotationDegrees(45.0F));
        pose.scale(-1.0F, -1.0F, -1.0F);
    }

    /**
     * Caller has already translated (0.5, 0.5, 0.5) for non-GUI ISTER space.
     * 1.7: equipped translate(0.5, 0.25, 0); entity scale 1.5; then scale 0.25;
     * Y90 unless {@code EQUIPPED} (third-person).
     */
    public static void applyNonInventory(ItemDisplayContext context, PoseStack pose) {
        if (context == ItemDisplayContext.GROUND) {
            pose.scale(1.5F, 1.5F, 1.5F);
        } else {
            pose.translate(0.0F, -0.25F, -0.5F);
        }
        pose.scale(0.25F, 0.25F, 0.25F);
        if (context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
    }
}
