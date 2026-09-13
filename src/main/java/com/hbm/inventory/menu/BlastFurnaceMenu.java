package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.BlastFurnaceBlockEntity;
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

public class BlastFurnaceMenu extends AbstractContainerMenu {
    private final BlastFurnaceBlockEntity be;
    private final ContainerData data;

    public BlastFurnaceMenu(int id, Inventory inv, BlastFurnaceBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public BlastFurnaceMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, BlastFurnaceBlockEntity.class, BlastFurnaceBlockEntity::new),
                new SimpleContainerData(4));
    }

    private BlastFurnaceMenu(int id, Inventory inv, BlastFurnaceBlockEntity be, ContainerData data) {
        super(ModMenus.BLAST_FURNACE.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), 0, 80, 81));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 80, 27));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 80, 45));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 134, 72) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 4, 134, 90) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        addPlayerInventory(inv, 140, 198);
        addDataSlots(this.data);
    }

    private static ContainerData createData(BlastFurnaceBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getFuel();
                    case 1 -> Math.round(be.getProgress() * 10000.0F);
                    case 2 -> Math.round(be.getSpeed() * 100.0F);
                    case 3 -> be.isProgressing() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
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

    public BlastFurnaceBlockEntity getBlockEntity() {
        return be;
    }

    public int getFuel() {
        return data.get(0);
    }

    public float getProgress() {
        return data.get(1) / 10000.0F;
    }

    public float getSpeed() {
        return data.get(2) / 100.0F;
    }

    public boolean isProgressing() {
        return data.get(3) != 0;
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
        if (index <= 4) {
            if (!this.moveItemStackTo(stack, 5, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (BlastFurnaceBlockEntity.getBurnTime(result) > 0) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 1, 3, false)) {
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
