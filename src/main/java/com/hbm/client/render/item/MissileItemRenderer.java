package com.hbm.client.render.item;

import com.hbm.client.render.ObjModelRenderer;
import com.hbm.client.render.blockentity.RenderLaunchPad;
import com.hbm.client.render.entity.RenderMissile;
import com.hbm.items.weapon.MissileItem;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy {@code ItemRenderMissileGeneric} + {@code ItemRenderLibrary} launch-pad inventory.
 * Item JSON display transforms must be identity so these poses are not double-rotated.
 */
public class MissileItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static MissileItemRenderer instance;

    public MissileItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    public static MissileItemRenderer get() {
        if (instance == null) {
            instance = new MissileItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        boolean isPad = isLaunchPad(stack);

        pose.pushPose();
        if (context == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();
        }

        if (isPad) {
            applyLaunchPadTransforms(context, pose);
            ObjModelRenderer.render(pose, buffers, RenderLaunchPad.PAD_MODEL,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        } else {
            applyMissileTransforms(stack, context, pose);
            ObjModelRenderer.render(pose, buffers, RenderMissile.modelForItem(stack),
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }

        if (context == ItemDisplayContext.GUI) {
            Lighting.setupFor3DItems();
        }
        pose.popPose();
    }

    private static boolean isLaunchPad(ItemStack stack) {
        ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && key.getPath().equals("launch_pad");
    }

    /**
     * Legacy inventory (pixel space): scale → Z tip → Y spin → translate Y.
     * Mesh is nose-along-+Y, height ~0..7.
     */
    private static void applyMissileTransforms(ItemStack stack, ItemDisplayContext context, PoseStack pose) {
        MissileItem.GuiTier tier = stack.getItem() instanceof MissileItem mi
                ? mi.getTier()
                : (RenderMissile.isStrongItem(stack) ? MissileItem.GuiTier.TIER2 : MissileItem.GuiTier.TIER1);

        float mesh = tier.meshScale;

        switch (context) {
            case GUI -> {
                // Slot is ~1 unit; identity JSON display.
                // ZP(45) maps +Y nose to top-left (legacy used 135; that is bottom-left in 1.20 GUI).
                pose.translate(0.5F, 0.5F, 0.0F);
                float s = 0.085F * tier.guiScale;
                pose.scale(s, s, s);
                pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
                pose.mulPose(Axis.YP.rotationDegrees((System.currentTimeMillis() / 15L) % 360L));
                // Center body: height ≈7, legacy used -16+guiOffset in 16px units
                pose.translate(0.0F, -3.5F + (tier.guiOffset - 8.0F) * 0.25F, 0.0F);
                if (mesh != 1.0F) {
                    pose.scale(mesh, mesh, mesh);
                }
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.translate(0.4F, 0.35F, 0.2F);
                pose.scale(0.08F * mesh, 0.08F * mesh, 0.08F * mesh);
                pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
                pose.translate(0.0F, -1.0F, 0.0F);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                pose.translate(0.3F, 0.15F, 0.15F);
                pose.scale(0.12F * mesh, 0.12F * mesh, 0.12F * mesh);
                pose.translate(0.0F, -1.0F, 0.0F);
            }
            case GROUND, FIXED, HEAD -> {
                pose.translate(0.5F, 0.0F, 0.5F);
                pose.scale(0.12F * mesh, 0.12F * mesh, 0.12F * mesh);
            }
            default -> {
                pose.translate(0.5F, 0.0F, 0.5F);
                pose.scale(0.15F * mesh, 0.15F * mesh, 0.15F * mesh);
            }
        }
    }

    /**
     * Legacy {@code ItemRenderBase} inventory: translate(8,10) → X-30 → Y45 → scale(-1,-1,-1),
     * then pad {@code translate(0,-1); scale(3)}.
     */
    private static void applyLaunchPadTransforms(ItemDisplayContext context, PoseStack pose) {
        switch (context) {
            case GUI -> {
                pose.translate(0.5F, 0.45F, 0.0F);
                pose.mulPose(Axis.XP.rotationDegrees(30.0F));
                pose.mulPose(Axis.YP.rotationDegrees(225.0F));
                // Silo is 3×1×3; fit in slot
                pose.scale(0.22F, 0.22F, 0.22F);
                pose.translate(0.0F, -0.5F, 0.0F);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.translate(0.5F, 0.3F, 0.4F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                pose.scale(0.2F, 0.2F, 0.2F);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                pose.translate(0.25F, 0.2F, 0.25F);
                pose.scale(0.2F, 0.2F, 0.2F);
            }
            case GROUND, FIXED -> {
                pose.translate(0.5F, 0.0F, 0.5F);
                pose.scale(0.25F, 0.25F, 0.25F);
            }
            default -> {
                pose.translate(0.5F, 0.0F, 0.5F);
                pose.scale(0.3F, 0.3F, 0.3F);
            }
        }
    }
}
