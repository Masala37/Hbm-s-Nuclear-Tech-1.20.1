package com.hbm.client.render.blockentity;

import com.hbm.blockentity.network.PylonBlockEntity;
import com.hbm.blockentity.network.PylonKind;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 1.7.10 {@code RenderPylon} / {@code RenderConnector} / medium / large / {@code RenderSubstation}
 * plus {@code RenderPylonBase} hanging wires.
 */
public class RenderPylon implements BlockEntityRenderer<PylonBlockEntity> {
    public static final ResourceLocation CONNECTOR =
            new ResourceLocation(RefStrings.MODID, "block/red_connector");
    public static final ResourceLocation MEDIUM_WOOD =
            new ResourceLocation(RefStrings.MODID, "block/red_pylon_medium_wood");
    public static final ResourceLocation MEDIUM_WOOD_TRANSFORMER =
            new ResourceLocation(RefStrings.MODID, "block/red_pylon_medium_wood_transformer");
    public static final ResourceLocation MEDIUM_STEEL =
            new ResourceLocation(RefStrings.MODID, "block/red_pylon_medium_steel");
    public static final ResourceLocation MEDIUM_STEEL_TRANSFORMER =
            new ResourceLocation(RefStrings.MODID, "block/red_pylon_medium_steel_transformer");
    public static final ResourceLocation LARGE =
            new ResourceLocation(RefStrings.MODID, "block/red_pylon_large");
    public static final ResourceLocation SUBSTATION =
            new ResourceLocation(RefStrings.MODID, "block/substation");
    private static final ResourceLocation SMALL_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/model_pylon.png");
    private static final ResourceLocation WIRE_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/network/wire.png");
    private static final ResourceLocation WIRE_GREY_TEX =
            new ResourceLocation(RefStrings.MODID, "textures/models/network/wire_greyscale.png");

    private final ModelPart smallPylon;

    public static List<ResourceLocation> allModels() {
        return List.of(CONNECTOR, MEDIUM_WOOD, MEDIUM_WOOD_TRANSFORMER, MEDIUM_STEEL,
                MEDIUM_STEEL_TRANSFORMER, LARGE, SUBSTATION);
    }

    public RenderPylon(BlockEntityRendererProvider.Context context) {
        this.smallPylon = createSmallPylon();
    }

