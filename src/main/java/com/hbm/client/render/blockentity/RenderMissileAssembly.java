package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.MissileAssemblyBlockEntity;
import com.hbm.blocks.machine.MissileAssemblyBlock;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.client.render.missile.MissilePronter;
import com.hbm.handler.MissileStruct;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class RenderMissileAssembly implements BlockEntityRenderer<MissileAssemblyBlockEntity> {
    public static final ResourceLocation BENCH_MODEL =
            new ResourceLocation(RefStrings.MODID, "block/machine_missile_assembly");
    public static final ResourceLocation STRUT_MODEL =
            new ResourceLocation(RefStrings.MODID, "block/strut");

    public RenderMissileAssembly(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MissileAssemblyBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Direction facing = be.getBlockState().hasProperty(MissileAssemblyBlock.FACING)
                ? be.getBlockState().getValue(MissileAssemblyBlock.FACING)
                : Direction.NORTH;
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(facingToYaw(facing)));
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        ObjModelRenderer.render(pose, buffers, BENCH_MODEL, light, packedOverlay);

        MissileStruct missile = new MissileStruct(be.warhead(), be.fuselage(), be.fins(), be.thruster());
        if (hasAnyPart(missile)) {
            renderSupportsAndMissile(pose, buffers, missile);
        }
        pose.popPose();
    }

    public static void renderBenchItem(PoseStack pose, MultiBufferSource buffers) {
        ObjModelRenderer.render(pose, buffers, BENCH_MODEL, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    private static void renderSupportsAndMissile(PoseStack pose, MultiBufferSource buffers, MissileStruct missile) {
        double height = MissilePronter.getHeight(missile);
        int range = (int) (height / 2.0D - 1.0D);
        int step = range >= 2 ? 2 : 1;
        for (int i = -range; i <= range; i += step) {
            if (i == 0) {
                continue;
            }
            pose.pushPose();
            pose.translate(i, 0.0D, 0.0D);
            ObjModelRenderer.render(pose, buffers, STRUT_MODEL, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }

        pose.pushPose();
        pose.translate(0.0D, 1.5D, 0.0D);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        pose.translate(-height / 2.0D, 0.0D, 0.0D);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        MissilePronter.prontMissile(pose, buffers, missile, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }

    private static boolean hasAnyPart(MissileStruct missile) {
        return missile.warhead != null || missile.fuselage != null
                || missile.fins != null || missile.thruster != null;
    }

    private static float facingToYaw(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0.0F;
            case WEST -> 270.0F;
            case NORTH -> 180.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }

    @Override
    public boolean shouldRenderOffScreen(MissileAssemblyBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
