package com.hbm.client.render.item;

import com.hbm.client.render.ObjModelRenderer;
import com.hbm.client.render.blockentity.RenderCompactLauncher;
import com.hbm.client.render.blockentity.RenderLaunchPad;
import com.hbm.client.render.blockentity.RenderLaunchPadLarge;
import com.hbm.client.render.blockentity.RenderLaunchTable;
import com.hbm.client.render.blockentity.RenderMissileAssembly;
import com.hbm.client.render.blockentity.RenderRadar;
import com.hbm.client.render.blockentity.RenderRadarLarge;
import com.hbm.client.render.blockentity.RenderRadarScreen;
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
        ResourceLocation key = itemKey(stack);
        pose.pushPose();
        if (context == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();
        } else {
            pose.translate(0.5F, 0.5F, 0.5F);
        }

        if (isPath(key, "launch_pad")) {
            applyLaunchPadTransforms(context, pose);
            RenderLaunchPad.renderSilo(pose, buffers, RenderLaunchPad.PAD_MODEL,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        } else if (isPath(key, "launch_pad_rusted")) {
            applyLaunchPadTransforms(context, pose);
            RenderLaunchPad.renderSilo(pose, buffers, RenderLaunchPad.PAD_RUSTED_MODEL,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        } else if (isPath(key, "launch_pad_large")) {
            applyLaunchPadLargeTransforms(context, pose);
            RenderLaunchPadLarge.renderItem(pose, buffers);
        } else if (isPath(key, "machine_radar")) {
            applyRadarDishTransforms(context, pose);
            RenderRadar.renderItem(pose, buffers);
        } else if (isPath(key, "machine_radar_large")) {
            applyRadarLargeTransforms(context, pose);
            RenderRadarLarge.renderItem(pose, buffers);
        } else if (isPath(key, "radar_screen")) {
            applyRadarScreenTransforms(context, pose);
            RenderRadarScreen.renderItem(pose, buffers);
        } else if (isPath(key, "machine_missile_assembly")) {
            applyAssemblyTransforms(context, pose);
            RenderMissileAssembly.renderBenchItem(pose, buffers);
        } else if (isPath(key, "compact_launcher")) {
            applyCompactLauncherTransforms(context, pose);
            RenderCompactLauncher.renderItem(pose, buffers);
        } else if (isPath(key, "launch_table")) {
            applyLaunchTableTransforms(context, pose);
            RenderLaunchTable.renderItem(pose, buffers);
        } else {
            applyMissileTransforms(stack, context, pose);
            ObjModelRenderer.render(pose, buffers, RenderMissile.modelForItem(stack),
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
        pose.popPose();
    }

    private static ResourceLocation itemKey(ItemStack stack) {
        return net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    private static boolean isPath(ResourceLocation key, String path) {
        return key != null && path.equals(key.getPath());
    }

    /**
     * Inventory uses 1.7.10 INVENTORY pixel space. Mesh is nose-along-+Y.
     */
    private static void applyMissileTransforms(ItemStack stack, ItemDisplayContext context, PoseStack pose) {
        MissileItem.GuiTier tier = stack.getItem() instanceof MissileItem mi
                ? mi.getTier()
                : (RenderMissile.isStrongItem(stack) ? MissileItem.GuiTier.TIER2 : MissileItem.GuiTier.TIER1);

        float mesh = tier.meshScale;

        switch (context) {
            case GUI -> {
                // 1.7.10 INVENTORY is slot top-left, +Y down, 1 unit = 1 pixel.
                // 1.20 GUI already Y-flips and scales by 16; this maps that pixel space back.
                pose.translate(0.0F, 1.0F, 0.5F);
                pose.scale(1.0F / 16.0F, -1.0F / 16.0F, 1.0F / 16.0F);
                pose.scale(tier.guiScale, tier.guiScale, tier.guiScale);
                pose.mulPose(Axis.ZP.rotationDegrees(135.0F));
                pose.mulPose(Axis.YP.rotationDegrees((System.currentTimeMillis() / 15L) % 360L));
                pose.translate(0.0F, -16.0F + tier.guiOffset, 0.0F);
                if (mesh != 1.0F) {
                    pose.scale(mesh, mesh, mesh);
                }
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.translate(0.5F, 0.25F, 0.0F);
                pose.scale(0.1F * mesh, 0.1F * mesh, 0.1F * mesh);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                pose.translate(0.5F, -0.25F, 0.0F);
                pose.scale(0.15F * mesh, 0.15F * mesh, 0.15F * mesh);
            }
            default -> pose.scale(0.15F * mesh, 0.15F * mesh, 0.15F * mesh);
        }
    }

    /**
     * 1.7.10 {@code ItemRenderBase} INVENTORY: pixel space (slot top-left, +Y down), then
     * {@code translate(8,10)} → X-30 → Y45 → {@code scale(-1)}.
     */
    private static void applyItemRenderBaseInventory(PoseStack pose) {
        pose.translate(0.0F, 1.0F, 0.5F);
        pose.scale(1.0F / 16.0F, -1.0F / 16.0F, 1.0F / 16.0F);
        pose.translate(8.0F, 10.0F, 0.0F);
        pose.mulPose(Axis.XP.rotationDegrees(-30.0F));
        pose.mulPose(Axis.YP.rotationDegrees(45.0F));
        pose.scale(-1.0F, -1.0F, -1.0F);
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
                pose.scale(0.25F, 0.25F, 0.25F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                pose.scale(0.25F, 0.25F, 0.25F);
            }
            default -> {
                pose.scale(0.375F, 0.375F, 0.375F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
        }
    }

    /**
     * Legacy inventory: translate(0,-3.75) scale(1.625) then common scale(0.5) Y90 Pad+Atlas.
     */
    private static void applyLaunchPadLargeTransforms(ItemDisplayContext context, PoseStack pose) {
        switch (context) {
            case GUI -> {
                pose.translate(0.5F, 0.55F, 0.0F);
                pose.mulPose(Axis.XP.rotationDegrees(30.0F));
                pose.mulPose(Axis.YP.rotationDegrees(225.0F));
                pose.scale(0.11F, 0.11F, 0.11F);
                pose.translate(0.0F, -3.75F, 0.0F);
                pose.scale(1.625F, 1.625F, 1.625F);
                pose.scale(0.5F, 0.5F, 0.5F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.scale(0.12F, 0.12F, 0.12F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                pose.scale(0.12F, 0.12F, 0.12F);
            }
            default -> {
                pose.scale(0.2F, 0.2F, 0.2F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
        }
    }

    private static void applyRadarDishTransforms(ItemDisplayContext context, PoseStack pose) {
        switch (context) {
            case GUI -> {
                applyItemRenderBaseInventory(pose);
                pose.translate(0.0F, -4.0F, 0.0F);
                pose.scale(5.0F, 5.0F, 5.0F);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.scale(0.4F, 0.4F, 0.4F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> pose.scale(0.4F, 0.4F, 0.4F);
            default -> pose.scale(0.5F, 0.5F, 0.5F);
        }
    }

    private static void applyRadarLargeTransforms(ItemDisplayContext context, PoseStack pose) {
        switch (context) {
            case GUI -> {
                applyItemRenderBaseInventory(pose);
                pose.translate(0.0F, -5.0F, 0.0F);
                pose.scale(3.0F, 3.0F, 3.0F);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.scale(0.12F, 0.12F, 0.12F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> pose.scale(0.12F, 0.12F, 0.12F);
            default -> pose.scale(0.18F, 0.18F, 0.18F);
        }
    }

    private static void applyRadarScreenTransforms(ItemDisplayContext context, PoseStack pose) {
        switch (context) {
            case GUI -> {
                applyItemRenderBaseInventory(pose);
                pose.translate(0.0F, -3.0F, 0.0F);
                pose.scale(5.5F, 5.5F, 5.5F);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.scale(0.35F, 0.35F, 0.35F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> pose.scale(0.35F, 0.35F, 0.35F);
            default -> pose.scale(0.4F, 0.4F, 0.4F);
        }
    }

    /**
     * ItemRenderLibrary: translate(0,-2.5) scale(10) after the standard inventory pose.
     */
    private static void applyAssemblyTransforms(ItemDisplayContext context, PoseStack pose) {
        switch (context) {
            case GUI -> {
                pose.translate(0.5F, 0.4F, 0.0F);
                pose.mulPose(Axis.XP.rotationDegrees(30.0F));
                pose.mulPose(Axis.YP.rotationDegrees(225.0F));
                pose.scale(0.65F, 0.65F, 0.65F);
                pose.translate(0.0F, -0.4F, 0.0F);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.scale(0.25F, 0.25F, 0.25F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                pose.scale(0.25F, 0.25F, 0.25F);
            }
            default -> {
                pose.scale(0.375F, 0.375F, 0.375F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
        }
    }

    private static void applyCompactLauncherTransforms(ItemDisplayContext context, PoseStack pose) {
        switch (context) {
            case GUI -> {
                applyItemRenderBaseInventory(pose);
                pose.translate(0.0F, -4.0F, 0.0F);
                pose.scale(3.5F, 3.5F, 3.5F);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.scale(0.2F, 0.2F, 0.2F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> pose.scale(0.2F, 0.2F, 0.2F);
            default -> pose.scale(0.25F, 0.25F, 0.25F);
        }
    }

    private static void applyLaunchTableTransforms(ItemDisplayContext context, PoseStack pose) {
        switch (context) {
            case GUI -> {
                applyItemRenderBaseInventory(pose);
                pose.translate(0.0F, -2.0F, 0.0F);
                pose.scale(2.5F, 2.5F, 2.5F);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                pose.scale(0.08F, 0.08F, 0.08F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> pose.scale(0.08F, 0.08F, 0.08F);
            default -> pose.scale(0.12F, 0.12F, 0.12F);
        }
    }
}
