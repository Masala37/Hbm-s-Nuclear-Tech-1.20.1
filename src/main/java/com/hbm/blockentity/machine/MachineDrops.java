package com.hbm.blockentity.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

public final class MachineDrops {
    private MachineDrops() {
    }

    public static void drop(Level level, BlockPos pos, ItemStackHandler items) {
        SimpleContainer container = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) {
            container.setItem(i, items.getStackInSlot(i));
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
        Containers.dropContents(level, pos, container);
    }
}
