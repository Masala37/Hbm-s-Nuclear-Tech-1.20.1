package com.hbm.inventory.menu;

import com.hbm.blockentity.bomb.NukeFstbmbBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class NukeFstbmbMenu extends AbstractContainerMenu {
    private final NukeFstbmbBlockEntity be;

    public NukeFstbmbMenu(int id, Inventory inv, NukeFstbmbBlockEntity be) {
        super(ModMenus.NUKE_FSTBMB.get(), id);
        this.be = be;

        this.addSlot(new SlotItemHandler(be.getItems(), NukeFstbmbBlockEntity.SLOT_EGG, 17, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), NukeFstbmbBlockEntity.SLOT_BATTERY, 53, 36));

        addPlayerInventory(inv, 140, 198);
    }

    public NukeFstbmbMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, NukeFstbmbBlockEntity.class, NukeFstbmbBlockEntity::new));
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

    public NukeFstbmbBlockEntity getBlockEntity() {
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
            if (index < NukeFstbmbBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, NukeFstbmbBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, NukeFstbmbBlockEntity.SLOT_COUNT, false)) {
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
