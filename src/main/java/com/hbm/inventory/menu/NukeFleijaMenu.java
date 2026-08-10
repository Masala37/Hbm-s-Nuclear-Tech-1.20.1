package com.hbm.inventory.menu;

import com.hbm.blockentity.bomb.NukeFleijaBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class NukeFleijaMenu extends AbstractContainerMenu {
    private final NukeFleijaBlockEntity be;

    public NukeFleijaMenu(int id, Inventory inv, NukeFleijaBlockEntity be) {
        super(ModMenus.NUKE_FLEIJA.get(), id);
        this.be = be;

        // Legacy ContainerNukeFleija slot layout
        this.addSlot(new SlotItemHandler(be.getItems(), 0, 8, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 152, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 44, 18));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 44, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 4, 44, 54));
        this.addSlot(new SlotItemHandler(be.getItems(), 5, 80, 18));
        this.addSlot(new SlotItemHandler(be.getItems(), 6, 98, 18));
        this.addSlot(new SlotItemHandler(be.getItems(), 7, 80, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 8, 98, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 9, 80, 54));
        this.addSlot(new SlotItemHandler(be.getItems(), 10, 98, 54));

        addPlayerInventory(inv, 140, 198);
    }

    public NukeFleijaMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, NukeFleijaBlockEntity.class, NukeFleijaBlockEntity::new));
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

    public NukeFleijaBlockEntity getBlockEntity() {
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
            if (index < NukeFleijaBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, NukeFleijaBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, NukeFleijaBlockEntity.SLOT_COUNT, false)) {
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
