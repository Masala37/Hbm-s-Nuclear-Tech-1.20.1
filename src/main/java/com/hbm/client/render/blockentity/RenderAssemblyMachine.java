package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.AssemblyMachineBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.inventory.recipes.GenericRecipeMatch;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;

import java.util.List;

/**
 * 1.7.10 {@code RenderAssemblyMachine}: base, optional frame, spinning ring, two arms.
 */
public class RenderAssemblyMachine implements BlockEntityRenderer<AssemblyMachineBlockEntity> {
    public static final ResourceLocation BASE =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_base");
    public static final ResourceLocation FRAME =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_frame");
    public static final ResourceLocation RING =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_ring");
    public static final ResourceLocation ARM_LOWER_1 =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_arm_lower_1");
    public static final ResourceLocation ARM_UPPER_1 =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_arm_upper_1");
    public static final ResourceLocation HEAD_1 =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_head_1");
    public static final ResourceLocation SPIKE_1 =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_spike_1");
    public static final ResourceLocation ARM_LOWER_2 =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_arm_lower_2");
    public static final ResourceLocation ARM_UPPER_2 =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_arm_upper_2");
    public static final ResourceLocation HEAD_2 =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_head_2");
    public static final ResourceLocation SPIKE_2 =
            new ResourceLocation(RefStrings.MODID, "block/assembly_machine_spike_2");

    public static List<ResourceLocation> allModels() {
        return List.of(BASE, FRAME, RING, ARM_LOWER_1, ARM_UPPER_1, HEAD_1, SPIKE_1,
                ARM_LOWER_2, ARM_UPPER_2, HEAD_2, SPIKE_2);
    }

    public RenderAssemblyMachine(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AssemblyMachineBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.tesrYaw(DummyableMeta.coreFacing(meta))));

        ObjModelRenderer.render(pose, buffers, BASE, light, packedOverlay);
        if (be.hasFrame()) {
            ObjModelRenderer.render(pose, buffers, FRAME, light, packedOverlay);
        }

        pose.pushPose();
        double spin = Mth.lerp(partialTick, be.prevRing, be.ring);
        double[] arm1 = be.arms[0].getPositions(partialTick);
        double[] arm2 = be.arms[1].getPositions(partialTick);
        pose.mulPose(Axis.YP.rotationDegrees((float) spin));
        ObjModelRenderer.render(pose, buffers, RING, light, packedOverlay);

        pose.pushPose();
        pose.translate(0.0D, 1.625D, 0.9375D);
        pose.mulPose(Axis.XP.rotationDegrees((float) arm1[0]));
        pose.translate(0.0D, -1.625D, -0.9375D);
        ObjModelRenderer.render(pose, buffers, ARM_LOWER_1, light, packedOverlay);
        pose.translate(0.0D, 2.375D, 0.9375D);
        pose.mulPose(Axis.XP.rotationDegrees((float) arm1[1]));
        pose.translate(0.0D, -2.375D, -0.9375D);
        ObjModelRenderer.render(pose, buffers, ARM_UPPER_1, light, packedOverlay);
        pose.translate(0.0D, 2.375D, 0.4375D);
        pose.mulPose(Axis.XP.rotationDegrees((float) arm1[2]));
        pose.translate(0.0D, -2.375D, -0.4375D);
        ObjModelRenderer.render(pose, buffers, HEAD_1, light, packedOverlay);
        pose.translate(0.0D, arm1[3], 0.0D);
        ObjModelRenderer.render(pose, buffers, SPIKE_1, light, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.translate(0.0D, 1.625D, -0.9375D);
        pose.mulPose(Axis.XP.rotationDegrees((float) -arm2[0]));
        pose.translate(0.0D, -1.625D, 0.9375D);
        ObjModelRenderer.render(pose, buffers, ARM_LOWER_2, light, packedOverlay);
        pose.translate(0.0D, 2.375D, -0.9375D);
        pose.mulPose(Axis.XP.rotationDegrees((float) -arm2[1]));
        pose.translate(0.0D, -2.375D, 0.9375D);
        ObjModelRenderer.render(pose, buffers, ARM_UPPER_2, light, packedOverlay);
        pose.translate(0.0D, 2.375D, -0.4375D);
        pose.mulPose(Axis.XP.rotationDegrees((float) -arm2[2]));
        pose.translate(0.0D, -2.375D, 0.4375D);
        ObjModelRenderer.render(pose, buffers, HEAD_2, light, packedOverlay);
        pose.translate(0.0D, arm2[3], 0.0D);
        ObjModelRenderer.render(pose, buffers, SPIKE_2, light, packedOverlay);
        pose.popPose();

        pose.popPose();

        ItemStack icon = GenericRecipeMatch.icon(be.getRecipe());
        Player player = Minecraft.getInstance().player;
        if (!icon.isEmpty() && player != null
                && player.distanceToSqr(be.getBlockPos().getX() + 0.5D, be.getBlockPos().getY() + 1.0D,
                be.getBlockPos().getZ() + 0.5D) < 35.0D * 35.0D) {
            pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            pose.translate(0.0D, 1.0625D, 0.0D);
            icon = icon.copy();
            icon.setCount(1);
            if (icon.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block.defaultBlockState().getRenderShape() == RenderShape.MODEL) {
                    pose.translate(0.0D, -0.0625D, 0.0D);
                } else {
                    pose.translate(0.0D, -0.125D, 0.0D);
                    pose.scale(0.5F, 0.5F, 0.5F);
                }
            } else {
                pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
                pose.translate(0.0D, -0.25D, 0.0D);
            }
            pose.scale(1.25F, 1.25F, 1.25F);
            ItemRenderer items = Minecraft.getInstance().getItemRenderer();
            items.renderStatic(icon, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, be.getLevel(), 0);
        }

        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(AssemblyMachineBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
