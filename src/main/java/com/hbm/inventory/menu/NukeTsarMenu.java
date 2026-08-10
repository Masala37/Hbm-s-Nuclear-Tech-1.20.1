package com.hbm.inventory.menu;

import com.hbm.blockentity.bomb.NukeTsarBlockEntity;
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

public class NukeTsarMenu extends AbstractContainerMenu {
    private final NukeTsarBlockEntity be;
    private final ContainerLevelAccess access;

    public NukeTsarMenu(int id, Inventory inv, NukeTsarBlockEntity be) {
        super(ModMenus.NUKE_TSAR.get(), id);
        this.be = be;
        this.access = ContainerLevelAccess.create(inv.player.level(), be.getBlockPos());

        this.addSlot(new SlotItemHandler(be.getItems(), 0, 48, 101));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 66, 101));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 84, 101));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 102, 101));
        this.addSlot(new SlotItemHandler(be.getItems(), 4, 55, 51));
        this.addSlot(new SlotItemHandler(be.getItems(), 5, 138, 101));

        addPlayerInventory(inv, 151, 209);
    }

    public NukeTsarMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, NukeTsarBlockEntity.class, NukeTsarBlockEntity::new));
    }

    private void addPlayerInventory(Inventory inv, int invY, int hotbarY) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 48 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 48 + col * 18, hotbarY));
        }
    }

    public NukeTsarBlockEntity getBlockEntity() {
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
            if (index < NukeTsarBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, NukeTsarBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, NukeTsarBlockEntity.SLOT_COUNT, false)) {
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