    private static ModelPart createSmallPylon() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 96).mirror(true)
                .addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(-8.0F, -6.0F, -8.0F));
        root.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(1, 1).mirror(true)
                .addBox(0.0F, 0.0F, 0.0F, 4.0F, 73.0F, 4.0F), PartPose.offset(-2.0F, -79.0F, -2.0F));
        root.addOrReplaceChild("top1", CubeListBuilder.create().texOffs(24, 1).mirror(true)
                .addBox(0.0F, 0.0F, 0.0F, 6.0F, 4.0F, 6.0F), PartPose.offset(-3.0F, -74.0F, -3.0F));
        root.addOrReplaceChild("top2", CubeListBuilder.create().texOffs(25, 17).mirror(true)
                .addBox(0.0F, 0.0F, 0.0F, 6.0F, 2.0F, 6.0F), PartPose.offset(-3.0F, -78.0F, -3.0F));
        return LayerDefinition.create(mesh, 64, 128).bakeRoot();
    }

    @Override
    public void render(PylonBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int light = Math.max(packedLight, LightTexture.pack(12, 12));
        PylonKind kind = be.kind();
        Direction facing = be.facing();
        pose.pushPose();
        switch (kind) {
            case CONNECTOR -> renderConnector(pose, buffers, facing, light, packedOverlay);
            case SMALL -> renderSmall(pose, buffers, light, packedOverlay);
            case MEDIUM_WOOD, MEDIUM_WOOD_TRANSFORMER, MEDIUM_STEEL, MEDIUM_STEEL_TRANSFORMER, LARGE, SUBSTATION ->
                    renderObj(pose, buffers, kind, facing, light, packedOverlay);
        }
        pose.popPose();
        renderLines(be, pose, buffers, packedOverlay);
    }

    private void renderSmall(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.translate(0.5D, 1.5D - (14.0D / 16.0D), 0.5D);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(SMALL_TEX));
        smallPylon.render(pose, consumer, light, overlay);
    }

    private static void renderConnector(PoseStack pose, MultiBufferSource buffers, Direction facing,
                                        int light, int overlay) {
        pose.translate(0.5D, 0.5D, 0.5D);
        switch (facing) {
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(180.0F));
            case UP -> {
            }
            case NORTH -> {
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            case SOUTH -> pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            case WEST -> {
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            case EAST -> {
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.mulPose(Axis.ZP.rotationDegrees(270.0F));
            }
        }
        pose.translate(0.0D, -0.5D, 0.0D);
        ObjModelRenderer.render(pose, buffers, CONNECTOR, light, overlay);
    }

    private static void renderObj(PoseStack pose, MultiBufferSource buffers, PylonKind kind, Direction facing,
                                  int light, int overlay) {
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(kind.tesrYaw(facing)));
        ResourceLocation model = switch (kind) {
            case MEDIUM_WOOD -> MEDIUM_WOOD;
            case MEDIUM_WOOD_TRANSFORMER -> MEDIUM_WOOD_TRANSFORMER;
            case MEDIUM_STEEL -> MEDIUM_STEEL;
            case MEDIUM_STEEL_TRANSFORMER -> MEDIUM_STEEL_TRANSFORMER;
            case LARGE -> LARGE;
            default -> SUBSTATION;
        };
        ObjModelRenderer.render(pose, buffers, model, light, overlay);
    }

    private static void renderLines(PylonBlockEntity pyl, PoseStack pose, MultiBufferSource buffers, int overlay) {
        Level level = pyl.getLevel();
        if (level == null) {
            return;
        }
        ResourceLocation tex = pyl.color() == 0 ? WIRE_TEX : WIRE_GREY_TEX;
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(tex));
        Vec3[] m1 = pyl.mountPos();
        int color = pyl.color() == 0 ? 0xFFFFFF : pyl.color();
        for (BlockPos wire : pyl.getConnected()) {
            BlockEntity tile = level.getBlockEntity(wire);
            if (!(tile instanceof PylonBlockEntity other)) {
                continue;
            }
            Vec3[] m2 = other.mountPos();
            int lineCount = Math.min(m1.length, m2.length);
            for (int line = 0; line < lineCount; line++) {
                Vec3 first = m1[line % m1.length];
                int secondIndex = line % m2.length;
                if (lineCount == 4 && (
                        (pyl.facing() == Direction.EAST && other.facing() == Direction.NORTH)
                                || (pyl.facing() == Direction.NORTH && other.facing() == Direction.EAST))) {
                    secondIndex = (secondIndex + 2) % m2.length;
                }
                Vec3 second = m2[secondIndex];
                double sX = second.x + other.getBlockPos().getX() - pyl.getBlockPos().getX();
                double sY = second.y + other.getBlockPos().getY() - pyl.getBlockPos().getY();
                double sZ = second.z + other.getBlockPos().getZ() - pyl.getBlockPos().getZ();
                renderLine(level, pyl, pose, consumer, overlay, color,
                        first.x, first.y, first.z,
                        first.x + (sX - first.x) * 0.5D,
                        first.y + (sY - first.y) * 0.5D,
                        first.z + (sZ - first.z) * 0.5D);
            }
        }
    }

    private static void renderLine(Level world, PylonBlockEntity pyl, PoseStack pose, VertexConsumer consumer,
                                   int overlay, int color, double x0, double y0, double z0,
                                   double x1, double y1, double z1) {
        double dx = x0 - x1;
        double dy = y0 - y1;
        double dz = z0 - z1;
        double girth = 0.03125D;
        double hyp = Math.sqrt(dx * dx + dz * dz);
        double yaw = Math.atan2(dx, dz);
        double pitch = Math.atan2(dy, hyp);
        double rotator = Math.PI * 0.5D;
        double newPitch = pitch + rotator;
        double newYaw = yaw + rotator;
        double iZ = Math.cos(yaw) * Math.cos(newPitch) * girth;
        double iX = Math.sin(yaw) * Math.cos(newPitch) * girth;
        double iY = Math.sin(newPitch) * girth;
        double jZ = Math.cos(newYaw) * girth;
        double jX = Math.sin(newYaw) * girth;
        double hang = Math.min(Math.sqrt(dx * dx + dy * dy + dz * dz) / 15.0D, 2.5D);
        float count = 10.0F;
        double deltaX = x1 - x0;
        double deltaY = y1 - y0;
        double deltaZ = z1 - z0;
        for (float j = 0; j < count; j++) {
            float k = j + 1.0F;
            double sagJ = Math.sin(j / count * Math.PI * 0.5D) * hang;
            double sagK = Math.sin(k / count * Math.PI * 0.5D) * hang;
            double sagMean = (sagJ + sagK) / 2.0D;
            double ja = j + 0.5D;
            double ix = pyl.getBlockPos().getX() + x0 + deltaX / count * ja;
            double iy = pyl.getBlockPos().getY() + y0 + deltaY / count * ja - sagMean;
            double iz = pyl.getBlockPos().getZ() + z0 + deltaZ / count * ja;
            int brightness = LevelRenderer.getLightColor(world, BlockPos.containing(ix, iy, iz));
            drawLineSegment(pose, consumer, overlay, brightness, color,
                    x0 + (deltaX * j / count),
                    y0 + (deltaY * j / count) - sagJ,
                    z0 + (deltaZ * j / count),
                    x0 + (deltaX * k / count),
                    y0 + (deltaY * k / count) - sagK,
                    z0 + (deltaZ * k / count),
                    iX, iY, iZ, jX, jZ);
        }
    }

    private static void drawLineSegment(PoseStack pose, VertexConsumer consumer, int overlay, int light, int color,
                                        double x, double y, double z, double a, double b, double c,
                                        double iX, double iY, double iZ, double jX, double jZ) {
        double deltaX = a - x;
        double deltaY = b - y;
        double deltaZ = c - z;
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        int wrap = (int) Math.ceil(length * 8.0D);
        double ujX = jX;
        double ujZ = jZ;
        if (deltaX + deltaZ < 0.0D) {
            wrap *= -1;
            ujZ *= -1.0D;
            ujX *= -1.0D;
        }
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int bl = color & 255;
        Matrix4f mat = pose.last().pose();
        Matrix3f nrm = pose.last().normal();
        quad(consumer, mat, nrm, overlay, light, r, g, bl,
                x + iX, y + iY, z + iZ, 0.0F, 0.0F,
                x - iX, y - iY, z - iZ, 0.0F, 1.0F,
                a - iX, b - iY, c - iZ, wrap, 1.0F,
                a + iX, b + iY, c + iZ, wrap, 0.0F);
        quad(consumer, mat, nrm, overlay, light, r, g, bl,
                x + ujX, y, z + ujZ, 0.0F, 0.0F,
                x - ujX, y, z - ujZ, 0.0F, 1.0F,
                a - ujX, b, c - ujZ, wrap, 1.0F,
                a + ujX, b, c + ujZ, wrap, 0.0F);
    }

    private static void quad(VertexConsumer consumer, Matrix4f mat, Matrix3f nrm, int overlay, int light,
                             int r, int g, int b,
                             double x0, double y0, double z0, float u0, float v0,
                             double x1, double y1, double z1, float u1, float v1,
                             double x2, double y2, double z2, float u2, float v2,
                             double x3, double y3, double z3, float u3, float v3) {
        vert(consumer, mat, nrm, overlay, light, r, g, b, x0, y0, z0, u0, v0);
        vert(consumer, mat, nrm, overlay, light, r, g, b, x1, y1, z1, u1, v1);
        vert(consumer, mat, nrm, overlay, light, r, g, b, x2, y2, z2, u2, v2);
        vert(consumer, mat, nrm, overlay, light, r, g, b, x3, y3, z3, u3, v3);
    }

    private static void vert(VertexConsumer consumer, Matrix4f mat, Matrix3f nrm, int overlay, int light,
                             int r, int g, int b, double x, double y, double z, float u, float v) {
        consumer.vertex(mat, (float) x, (float) y, (float) z)
                .color(r, g, b, 255)
                .uv(u, v)
                .overlayCoords(overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay)
                .uv2(light)
                .normal(nrm, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(PylonBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
