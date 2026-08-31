package com.hbm.client.render.item;

import com.hbm.client.render.missile.MissilePartModels;
import com.hbm.client.render.missile.MissilePronter;
import com.hbm.items.weapon.ItemCustomMissilePart;
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
 * Legacy {@code ItemRenderMissilePart}.
 */
public class MissilePartItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static MissilePartItemRenderer instance;

    public MissilePartItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    public static MissilePartItemRenderer get() {
        if (instance == null) {
            instance = new MissilePartItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                               MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof ItemCustomMissilePart part)) {
            return;
        }
        MissilePartModels.Spec spec = MissilePartModels.get(part);
        if (spec == null) {
            return;
        }
        pose.pushPose();
        if (context == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();
        } else {
            // 1.20 ItemRenderer translates -0.5³ before BEWLR; 1.7.10 IItemRenderer did not.
            pose.translate(0.5F, 0.5F, 0.5F);
        }
        applyTransforms(context, pose, spec, part);
        MissilePronter.renderPart(pose, buffers, part);
        pose.popPose();
    }

    private static void applyTransforms(ItemDisplayContext context, PoseStack pose,
                                        MissilePartModels.Spec spec, ItemCustomMissilePart part) {
        switch (context) {
            case GUI -> {
                double height = spec.guiheight() == 0.0D ? 4.0D : spec.guiheight();
                double size = 10.0D;
                double scale = size / height;
                pose.translate(0.0F, 1.0F, 0.5F);
                pose.scale(1.0F / 16.0F, -1.0F / 16.0F, 1.0F / 16.0F);
                pose.translate(height / 2.0D * scale, 0.0D, 0.0D);
                pose.mulPose(Axis.ZP.rotationDegrees(135.0F));
                pose.mulPose(Axis.XP.rotationDegrees(145.0F));
                if (part.type == ItemCustomMissilePart.PartType.WARHEAD) {
                    pose.translate(0.0D, height / 8.0D * scale, 0.0D);
                }
                if (part.type == ItemCustomMissilePart.PartType.FUSELAGE) {
                    pose.translate(0.0D, height / 4.0D * scale, 0.0D);
                }
                pose.translate(3.5D, 14.0D, 0.0D);
                pose.scale((float) -scale, (float) -scale, (float) -scale);
                pose.mulPose(Axis.YP.rotationDegrees(-(System.currentTimeMillis() / 25L % 360L)));
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND,
                    THIRD_PERSON_RIGHT_HAND -> {
                pose.translate(0.5F, 0.0F, 0.0F);
                pose.scale(0.4F, 0.4F, 0.4F);
            }
            default -> pose.scale(0.4F, 0.4F, 0.4F);
        }
    }
}
