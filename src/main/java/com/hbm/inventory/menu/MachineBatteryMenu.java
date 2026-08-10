package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.MachineBatteryBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MachineBatteryMenu extends AbstractContainerMenu {
    private final MachineBatteryBlockEntity be;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public MachineBatteryMenu(int id, Inventory inv, MachineBatteryBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public MachineBatteryMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, MachineBatteryBlockEntity.class, MachineBatteryBlockEntity::new),
                new SimpleContainerData(2));
    }

    private MachineBatteryMenu(int id, Inventory inv, MachineBatteryBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_BATTERY.get(), id);
        this.be = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(inv.player.level(), be.getBlockPos());

        addPlayerInventory(inv, 84, 142);
        addDataSlots(this.data);
    }

    private static ContainerData createData(MachineBatteryBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
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

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public MachineBatteryBlockEntity getBlockEntity() {
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
            if (index < 27) {
                if (!this.moveItemStackTo(stack, 27, 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 27, false)) {
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
