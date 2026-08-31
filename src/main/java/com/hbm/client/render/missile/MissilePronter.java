package com.hbm.client.render.missile;

import com.hbm.client.render.ObjModelRenderer;
import com.hbm.handler.MissileStruct;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

public final class MissilePronter {
    private MissilePronter() {
    }

    public static void prontMissile(PoseStack pose, MultiBufferSource buffers, MissileStruct struct,
                                    int packedLight) {
        if (struct == null) {
            return;
        }
        pose.pushPose();
        MissilePartModels.Spec thruster = MissilePartModels.get(struct.thruster);
        if (thruster != null && struct.thruster != null && struct.thruster.type == PartType.THRUSTER) {
            ObjModelRenderer.render(pose, buffers, thruster.model(), packedLight, OverlayTexture.NO_OVERLAY);
            pose.translate(0.0D, thruster.height(), 0.0D);
        }
        MissilePartModels.Spec fuselage = MissilePartModels.get(struct.fuselage);
        if (fuselage != null && struct.fuselage != null && struct.fuselage.type == PartType.FUSELAGE) {
            MissilePartModels.Spec fins = MissilePartModels.get(struct.fins);
            if (fins != null && struct.fins != null && struct.fins.type == PartType.FINS) {
                ObjModelRenderer.render(pose, buffers, fins.model(), packedLight, OverlayTexture.NO_OVERLAY);
            }
            ObjModelRenderer.render(pose, buffers, fuselage.model(), packedLight, OverlayTexture.NO_OVERLAY);
            pose.translate(0.0D, fuselage.height(), 0.0D);
        }
        MissilePartModels.Spec warhead = MissilePartModels.get(struct.warhead);
        if (warhead != null && struct.warhead != null && struct.warhead.type == PartType.WARHEAD) {
            ObjModelRenderer.render(pose, buffers, warhead.model(), packedLight, OverlayTexture.NO_OVERLAY);
        }
        pose.popPose();
    }

    public static void prontMissile(PoseStack pose, MultiBufferSource buffers, ItemStack warhead, ItemStack fuselage,
                                    ItemStack fins, ItemStack thruster, int packedLight) {
        prontMissile(pose, buffers, new MissileStruct(warhead, fuselage, fins, thruster), packedLight);
    }

    public static double getHeight(MissileStruct struct) {
        if (struct == null) {
            return 0.0D;
        }
        double h = 0.0D;
        MissilePartModels.Spec t = MissilePartModels.get(struct.thruster);
        if (t != null) {
            h += t.height();
        }
        MissilePartModels.Spec f = MissilePartModels.get(struct.fuselage);
        if (f != null) {
            h += f.height();
        }
        MissilePartModels.Spec w = MissilePartModels.get(struct.warhead);
        if (w != null) {
            h += w.height();
        }
        return h;
    }

    public static double getHeight(ItemStack warhead, ItemStack fuselage, ItemStack fins, ItemStack thruster) {
        return getHeight(new MissileStruct(warhead, fuselage, fins, thruster));
    }

    public static void renderPart(PoseStack pose, MultiBufferSource buffers, ItemCustomMissilePart part) {
        MissilePartModels.Spec spec = MissilePartModels.get(part);
        if (spec == null) {
            return;
        }
        ObjModelRenderer.render(pose, buffers, spec.model(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }
}
