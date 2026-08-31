package com.hbm.client.render.entity;

import com.hbm.client.render.ObjModelRenderer;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.EntityMissileBHole;
import com.hbm.entity.missile.EntityMissileBurst;
import com.hbm.entity.missile.EntityMissileBuster;
import com.hbm.entity.missile.EntityMissileCluster;
import com.hbm.entity.missile.EntityMissileDecoy;
import com.hbm.entity.missile.EntityMissileEMP;
import com.hbm.entity.missile.EntityMissileEMPStrong;
import com.hbm.entity.missile.EntityMissileIncendiary;
import com.hbm.entity.missile.EntityMissileInferno;
import com.hbm.entity.missile.EntityMissileMicro;
import com.hbm.entity.missile.EntityMissileRain;
import com.hbm.entity.missile.EntityMissileDrill;
import com.hbm.entity.missile.EntityMissileSchrabidium;
import com.hbm.entity.missile.EntityMissileDoomsday;
import com.hbm.entity.missile.EntityMissileDoomsdayRusted;
import com.hbm.entity.missile.EntityMissileMirv;
import com.hbm.entity.missile.EntityMissileNuclear;
import com.hbm.entity.missile.EntityMissileVolcano;
import com.hbm.entity.missile.EntityMissileShuttle;
import com.hbm.entity.missile.EntityMissileStealth;
import com.hbm.entity.missile.EntityMissileStrong;
import com.hbm.entity.missile.EntityMissileTaint;
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
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Nose along +Y. Flight pose matches 1.7.10 {@code RenderMissileGeneric}:
 * yaw-90 around Y, pitch around Z, then undo yaw.
 */
public class RenderMissile extends EntityRenderer<EntityMissileBaseNT> {
    public static final ResourceLocation MODEL_V2 = id("block/missile_v2");
    public static final ResourceLocation MODEL_V2_INC = id("block/missile_v2_inc");
    public static final ResourceLocation MODEL_V2_CL = id("block/missile_v2_cl");
    public static final ResourceLocation MODEL_V2_BU = id("block/missile_v2_bu");
    public static final ResourceLocation MODEL_V2_DECOY = id("block/missile_v2_decoy");
    public static final ResourceLocation MODEL_STRONG = id("block/missile_strong");
    public static final ResourceLocation MODEL_STRONG_INC = id("block/missile_strong_inc");
    public static final ResourceLocation MODEL_STRONG_CL = id("block/missile_strong_cl");
    public static final ResourceLocation MODEL_STRONG_BU = id("block/missile_strong_bu");
    public static final ResourceLocation MODEL_MICRO_TAINT = id("block/missile_micro_taint");
    public static final ResourceLocation MODEL_MICRO = id("block/missile_micro");
    public static final ResourceLocation MODEL_MICRO_BHOLE = id("block/missile_micro_bhole");
    public static final ResourceLocation MODEL_MICRO_SCHRAB = id("block/missile_micro_schrab");
    public static final ResourceLocation MODEL_MICRO_EMP = id("block/missile_micro_emp");
    public static final ResourceLocation MODEL_STRONG_EMP = id("block/missile_strong_emp");
    public static final ResourceLocation MODEL_STEALTH = id("block/missile_stealth");
    public static final ResourceLocation MODEL_HUGE = id("block/missile_huge");
    public static final ResourceLocation MODEL_HUGE_INC = id("block/missile_huge_inc");
    public static final ResourceLocation MODEL_HUGE_CL = id("block/missile_huge_cl");
    public static final ResourceLocation MODEL_HUGE_BU = id("block/missile_huge_bu");
    public static final ResourceLocation MODEL_SHUTTLE = id("block/missile_shuttle");
    public static final ResourceLocation MODEL_NUCLEAR = id("block/missile_nuclear");
    public static final ResourceLocation MODEL_NUCLEAR_CLUSTER = id("block/missile_nuclear_cluster");
    public static final ResourceLocation MODEL_VOLCANO = id("block/missile_volcano");
    public static final ResourceLocation MODEL_DOOMSDAY = id("block/missile_doomsday");
    public static final ResourceLocation MODEL_DOOMSDAY_RUSTED = id("block/missile_doomsday_rusted");
    public static final ResourceLocation MODEL_ABM = id("block/missile_abm");

    private static final ResourceLocation[] ALL = {
            MODEL_V2, MODEL_V2_INC, MODEL_V2_CL, MODEL_V2_BU, MODEL_V2_DECOY,
            MODEL_STRONG, MODEL_STRONG_INC, MODEL_STRONG_CL, MODEL_STRONG_BU, MODEL_STRONG_EMP,
            MODEL_MICRO_TAINT, MODEL_MICRO, MODEL_MICRO_BHOLE, MODEL_MICRO_SCHRAB, MODEL_MICRO_EMP,
            MODEL_STEALTH, MODEL_HUGE, MODEL_HUGE_INC, MODEL_HUGE_CL, MODEL_HUGE_BU,
            MODEL_SHUTTLE, MODEL_NUCLEAR, MODEL_NUCLEAR_CLUSTER, MODEL_VOLCANO,
            MODEL_DOOMSDAY, MODEL_DOOMSDAY_RUSTED, MODEL_ABM
    };

