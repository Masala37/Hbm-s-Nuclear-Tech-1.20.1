package com.hbm.handler;

import com.hbm.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Radiation resistance registry (legacy HazmatRegistry subset for yellow/grey/red hazmat).
 */
public final class HazmatRegistry {
    private static final Map<Item, Double> ENTRIES = new IdentityHashMap<>();

    public static final double HELMET = 0.2D;
    public static final double CHEST = 0.4D;
    public static final double LEGS = 0.3D;
    public static final double BOOTS = 0.1D;

    private static boolean initialized;

    private HazmatRegistry() {
    }

    public static void initDefault() {
        if (initialized) {
            return;
        }
        initialized = true;

        double hazYellow = 0.6D;
        double hazRed = 1.0D;
        double hazGray = 2.0D;

        register(ModItems.HAZMAT_HELMET.get(), hazYellow * HELMET);
        register(ModItems.HAZMAT_PLATE.get(), hazYellow * CHEST);
        register(ModItems.HAZMAT_LEGS.get(), hazYellow * LEGS);
        register(ModItems.HAZMAT_BOOTS.get(), hazYellow * BOOTS);

        register(ModItems.HAZMAT_HELMET_RED.get(), hazRed * HELMET);
        register(ModItems.HAZMAT_PLATE_RED.get(), hazRed * CHEST);
        register(ModItems.HAZMAT_LEGS_RED.get(), hazRed * LEGS);
        register(ModItems.HAZMAT_BOOTS_RED.get(), hazRed * BOOTS);

        register(ModItems.HAZMAT_HELMET_GREY.get(), hazGray * HELMET);
        register(ModItems.HAZMAT_PLATE_GREY.get(), hazGray * CHEST);
        register(ModItems.HAZMAT_LEGS_GREY.get(), hazGray * LEGS);
        register(ModItems.HAZMAT_BOOTS_GREY.get(), hazGray * BOOTS);
    }

    public static void register(Item item, double resistance) {
        ENTRIES.put(item, resistance);
    }

    public static double getResistance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0D;
        }
        initDefault();
        return ENTRIES.getOrDefault(stack.getItem(), 0.0D);
    }

    public static float getResistance(Player player) {
        initDefault();
        float res = 0.0F;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            res += (float) getResistance(player.getItemBySlot(slot));
        }
        return res;
    }
}
