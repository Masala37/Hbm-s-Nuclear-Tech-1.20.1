package com.hbm.blockentity.machine;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

/** Container transfers must leave room for the entire returned container before filling a tank. */
public final class LauncherFluidTransfer {
    private LauncherFluidTransfer() {
    }

    public static boolean canStore(ItemStack output, ItemStack remainder, int slotLimit) {
        if (remainder.isEmpty()) {
            return true;
        }
        return canStore(output.getCount(), remainder.getCount(), remainder.getMaxStackSize(), slotLimit,
                output.isEmpty() || ItemStack.isSameItemSameTags(output, remainder));
    }

    static boolean canStore(int outputCount, int remainderCount, int maxStackSize, int slotLimit, boolean compatible) {
        if (remainderCount == 0) {
            return true;
        }
        return compatible && remainderCount <= Math.min(slotLimit, maxStackSize) - outputCount;
    }

    public static boolean emptyContainer(ItemStackHandler items, int inputSlot, int outputSlot, FluidTank tank) {
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.isEmpty()) {
            return false;
        }
        // Forge simulates the tank transfer but drains a copy of the container to preview its remainder.
        var preview = FluidUtil.tryEmptyContainer(input, tank, 1000, null, false);
        ItemStack output = items.getStackInSlot(outputSlot);
        if (!preview.isSuccess() || !canStore(output, preview.getResult(), items.getSlotLimit(outputSlot))) {
            return false;
        }
        var result = FluidUtil.tryEmptyContainer(input, tank, 1000, null, true);
        if (!result.isSuccess()) {
            return false;
        }
        ItemStack remainder = result.getResult();
        if (!remainder.isEmpty()) {
            ItemStack combined = output.isEmpty() ? remainder.copy() : output.copy();
            if (!output.isEmpty()) {
                combined.grow(remainder.getCount());
            }
            items.setStackInSlot(outputSlot, combined);
        }
        items.setStackInSlot(inputSlot, input.copyWithCount(input.getCount() - 1));
        return true;
    }

    public static boolean fillContainer(ItemStackHandler items, int inputSlot, int outputSlot, FluidTank tank) {
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.isEmpty() || tank.getFluid().isEmpty()) {
            return false;
        }
        var preview = FluidUtil.tryFillContainer(input, tank, 1000, null, false);
        ItemStack output = items.getStackInSlot(outputSlot);
        if (!preview.isSuccess() || !canStore(output, preview.getResult(), items.getSlotLimit(outputSlot))) {
            return false;
        }
        var result = FluidUtil.tryFillContainer(input, tank, 1000, null, true);
        if (!result.isSuccess()) {
            return false;
        }
        ItemStack remainder = result.getResult();
        if (!remainder.isEmpty()) {
            ItemStack combined = output.isEmpty() ? remainder.copy() : output.copy();
            if (!output.isEmpty()) {
                combined.grow(remainder.getCount());
            }
            items.setStackInSlot(outputSlot, combined);
        }
        items.setStackInSlot(inputSlot, input.copyWithCount(input.getCount() - 1));
        return true;
    }
}
