package com.hbm.inventory.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 1.7.10 {@code ModuleBurnTime} bonuses for {@code TileEntityHeaterFirebox}.
 */
public final class FireboxBurnTime {
    public static final double LIGNITE_TIME = FireboxFuelMods.LIGNITE_TIME;
    public static final double COAL_TIME = FireboxFuelMods.COAL_TIME;
    public static final double COKE_TIME = FireboxFuelMods.COKE_TIME;
    public static final double SOLID_TIME = FireboxFuelMods.SOLID_TIME;
    public static final double ROCKET_TIME = FireboxFuelMods.ROCKET_TIME;
    public static final double BALEFIRE_TIME = FireboxFuelMods.BALEFIRE_TIME;

    public static final double LIGNITE_HEAT = FireboxFuelMods.LIGNITE_HEAT;
    public static final double COAL_HEAT = FireboxFuelMods.COAL_HEAT;
    public static final double COKE_HEAT = FireboxFuelMods.COKE_HEAT;
    public static final double SOLID_HEAT = FireboxFuelMods.SOLID_HEAT;
    public static final double ROCKET_HEAT = FireboxFuelMods.ROCKET_HEAT;
    public static final double BALEFIRE_HEAT = FireboxFuelMods.BALEFIRE_HEAT;

    private FireboxBurnTime() {
    }

    public static int burnTime(ItemStack stack, double timeMult) {
        if (stack.isEmpty()) {
            return 0;
        }
        int fuel = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
        if (fuel <= 0) {
            return 0;
        }
        return (int) (fuel * timeMod(stack) * timeMult);
    }

    public static int burnHeat(int baseHeat, ItemStack stack) {
        if (stack.isEmpty() || baseHeat <= 0) {
            return 0;
        }
        return (int) (baseHeat * heatMod(stack));
    }

    public static double timeMod(ItemStack stack) {
        return FireboxFuelMods.fromPath(path(stack))[0];
    }

    public static double heatMod(ItemStack stack) {
        return FireboxFuelMods.fromPath(path(stack))[1];
    }

    public static double[] modsFromPath(String path) {
        return FireboxFuelMods.fromPath(path);
    }

    private static String path(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key == null ? "" : key.getPath();
    }
}
