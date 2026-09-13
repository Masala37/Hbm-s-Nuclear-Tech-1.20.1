package com.hbm.inventory.menu;

import com.hbm.blockentity.network.CraneInserterBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CraneInserterMenu extends AbstractContainerMenu {
    private final CraneInserterBlockEntity be;
    private final ContainerData data;

    public CraneInserterMenu(int id, Inventory inv, CraneInserterBlockEntity be) {
        this(id, inv, be, data(be));
    }

    public CraneInserterMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, CraneInserterBlockEntity.class,
                CraneInserterBlockEntity::new), new SimpleContainerData(1));
    }

    private CraneInserterMenu(int id, Inventory inv, CraneInserterBlockEntity be, ContainerData data) {
        super(ModMenus.CRANE_INSERTER.get(), id);
        this.be = be;
        this.data = data;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 7; col++) {
                this.addSlot(new SlotItemHandler(be.getItems(), col + row * 7, 8 + col * 18, 17 + row * 18));
            }
        }
        addPlayerInventory(inv, 103, 161);
        addDataSlots(this.data);
    }

    private static ContainerData data(CraneInserterBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return be.isDestroyer() ? 1 : 0;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    public CraneInserterBlockEntity getBlockEntity() {
        return be;
    }

    public boolean isDestroyer() {
        return data.get(0) != 0;
    }

    private void addPlayerInventory(Inventory inv, int invY, int hotbarY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return MenuValidity.boundToBlock(player, be);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < CraneInserterBlockEntity.SLOTS) {
            if (!this.moveItemStackTo(stack, CraneInserterBlockEntity.SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, CraneInserterBlockEntity.SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }
}
