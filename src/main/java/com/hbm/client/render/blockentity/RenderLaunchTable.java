package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.LaunchTableBlockEntity;
import com.hbm.client.render.ObjPartModel;
import com.hbm.client.render.missile.MissilePronter;
import com.hbm.entity.missile.MissileSystemRules;
import com.hbm.handler.MissileStruct;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RenderLaunchTable implements BlockEntityRenderer<LaunchTableBlockEntity> {
    public static final ResourceLocation BASE =
            new ResourceLocation(RefStrings.MODID, "models/obj/launch_table/launch_table_base.obj");
    public static final ResourceLocation SMALL_PAD =
            new ResourceLocation(RefStrings.MODID, "models/obj/launch_table/launch_table_small_pad.obj");
    public static final ResourceLocation LARGE_PAD =
            new ResourceLocation(RefStrings.MODID, "models/obj/launch_table/launch_table_large_pad.obj");
    public static final ResourceLocation LARGE_SCAFFOLD_BASE =
            new ResourceLocation(RefStrings.MODID, "models/obj/launch_table/launch_table_large_scaffold_base.obj");
    public static final ResourceLocation LARGE_SCAFFOLD_CONNECTOR =
            new ResourceLocation(RefStrings.MODID, "models/obj/launch_table/launch_table_large_scaffold_connector.obj");
    public static final ResourceLocation LARGE_SCAFFOLD_EMPTY =
            new ResourceLocation(RefStrings.MODID, "models/obj/launch_table/launch_table_large_scaffold_empty.obj");
    public static final ResourceLocation SMALL_SCAFFOLD_BASE =
            new ResourceLocation(RefStrings.MODID, "models/obj/launch_table/launch_table_small_scaffold_base.obj");
    public static final ResourceLocation SMALL_SCAFFOLD_CONNECTOR =
            new ResourceLocation(RefStrings.MODID, "models/obj/launch_table/launch_table_small_scaffold_connector.obj");
    public static final ResourceLocation SMALL_SCAFFOLD_EMPTY =
            new ResourceLocation(RefStrings.MODID, "models/obj/launch_table/launch_table_small_scaffold_empty.obj");

    public static final ResourceLocation BASE_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/missile_parts/launch_table.png");
    public static final ResourceLocation SMALL_PAD_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/missile_parts/launch_table_small_pad.png");
    public static final ResourceLocation LARGE_PAD_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/missile_parts/launch_table_large_pad.png");
    public static final ResourceLocation LARGE_SCAFFOLD_BASE_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/missile_parts/launch_table_large_scaffold_base.png");
    public static final ResourceLocation LARGE_SCAFFOLD_CONNECTOR_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/missile_parts/launch_table_large_scaffold_connector.png");
    public static final ResourceLocation SMALL_SCAFFOLD_BASE_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/missile_parts/launch_table_small_scaffold_base.png");
    public static final ResourceLocation SMALL_SCAFFOLD_CONNECTOR_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/missile_parts/launch_table_small_scaffold_connector.png");

    public RenderLaunchTable(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LaunchTableBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(MissileSystemRules.launchTableYaw(be.getFacing().getSerializedName())));
        MissileStruct load = be.getLoad();
        if (load != null && load.fuselage != null) {
            int measured = (int) MissilePronter.getHeight(load);
            if (measured > 0) {
                be.height = measured;
            }
        }
        renderTable(pose, buffers, be.getPadSize(), load, be.height, light, packedOverlay);
        pose.popPose();
    }

    public static void renderTable(PoseStack pose, MultiBufferSource buffers, PartSize padSize, MissileStruct load,
                                    int height, int packedLight, int packedOverlay) {
        ObjPartModel.get(BASE).renderAll(pose, buffers, BASE_TEX, packedLight, packedOverlay);
        if (padSize == PartSize.SIZE_20) {
            ObjPartModel.get(LARGE_PAD).renderAll(pose, buffers, LARGE_PAD_TEX, packedLight, packedOverlay);
        } else {
            ObjPartModel.get(SMALL_PAD).renderAll(pose, buffers, SMALL_PAD_TEX, packedLight, packedOverlay);
        }

        if (height < 1) {
            height = 10;
        }
        int connectorAt = (int) (height * 0.75);

        ResourceLocation baseTex = LARGE_SCAFFOLD_BASE_TEX;
        ResourceLocation connectorTex = LARGE_SCAFFOLD_CONNECTOR_TEX;
        ResourceLocation baseObj = LARGE_SCAFFOLD_BASE;
        ResourceLocation connectorObj = LARGE_SCAFFOLD_CONNECTOR;
        ResourceLocation emptyObj = LARGE_SCAFFOLD_EMPTY;

        pose.pushPose();
        if (padSize == PartSize.SIZE_10) {
            baseTex = SMALL_SCAFFOLD_BASE_TEX;
            connectorTex = SMALL_SCAFFOLD_CONNECTOR_TEX;
            baseObj = SMALL_SCAFFOLD_BASE;
            connectorObj = SMALL_SCAFFOLD_CONNECTOR;
            emptyObj = SMALL_SCAFFOLD_EMPTY;
            pose.translate(0.0D, 0.0D, -1.0D);
        }
        pose.translate(0.0D, 1.0D, 3.5D);
        boolean matching = load != null && load.fuselage != null && load.fuselage.top == padSize;
        for (int i = 0; i < height + 1; i++) {
            if (i < connectorAt) {
                ObjPartModel.get(baseObj).renderAll(pose, buffers, baseTex, packedLight, packedOverlay);
            } else if (i > connectorAt) {
                ObjPartModel.get(emptyObj).renderAll(pose, buffers, baseTex, packedLight, packedOverlay);
            } else if (matching) {
                ObjPartModel.get(connectorObj).renderAll(pose, buffers, connectorTex, packedLight, packedOverlay);
            } else {
                ObjPartModel.get(baseObj).renderAll(pose, buffers, baseTex, packedLight, packedOverlay);
            }
            pose.translate(0.0D, 1.0D, 0.0D);
        }
        pose.popPose();

        if (matching) {
            pose.translate(0.0D, 2.0625D, 0.0D);
            MissilePronter.prontMissile(pose, buffers, load, LightTexture.FULL_BRIGHT);
        }
    }

    public static void renderItem(PoseStack pose, MultiBufferSource buffers) {
        pose.pushPose();
        pose.scale(0.5F, 0.5F, 0.5F);
        ObjPartModel.get(BASE).renderAll(pose, buffers, BASE_TEX, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
        ObjPartModel.get(SMALL_PAD).renderAll(pose, buffers, SMALL_PAD_TEX, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
        pose.translate(0.0D, 0.0D, 2.5D);
        for (int i = 0; i < 8; i++) {
            pose.translate(0.0D, 1.0D, 0.0D);
            if (i < 6) {
                ObjPartModel.get(SMALL_SCAFFOLD_BASE).renderAll(pose, buffers, SMALL_SCAFFOLD_BASE_TEX,
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
            } else if (i == 6) {
                ObjPartModel.get(SMALL_SCAFFOLD_CONNECTOR).renderAll(pose, buffers, SMALL_SCAFFOLD_CONNECTOR_TEX,
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
            } else {
                ObjPartModel.get(SMALL_SCAFFOLD_EMPTY).renderAll(pose, buffers, SMALL_SCAFFOLD_BASE_TEX,
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, true);
            }
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LaunchTableBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
