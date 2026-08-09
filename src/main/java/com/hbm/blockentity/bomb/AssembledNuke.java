package com.hbm.blockentity.bomb;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Shared inventory contract for assembled nuclear bombs.
 */
public interface AssembledNuke {
    boolean isReady();

    void clearSlots();

    void dropContents();

    Component statusMessage();

    int findInsertSlot(Item item);

    ItemStackHandler getItems();

    ItemStack[] copyStacks();

    /**
     * Yield radius for this assembly. Default uses the block's configured radius.
     * Ivy Mike uses a smaller primary yield until the secondary is fully loaded.
     */
    default int resolveBlastRadius(int configuredRadius) {
        return configuredRadius;
    }
}
