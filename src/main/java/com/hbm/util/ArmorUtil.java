package com.hbm.util;

import com.hbm.lib.RefStrings;
import com.hbm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Armor set checks used by contamination (legacy ArmorUtil hazmat + digamma subset).
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
                || checkForHaz2(player);
    }

    public static boolean checkForHaz2(Player player) {
        return isFullSet(player,
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

    public static boolean checkForDigamma(Player player) {
        return isFullSetById(player, "fau_helmet", "fau_plate", "fau_legs", "fau_boots")
                || isFullSetById(player, "dns_helmet", "dns_plate", "dns_legs", "dns_boots");
    }

    public static boolean checkForDigamma2(Player player) {
        return isFullSetById(player, "robes_helmet", "robes_plate", "robes_legs", "robes_boots")
                || isFullSetById(player, "robe_helmet", "robe_plate", "robe_legs", "robe_boots");
    }

    private static boolean isFullSet(LivingEntity entity, Item helmet, Item chest, Item legs, Item boots) {
        return matches(entity.getItemBySlot(EquipmentSlot.HEAD), helmet)
                && matches(entity.getItemBySlot(EquipmentSlot.CHEST), chest)
                && matches(entity.getItemBySlot(EquipmentSlot.LEGS), legs)
                && matches(entity.getItemBySlot(EquipmentSlot.FEET), boots);
    }

    private static boolean isFullSetById(LivingEntity entity, String helmet, String chest, String legs, String boots) {
        return matchesId(entity.getItemBySlot(EquipmentSlot.HEAD), helmet)
                && matchesId(entity.getItemBySlot(EquipmentSlot.CHEST), chest)
                && matchesId(entity.getItemBySlot(EquipmentSlot.LEGS), legs)
                && matchesId(entity.getItemBySlot(EquipmentSlot.FEET), boots);
    }

    private static boolean matches(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.is(item);
    }

    private static boolean matchesId(ItemStack stack, String path) {
        if (stack.isEmpty()) {
            return false;
        }
        Item expected = ForgeRegistries.ITEMS.getValue(new ResourceLocation(RefStrings.MODID, path));
        return expected != null && stack.is(expected);
    }
}
