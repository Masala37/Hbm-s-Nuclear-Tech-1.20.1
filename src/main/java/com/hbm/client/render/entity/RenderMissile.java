package com.hbm.client.render.entity;

import com.hbm.client.render.ObjModelRenderer;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.EntityMissileBuster;
import com.hbm.entity.missile.EntityMissileCluster;
import com.hbm.entity.missile.EntityMissileIncendiary;
import com.hbm.entity.missile.EntityMissileStrong;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Y-up forge:obj missile renderer — nose (+Y) aligned to travel direction.
 */
public class RenderMissile extends EntityRenderer<EntityMissileBaseNT> {
    public static final ResourceLocation MODEL_V2 = id("block/missile_v2");
    public static final ResourceLocation MODEL_V2_INC = id("block/missile_v2_inc");
    public static final ResourceLocation MODEL_V2_CL = id("block/missile_v2_cl");
    public static final ResourceLocation MODEL_V2_BU = id("block/missile_v2_bu");
    public static final ResourceLocation MODEL_STRONG = id("block/missile_strong");
    public static final ResourceLocation MODEL_STRONG_INC = id("block/missile_strong_inc");
    public static final ResourceLocation MODEL_STRONG_CL = id("block/missile_strong_cl");
    public static final ResourceLocation MODEL_STRONG_BU = id("block/missile_strong_bu");

    private static final ResourceLocation[] ALL = {
            MODEL_V2, MODEL_V2_INC, MODEL_V2_CL, MODEL_V2_BU,
            MODEL_STRONG, MODEL_STRONG_INC, MODEL_STRONG_CL, MODEL_STRONG_BU
    };

    private static final Vector3f NOSE = new Vector3f(0.0F, 1.0F, 0.0F);

    public RenderMissile(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    public static ResourceLocation[] allModels() {
        return ALL;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(RefStrings.MODID, path);
    }

    @Override
    public void render(EntityMissileBaseNT entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        pose.pushPose();

        Vec3 dir = travelDirection(entity, partialTicks);
        Vector3f to = new Vector3f((float) dir.x, (float) dir.y, (float) dir.z);
        if (to.lengthSquared() > 1.0E-8F) {
            to.normalize();
            Quaternionf orient = new Quaternionf().rotationTo(NOSE, to);
            pose.mulPose(orient);
        } else {
            // Fallback: legacy yaw/pitch
            float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
            float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
            pose.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(pitch));
        }

        if (isStrong(entity)) {
            pose.scale(1.5F, 1.5F, 1.5F);
        }

        ObjModelRenderer.render(pose, buffers, modelFor(entity),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffers, packedLight);
    }

    /** Prefer live motion; fall back to displacement / upright. */
    private static Vec3 travelDirection(EntityMissileBaseNT entity, float partialTicks) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6D) {
            return motion;
        }
        double dx = entity.getX() - entity.xo;
        double dy = entity.getY() - entity.yo;
        double dz = entity.getZ() - entity.zo;
        if (dx * dx + dy * dy + dz * dz > 1.0E-6D) {
            return new Vec3(dx, dy, dz);
        }
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        // Legacy pitch 0 = up → world +Y
        double elev = Math.toRadians(pitch + 90.0F);
        double yawRad = Math.toRadians(yaw);
        double horiz = Math.cos(elev);
        return new Vec3(-Math.sin(yawRad) * horiz, Math.sin(elev), Math.cos(yawRad) * horiz);
    }

    public static void renderStanding(PoseStack pose, MultiBufferSource buffers, ResourceLocation modelId,
                                      int packedLight, boolean strongScale) {
        pose.pushPose();
        if (strongScale) {
            pose.scale(1.5F, 1.5F, 1.5F);
        }
        ObjModelRenderer.render(pose, buffers, modelId, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    public static boolean isStrong(EntityMissileBaseNT entity) {
        if (entity instanceof EntityMissileStrong) {
            return true;
        }
        if (entity instanceof EntityMissileIncendiary e) {
            return e.isStrong();
        }
        if (entity instanceof EntityMissileCluster e) {
            return e.isStrong();
        }
        if (entity instanceof EntityMissileBuster e) {
            return e.isStrong();
        }
        return false;
    }

    public static ResourceLocation modelFor(EntityMissileBaseNT entity) {
        boolean strong = isStrong(entity);
        if (entity instanceof EntityMissileIncendiary) {
            return strong ? MODEL_STRONG_INC : MODEL_V2_INC;
        }
        if (entity instanceof EntityMissileCluster) {
            return strong ? MODEL_STRONG_CL : MODEL_V2_CL;
        }
        if (entity instanceof EntityMissileBuster) {
            return strong ? MODEL_STRONG_BU : MODEL_V2_BU;
        }
        return strong ? MODEL_STRONG : MODEL_V2;
    }

    public static ResourceLocation modelForItem(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) {
            return MODEL_V2;
        }
        return switch (key.getPath()) {
            case "missile_strong" -> MODEL_STRONG;
            case "missile_incendiary_strong" -> MODEL_STRONG_INC;
            case "missile_cluster_strong" -> MODEL_STRONG_CL;
            case "missile_buster_strong" -> MODEL_STRONG_BU;
            case "missile_incendiary" -> MODEL_V2_INC;
            case "missile_cluster" -> MODEL_V2_CL;
            case "missile_buster" -> MODEL_V2_BU;
            default -> MODEL_V2;
        };
    }

    public static boolean isStrongItem(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && key.getPath().contains("_strong");
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMissileBaseNT entity) {
        return new ResourceLocation(RefStrings.MODID, "textures/block/missile/missile_v2.png");
    }
}
