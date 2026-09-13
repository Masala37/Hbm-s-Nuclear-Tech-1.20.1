package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.MachineBatteryBlockEntity;
import com.hbm.energy.ConnectionPriority;
import com.hbm.energy.ItemChargeStorage;
import com.hbm.energy.ItemEnergyHelper;
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

public class MachineBatteryMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 7;
    private final MachineBatteryBlockEntity be;
    private final ContainerData data;

    public MachineBatteryMenu(int id, Inventory inv, MachineBatteryBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public MachineBatteryMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, MachineBatteryBlockEntity.class, MachineBatteryBlockEntity::new),
                new SimpleContainerData(DATA_COUNT));
    }

    private MachineBatteryMenu(int id, Inventory inv, MachineBatteryBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_BATTERY.get(), id);
        this.be = be;
        this.data = data;

        this.addSlot(new SlotItemHandler(be.getItems(), MachineBatteryBlockEntity.SLOT_DISCHARGE, 26, 17) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), MachineBatteryBlockEntity.SLOT_CHARGE, 26, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });
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
                    case 2 -> be.getRedLow();
                    case 3 -> be.getRedHigh();
                    case 4 -> be.getPriority().ordinal();
                    case 5 -> ItemChargeStorage.toInt(be.getDelta());
                    case 6 -> be.relevantMode();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
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

    public int getRedLow() {
        return data.get(2);
    }

    public int getRedHigh() {
        return data.get(3);
    }

    public ConnectionPriority getPriority() {
        ConnectionPriority[] values = ConnectionPriority.values();
        int ordinal = data.get(4);
        if (ordinal < 0 || ordinal >= values.length) {
            return ConnectionPriority.LOW;
        }
        return values[ordinal];
    }

    public int getDelta() {
        return data.get(5);
    }

    public int getMode() {
        return data.get(6);
    }

    public MachineBatteryBlockEntity getBlockEntity() {
        return be;
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
        if (index <= 1) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.isEnergyItem(result)) {
            if (!this.moveItemStackTo(stack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 29) {
            if (!this.moveItemStackTo(stack, 29, 38, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 2, 29, false)) {
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
        return result;
    }
}
