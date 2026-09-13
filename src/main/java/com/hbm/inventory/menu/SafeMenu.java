package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.SafeBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/** 1.7 {@code ContainerSafe}: 3×5 slots. */
public class SafeMenu extends AbstractContainerMenu {
    private final SafeBlockEntity be;

    public SafeMenu(int id, Inventory inv, SafeBlockEntity be) {
        super(ModMenus.SAFE.get(), id);
        this.be = be;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                this.addSlot(new SlotItemHandler(be.getItems(), col + row * 5, 44 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 144));
        }
        be.startOpen(inv.player);
    }

    public SafeMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, SafeBlockEntity.class, SafeBlockEntity::new));
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
            if (index < 15) {
                if (!this.moveItemStackTo(stack, 15, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 15, false)) {
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

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        be.stopOpen(player);
    }
}
