package com.hbm.inventory.menu;

import com.hbm.blockentity.bomb.NukeN2BlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class NukeN2Menu extends AbstractContainerMenu {
    private final NukeN2BlockEntity be;

    public NukeN2Menu(int id, Inventory inv, NukeN2BlockEntity be) {
        super(ModMenus.NUKE_N2.get(), id);
        this.be = be;

        this.addSlot(new SlotItemHandler(be.getItems(), 0, 98, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 116, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 134, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 98, 54));
        this.addSlot(new SlotItemHandler(be.getItems(), 4, 116, 54));
        this.addSlot(new SlotItemHandler(be.getItems(), 5, 134, 54));
        this.addSlot(new SlotItemHandler(be.getItems(), 6, 98, 72));
        this.addSlot(new SlotItemHandler(be.getItems(), 7, 116, 72));
        this.addSlot(new SlotItemHandler(be.getItems(), 8, 134, 72));
        this.addSlot(new SlotItemHandler(be.getItems(), 9, 98, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), 10, 116, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), 11, 134, 90));

        addPlayerInventory(inv, 140, 198);
    }

    public NukeN2Menu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, NukeN2BlockEntity.class, NukeN2BlockEntity::new));
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

    public NukeN2BlockEntity getBlockEntity() {
        return be;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return MenuValidity.closeEnough(player, be);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < NukeN2BlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, NukeN2BlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, NukeN2BlockEntity.SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }
}
