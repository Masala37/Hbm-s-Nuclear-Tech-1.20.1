package com.hbm.inventory.menu;

import com.hbm.blockentity.bomb.NukeBoyBlockEntity;
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

public class NukeBoyMenu extends AbstractContainerMenu {
    private final NukeBoyBlockEntity be;
    private final ContainerLevelAccess access;

    public NukeBoyMenu(int id, Inventory inv, NukeBoyBlockEntity be) {
        super(ModMenus.NUKE_BOY.get(), id);
        this.be = be;
        this.access = ContainerLevelAccess.create(inv.player.level(), be.getBlockPos());

        this.addSlot(new SlotItemHandler(be.getItems(), 0, 26, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 44, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 62, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 80, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 4, 98, 36));

        addPlayerInventory(inv, 140, 198);
    }

    public NukeBoyMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, NukeBoyBlockEntity.class, NukeBoyBlockEntity::new));
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

    public NukeBoyBlockEntity getBlockEntity() {
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
            if (index < NukeBoyBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, NukeBoyBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, NukeBoyBlockEntity.SLOT_COUNT, false)) {
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
