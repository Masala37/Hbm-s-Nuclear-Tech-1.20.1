package com.hbm.energy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * 1.7.10 {@code ColorUtil.getColorFromDye} firework-style ints.
 * Ore dict {@code dye*} is {@code forge:dyes/<color>} on 1.20.1 (bone meal, lapis, ink sac, …).
 */
public final class PylonDye {
    private PylonDye() {
    }

    public static int from(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof DyeItem dye) {
            return fromOreSuffix(dye.getDyeColor().getName());
        }
        for (DyeColor color : DyeColor.values()) {
            if (stack.is(dyeTag(color))) {
                return fromOreSuffix(color.getName());
            }
        }
        return 0;
    }

    /**
     * 1.7 ore suffix after {@code dye}, lowercased, with underscores stripped
     * ({@code dyeLightBlue} → {@code lightblue}).
     */
    public static int fromOreSuffix(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return 0;
        }
        return switch (suffix.toLowerCase(Locale.ROOT).replace("_", "")) {
            case "black" -> 1_973_019;
            case "red" -> 11_743_532;
            case "green" -> 3_887_386;
            case "brown" -> 5_322_730;
            case "blue" -> 2_437_522;
            case "purple" -> 8_073_150;
            case "cyan" -> 2_651_799;
            case "silver", "lightgray" -> 11_250_603;
            case "gray" -> 4_408_131;
            case "pink" -> 14_188_952;
            case "lime" -> 4_312_372;
            case "yellow" -> 14_602_026;
            case "lightblue" -> 6_719_955;
            case "magenta" -> 12_801_229;
            case "orange" -> 15_435_844;
            case "white" -> 15_790_320;
            default -> 0;
        };
    }

    private static TagKey<Item> dyeTag(DyeColor color) {
        return ItemTags.create(new ResourceLocation("forge", "dyes/" + color.getName()));
    }
}
