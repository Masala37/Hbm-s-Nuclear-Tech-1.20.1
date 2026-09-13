package com.hbm.client;

import com.hbm.client.render.model.ModelM65;
import com.hbm.items.armor.HazmatArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Worn-layer texture, M65 hood, and vanilla armor-model hooks for {@link HazmatArmorItem}.
 */
public final class HazmatArmorClient {
    private static ModelM65 mask;

    private HazmatArmorClient() {
    }

    public static void attach(HazmatArmorItem item, Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> original) {
                if (!item.maskHelmet() || slot != EquipmentSlot.HEAD) {
                    return original;
                }
                if (mask == null) {
                    mask = ModelM65.create();
                }
                mask.attackTime = original.attackTime;
                mask.riding = original.riding;
                mask.young = original.young;
                mask.crouching = original.crouching;
                mask.leftArmPose = original.leftArmPose;
                mask.rightArmPose = original.rightArmPose;
                mask.head.copyFrom(original.head);
                mask.setAllVisible(false);
                mask.head.visible = true;
                return mask;
            }
        });
    }
}
