package com.hbm.items.machine;

import com.hbm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * 1.7.10 {@code ItemRTGPelletDepleted}: one item, enum variants via NBT {@code type}
 * (and legacy {@code Damage} ordinal).
 */
public class ItemRTGPelletDepleted extends Item {
    public static final String TAG_TYPE = "type";

    public enum DepletedRTGMaterial {
        BISMUTH,
        MERCURY,
        NEPTUNIUM,
        LEAD,
        ZIRCONIUM,
        NICKEL
    }

    public ItemRTGPelletDepleted() {
        super(new Properties());
    }

    public static ItemStack stack(DepletedRTGMaterial material) {
        ItemStack stack = new ItemStack(ModItems.PELLET_RTG_DEPLETED.get());
        setType(stack, material);
        return stack;
    }

    public static void setType(ItemStack stack, DepletedRTGMaterial material) {
        stack.getOrCreateTag().putString(TAG_TYPE, material.name());
    }

    public static DepletedRTGMaterial getType(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemRTGPelletDepleted)) {
            return DepletedRTGMaterial.BISMUTH;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return DepletedRTGMaterial.BISMUTH;
        }
        if (tag.contains(TAG_TYPE)) {
            return byName(tag.getString(TAG_TYPE));
        }
        if (tag.contains("Damage")) {
            return byOrdinal(tag.getInt("Damage"));
        }
        return DepletedRTGMaterial.BISMUTH;
    }

    public static DepletedRTGMaterial byName(String name) {
        if (name == null || name.isEmpty()) {
            return DepletedRTGMaterial.BISMUTH;
        }
        try {
            return DepletedRTGMaterial.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return DepletedRTGMaterial.BISMUTH;
        }
    }

    public static DepletedRTGMaterial byOrdinal(int ordinal) {
        DepletedRTGMaterial[] values = DepletedRTGMaterial.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return DepletedRTGMaterial.BISMUTH;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return new ItemStack(ModItems.PLATE_IRON.get());
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.hbm.pellet_rtg_depleted."
                + getType(stack).name().toLowerCase(Locale.ROOT));
    }

    public static int modelIndex(@Nullable ItemStack stack) {
        return getType(stack == null ? ItemStack.EMPTY : stack).ordinal();
    }
}