    public RenderMissile(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
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

        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        pose.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(pitch));
        pose.mulPose(Axis.YP.rotationDegrees(-(yaw - 90.0F)));

        if (isStrong(entity)) {
            pose.scale(1.5F, 1.5F, 1.5F);
        }

        ObjModelRenderer.render(pose, buffers, modelFor(entity),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffers, packedLight);
    }

    public static void renderStanding(PoseStack pose, MultiBufferSource buffers, ResourceLocation modelId,
                                      int packedLight, boolean strongScale) {
        renderStanding(pose, buffers, modelId, packedLight, strongScale ? 1.5F : 1.0F);
    }

    public static void renderStanding(PoseStack pose, MultiBufferSource buffers, ResourceLocation modelId,
                                      int packedLight, float scale) {
        pose.pushPose();
        if (scale != 1.0F) {
            pose.scale(scale, scale, scale);
        }
        ObjModelRenderer.render(pose, buffers, modelId, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    /** Small-pad world render: 1.5 only for strong (item renderer generateLarge). */
    public static float standingScale(ItemStack stack) {
        if (stack.getItem() instanceof com.hbm.items.weapon.MissileItem missile) {
            return missile.getTier().meshScale;
        }
        return isStrongItem(stack) ? 1.5F : 1.0F;
    }

    public static boolean isStealthItem(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && "missile_stealth".equals(key.getPath());
    }

    public static boolean isHugeItem(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) {
            return false;
        }
        String path = key.getPath();
        return "missile_burst".equals(path) || "missile_inferno".equals(path)
                || "missile_rain".equals(path) || "missile_drill".equals(path);
    }

    public static boolean isAtlasItem(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) {
            return false;
        }
        String path = key.getPath();
        return "missile_nuclear".equals(path) || "missile_nuclear_cluster".equals(path)
                || "missile_volcano".equals(path) || "missile_doomsday".equals(path)
                || "missile_doomsday_rusted".equals(path);
    }

    public static boolean isStrong(EntityMissileBaseNT entity) {
        if (entity instanceof EntityMissileStrong || entity instanceof EntityMissileEMPStrong) {
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
        if (entity instanceof EntityMissileTaint) {
            return MODEL_MICRO_TAINT;
        }
        if (entity instanceof EntityMissileMicro) {
            return MODEL_MICRO;
        }
        if (entity instanceof EntityMissileBHole) {
            return MODEL_MICRO_BHOLE;
        }
        if (entity instanceof EntityMissileSchrabidium) {
            return MODEL_MICRO_SCHRAB;
        }
        if (entity instanceof EntityMissileEMP) {
            return MODEL_MICRO_EMP;
        }
        if (entity instanceof EntityMissileEMPStrong) {
            return MODEL_STRONG_EMP;
        }
        if (entity instanceof EntityMissileDecoy) {
            return MODEL_V2_DECOY;
        }
        if (entity instanceof EntityMissileStealth) {
            return MODEL_STEALTH;
        }
        if (entity instanceof EntityMissileBurst) {
            return MODEL_HUGE;
        }
        if (entity instanceof EntityMissileInferno) {
            return MODEL_HUGE_INC;
        }
        if (entity instanceof EntityMissileRain) {
            return MODEL_HUGE_CL;
        }
        if (entity instanceof EntityMissileDrill) {
            return MODEL_HUGE_BU;
        }
        if (entity instanceof EntityMissileShuttle) {
            return MODEL_SHUTTLE;
        }
        if (entity instanceof EntityMissileNuclear) {
            return MODEL_NUCLEAR;
        }
        if (entity instanceof EntityMissileMirv) {
            return MODEL_NUCLEAR_CLUSTER;
        }
        if (entity instanceof EntityMissileVolcano) {
            return MODEL_VOLCANO;
        }
        if (entity instanceof EntityMissileDoomsdayRusted) {
            return MODEL_DOOMSDAY_RUSTED;
        }
        if (entity instanceof EntityMissileDoomsday) {
            return MODEL_DOOMSDAY;
        }
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
            case "missile_taint" -> MODEL_MICRO_TAINT;
            case "missile_micro" -> MODEL_MICRO;
            case "missile_bhole" -> MODEL_MICRO_BHOLE;
            case "missile_schrabidium" -> MODEL_MICRO_SCHRAB;
            case "missile_emp" -> MODEL_MICRO_EMP;
            case "missile_emp_strong" -> MODEL_STRONG_EMP;
            case "missile_decoy" -> MODEL_V2_DECOY;
            case "missile_stealth" -> MODEL_STEALTH;
            case "missile_burst" -> MODEL_HUGE;
            case "missile_inferno" -> MODEL_HUGE_INC;
            case "missile_rain" -> MODEL_HUGE_CL;
            case "missile_drill" -> MODEL_HUGE_BU;
            case "missile_shuttle" -> MODEL_SHUTTLE;
            case "missile_nuclear" -> MODEL_NUCLEAR;
            case "missile_nuclear_cluster" -> MODEL_NUCLEAR_CLUSTER;
            case "missile_volcano" -> MODEL_VOLCANO;
            case "missile_doomsday" -> MODEL_DOOMSDAY;
            case "missile_doomsday_rusted" -> MODEL_DOOMSDAY_RUSTED;
            case "missile_anti_ballistic" -> MODEL_ABM;
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
