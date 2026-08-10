package com.hbm.inventory.menu;

import com.hbm.blockentity.bomb.NukeMikeBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class NukeMikeMenu extends AbstractContainerMenu {
    private final NukeMikeBlockEntity be;
    private final ContainerLevelAccess access;

    public NukeMikeMenu(int id, Inventory inv, NukeMikeBlockEntity be) {
        super(ModMenus.NUKE_MIKE.get(), id);
        this.be = be;
        this.access = ContainerLevelAccess.create(inv.player.level(), be.getBlockPos());

        this.addSlot(new SlotItemHandler(be.getItems(), 0, 26, 83));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 26, 101));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 44, 83));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 44, 101));
        this.addSlot(new SlotItemHandler(be.getItems(), 4, 39, 35));
        this.addSlot(new SlotItemHandler(be.getItems(), 5, 98, 91));
        this.addSlot(new SlotItemHandler(be.getItems(), 6, 116, 91));
        this.addSlot(new SlotItemHandler(be.getItems(), 7, 134, 91));

        addPlayerInventory(inv, 135, 193);
    }

    public NukeMikeMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, NukeMikeBlockEntity.class, NukeMikeBlockEntity::new));
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

    public NukeMikeBlockEntity getBlockEntity() {
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
            if (index < NukeMikeBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, NukeMikeBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, NukeMikeBlockEntity.SLOT_COUNT, false)) {
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
