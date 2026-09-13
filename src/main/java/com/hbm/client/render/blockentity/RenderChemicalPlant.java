package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.ChemicalPlantBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.inventory.recipes.GenericMachineRecipe;
import com.hbm.inventory.recipes.GenericRecipeMatch;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.List;

/**
 * 1.7.10 {@code RenderChemicalPlant}: base, optional frame, slider, spinner, tinted fluid.
 */
public class RenderChemicalPlant implements BlockEntityRenderer<ChemicalPlantBlockEntity> {
    public static final ResourceLocation BASE =
            new ResourceLocation(RefStrings.MODID, "block/chemical_plant_base");
    public static final ResourceLocation FRAME =
            new ResourceLocation(RefStrings.MODID, "block/chemical_plant_frame");
    public static final ResourceLocation SLIDER =
            new ResourceLocation(RefStrings.MODID, "block/chemical_plant_slider");
    public static final ResourceLocation SPINNER =
            new ResourceLocation(RefStrings.MODID, "block/chemical_plant_spinner");
    public static final ResourceLocation FLUID =
            new ResourceLocation(RefStrings.MODID, "block/chemical_plant_fluid");

    public static List<ResourceLocation> allModels() {
        return List.of(BASE, FRAME, SLIDER, SPINNER, FLUID);
    }

    public RenderChemicalPlant(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ChemicalPlantBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        int meta = be.getBlockState().hasProperty(BlockDummyable.META)
                ? be.getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        pose.mulPose(Axis.YP.rotationDegrees(DummyableMeta.tesrYaw(DummyableMeta.coreFacing(meta))));

        ObjModelRenderer.render(pose, buffers, BASE, light, packedOverlay);
        if (be.hasFrame()) {
            ObjModelRenderer.render(pose, buffers, FRAME, light, packedOverlay);
        }

        float anim = Mth.lerp(partialTick, be.prevAnim, be.anim);
        pose.pushPose();
        pose.translate(sps(anim * 0.125D) * 0.375D, 0.0D, 0.0D);
        ObjModelRenderer.render(pose, buffers, SLIDER, light, packedOverlay);
        pose.popPose();

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees((anim * 15.0F) % 360.0F));
        pose.translate(-0.5D, 0.0D, -0.5D);
        ObjModelRenderer.render(pose, buffers, SPINNER, light, packedOverlay);
        pose.popPose();

        if (be.didProcess()) {
            float[] tint = fluidTint(be.getRecipe());
            if (tint != null) {
                ObjModelRenderer.renderColored(pose, buffers, FLUID, light, packedOverlay,
                        tint[0], tint[1], tint[2], 0.5F);
            }
        }

        pose.popPose();
    }

    private static double sps(double x) {
        return Math.sin(Math.PI / 2.0D * Math.cos(x));
    }

    private static float[] fluidTint(GenericMachineRecipe recipe) {
        if (recipe == null) {
            return null;
        }
        int colors = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        for (GenericMachineRecipe.FluidInput stack : recipe.outputFluid()) {
            int color = fluidColor(stack.fluid());
            if (color == 0) {
                continue;
            }
            r += (color >> 16) & 255;
            g += (color >> 8) & 255;
            b += color & 255;
            colors++;
        }
        if (colors == 0) {
            for (GenericMachineRecipe.FluidInput stack : recipe.inputFluid()) {
                int color = fluidColor(stack.fluid());
                if (color == 0) {
                    continue;
                }
                r += (color >> 16) & 255;
                g += (color >> 8) & 255;
                b += color & 255;
                colors++;
            }
        }
        if (colors == 0) {
            return null;
        }
        return new float[]{r / 255.0F / colors, g / 255.0F / colors, b / 255.0F / colors};
    }

    private static int fluidColor(String id) {
        Fluid fluid = GenericRecipeMatch.fluid(id);
        if (fluid == null) {
            return 0;
        }
        return IClientFluidTypeExtensions.of(fluid).getTintColor();
    }

    @Override
    public boolean shouldRenderOffScreen(ChemicalPlantBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
