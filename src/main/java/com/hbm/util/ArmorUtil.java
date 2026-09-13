package com.hbm.util;

import com.hbm.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Armor set checks used by contamination (legacy {@code ArmorUtil} hazmat subset).
 * Yellow/red/grey are {@code checkForHazmat}. {@code checkForHaz2} is PAA/liquidator/etc., not ported.
 */
public final class ArmorUtil {
    private ArmorUtil() {
    }

    public static boolean checkForHazmat(Player player) {
        return isFullSet(player,
                ModItems.HAZMAT_HELMET.get(),
                ModItems.HAZMAT_PLATE.get(),
                ModItems.HAZMAT_LEGS.get(),
                ModItems.HAZMAT_BOOTS.get())
                || isFullSet(player,
                ModItems.HAZMAT_HELMET_RED.get(),
                ModItems.HAZMAT_PLATE_RED.get(),
                ModItems.HAZMAT_LEGS_RED.get(),
                ModItems.HAZMAT_BOOTS_RED.get())
                || isFullSet(player,
                ModItems.HAZMAT_HELMET_GREY.get(),
                ModItems.HAZMAT_PLATE_GREY.get(),
                ModItems.HAZMAT_LEGS_GREY.get(),
                ModItems.HAZMAT_BOOTS_GREY.get());
    }

    public static boolean checkForHaz2(Player player) {
        return false;
    }

    public static boolean checkForDigamma(Player player) {
        return false;
    }

    public static boolean checkForDigamma2(Player player) {
        return false;
    }

    private static boolean isFullSet(LivingEntity entity, Item helmet, Item chest, Item legs, Item boots) {
        return matches(entity.getItemBySlot(EquipmentSlot.HEAD), helmet)
                && matches(entity.getItemBySlot(EquipmentSlot.CHEST), chest)
                && matches(entity.getItemBySlot(EquipmentSlot.LEGS), legs)
                && matches(entity.getItemBySlot(EquipmentSlot.FEET), boots);
    }

    private static boolean matches(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.is(item);
    }
}
