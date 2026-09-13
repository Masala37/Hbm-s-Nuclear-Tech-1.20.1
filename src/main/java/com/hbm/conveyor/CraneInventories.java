package com.hbm.conveyor;

import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Shared insert/extract against neighbor {@link IItemHandler}s (1.7 {@code CraneInserter.addToInventory}).
 */
public final class CraneInventories {
    private CraneInventories() {
    }

    @Nullable
    public static IItemHandler handler(BlockEntity be, Direction fromCrane) {
        if (be == null) {
            return null;
        }
        Direction query = be instanceof AbstractFurnaceBlockEntity ? Direction.DOWN : fromCrane.getOpposite();
        return be.getCapability(ForgeCapabilities.ITEM_HANDLER, query).orElse(null);
    }

    public static ItemStack insert(IItemHandler handler, ItemStack toAdd) {
        if (handler == null || toAdd.isEmpty()) {
            return toAdd;
        }
        return ItemHandlerHelper.insertItemStacked(handler, toAdd, false);
    }

    public static SimpleContainer asContainer(ItemStackHandler items) {
        SimpleContainer container = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) {
            container.setItem(i, items.getStackInSlot(i));
        }
        return container;
    }

    public static boolean matchesFilter(ItemStack stack, ItemStack filter) {
        return !filter.isEmpty() && ItemStack.isSameItemSameTags(stack, filter);
    }
}
