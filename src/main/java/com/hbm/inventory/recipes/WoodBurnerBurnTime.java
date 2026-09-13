package com.hbm.inventory.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 1.7.10 {@code ModuleBurnTime.setLogTimeMod(4).setWoodTimeMod(2)} for the wood burner.
 */
public final class WoodBurnerBurnTime {
    private WoodBurnerBurnTime() {
    }

    public static int burnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        int fuel = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
        if (fuel <= 0) {
            return 0;
        }
        return (int) (fuel * WoodBurnerFuelMods.timeMod(path(stack)));
    }

    private static String path(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key == null ? "" : key.getPath();
    }
}
