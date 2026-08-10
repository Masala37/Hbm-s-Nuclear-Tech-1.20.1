package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.StorageCrateBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Generic crate inventory (36 or 54 slots) using the vanilla chest GUI layout.
 */
public class StorageCrateMenu extends AbstractContainerMenu {
    private final StorageCrateBlockEntity be;
    private final int rows;

    public StorageCrateMenu(int id, Inventory inv, StorageCrateBlockEntity be) {
        super(be.getItems().getSlots() > 36 ? ModMenus.STORAGE_CRATE_LARGE.get() : ModMenus.STORAGE_CRATE.get(), id);
        this.be = be;
        this.rows = be.getItems().getSlots() / 9;
        addCrateSlots();
        addPlayerInventory(inv, 18 + rows * 18, 18 + rows * 18 + 58);
    }

    public StorageCrateMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, StorageCrateBlockEntity.class, StorageCrateBlockEntity::new));
    }

    private void addCrateSlots() {
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new SlotItemHandler(be.getItems(), col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
    }

    private void addPlayerInventory(Inventory inv, int invY, int hotbarY) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, hotbarY));
        }
    }

    public int getRows() {
        return rows;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return HbmMenuHelper.stillValid(player, be);
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int crateSlots = rows * 9;
            if (index < crateSlots) {
                if (!this.moveItemStackTo(stack, crateSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, crateSlots, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }
}
