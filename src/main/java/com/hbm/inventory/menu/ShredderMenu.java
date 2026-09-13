package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.ShredderBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.items.machine.ItemBlades;
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

public class ShredderMenu extends AbstractContainerMenu {
    private final ShredderBlockEntity be;
    private final ContainerData data;

    public ShredderMenu(int id, Inventory inv, ShredderBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public ShredderMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, ShredderBlockEntity.class, ShredderBlockEntity::new),
                new SimpleContainerData(5));
    }

    private ShredderMenu(int id, Inventory inv, ShredderBlockEntity be, ContainerData data) {
        super(ModMenus.SHREDDER.get(), id);
        this.be = be;
        this.data = data;
        int[] inX = {44, 62, 80};
        int[] inY = {18, 36, 54};
        int slot = 0;
        for (int y : inY) {
            for (int x : inX) {
                this.addSlot(new SlotItemHandler(be.getItems(), slot++, x, y));
            }
        }
        int[] outX = {116, 134, 152};
        int[] outY = {18, 36, 54, 72, 90, 108};
        for (int y : outY) {
            for (int x : outX) {
                int outSlot = slot++;
                this.addSlot(new SlotItemHandler(be.getItems(), outSlot, x, y) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        this.addSlot(new SlotItemHandler(be.getItems(), 27, 44, 108));
        this.addSlot(new SlotItemHandler(be.getItems(), 28, 80, 108));
        this.addSlot(new SlotItemHandler(be.getItems(), 29, 8, 108));
        addPlayerInventory(inv, 84 + 67, 142 + 67);
        addDataSlots(this.data);
    }

    private static ContainerData createData(ShredderBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getProgress();
                    case 3 -> be.getGearLeft();
                    case 4 -> be.getGearRight();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 5;
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

    public ShredderBlockEntity getBlockEntity() {
        return be;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getProgress() {
        return data.get(2);
    }

    public int getGearLeft() {
        return data.get(3);
    }

    public int getGearRight() {
        return data.get(4);
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
        if (index <= 29) {
            if (!this.moveItemStackTo(stack, 30, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.isEnergyItem(result)) {
            if (!this.moveItemStackTo(stack, 29, 30, false)) {
                return ItemStack.EMPTY;
            }
        } else if (result.getItem() instanceof ItemBlades) {
            if (!this.moveItemStackTo(stack, 27, 29, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 9, false)) {
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
