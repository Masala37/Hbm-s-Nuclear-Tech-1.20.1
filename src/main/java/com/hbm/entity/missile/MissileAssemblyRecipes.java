package com.hbm.entity.missile;

import com.hbm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * Size/preset missile assembly resolver.
 * <p>
 * Logic adapted from HBM-Modernized {@code MissileAssemblyBlockEntity} (GPL-3.0):
 * warhead size must match thruster size; output is a registered preset missile.
 */
public final class MissileAssemblyRecipes {
    public enum Size {
        SMALL,
        MEDIUM,
        LARGE,
        NUCLEAR
    }

    private MissileAssemblyRecipes() {
    }

    public static boolean isChip(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String path = path(stack.getItem());
        return path != null && (path.equals("missile_chip") || path.contains("circuit"));
    }

    public static boolean isFuselage(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String path = path(stack.getItem());
        return path != null && path.contains("fuselage");
    }

    public static boolean isFins(ItemStack stack) {
        if (stack.isEmpty()) {
            return true; // optional
        }
        String path = path(stack.getItem());
        return path != null && path.startsWith("fins_");
    }

    public static boolean isWarhead(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String path = path(stack.getItem());
        return path != null && path.startsWith("warhead_");
    }

    public static boolean isThruster(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String path = path(stack.getItem());
        return path != null && path.startsWith("thruster_");
    }

    @Nullable
    public static Size sizeOf(ItemStack stack) {
        String path = path(stack.getItem());
        if (path == null) {
            return null;
        }
        if (path.contains("nuclear") || path.contains("mirv") || path.contains("volcano")) {
            return Size.NUCLEAR;
        }
        if (path.contains("_large")) {
            return Size.LARGE;
        }
        if (path.contains("_medium")) {
            return Size.MEDIUM;
        }
        if (path.contains("_small") || path.equals("thruster_small")) {
            return Size.SMALL;
        }
        // Bare thruster_medium style already handled; thruster_large etc.
        if (path.equals("thruster_large")) {
            return Size.LARGE;
        }
        if (path.equals("thruster_medium")) {
            return Size.MEDIUM;
        }
        if (path.equals("thruster_nuclear")) {
            return Size.NUCLEAR;
        }
        return Size.MEDIUM;
    }

    /**
     * @return resulting missile item or empty if parts are invalid
     */
    public static ItemStack resolve(ItemStack chip, ItemStack warhead, ItemStack fuselage,
                                    ItemStack fins, ItemStack thruster) {
        if (!isChip(chip) || !isWarhead(warhead) || !isFuselage(fuselage) || !isThruster(thruster)) {
            return ItemStack.EMPTY;
        }
        if (!fins.isEmpty() && !isFins(fins)) {
            return ItemStack.EMPTY;
        }
        Size warheadSize = sizeOf(warhead);
        Size thrusterSize = sizeOf(thruster);
        if (warheadSize == null || thrusterSize == null || warheadSize != thrusterSize) {
            return ItemStack.EMPTY;
        }
        Item result = resolveMissile(warhead.getItem(), warheadSize);
        return result == null ? ItemStack.EMPTY : new ItemStack(result);
    }

    @Nullable
    private static Item resolveMissile(Item warhead, Size size) {
        String path = path(warhead);
        if (path == null) {
            return null;
        }
        boolean incendiary = path.contains("incendiary");
        boolean cluster = path.contains("cluster");
        boolean buster = path.contains("buster");

        return switch (size) {
            case SMALL, MEDIUM -> {
                if (incendiary) {
                    yield ModItems.MISSILE_INCENDIARY.get();
                }
                if (cluster) {
                    yield ModItems.MISSILE_CLUSTER.get();
                }
                if (buster) {
                    yield ModItems.MISSILE_BUSTER.get();
                }
                yield ModItems.MISSILE_GENERIC.get();
            }
            case LARGE -> {
                if (incendiary) {
                    yield ModItems.MISSILE_INCENDIARY_STRONG.get();
                }
                if (cluster) {
                    yield ModItems.MISSILE_CLUSTER_STRONG.get();
                }
                if (buster) {
                    yield ModItems.MISSILE_BUSTER_STRONG.get();
                }
                yield ModItems.MISSILE_STRONG.get();
            }
            case NUCLEAR -> ModItems.MISSILE_NUCLEAR.get(); // not launchable yet — still craftable catalog
        };
    }

    @Nullable
    private static String path(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key == null ? null : key.getPath();
    }
}
