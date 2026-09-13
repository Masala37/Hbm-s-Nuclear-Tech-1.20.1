package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.RtgBlockEntity;
import com.hbm.items.machine.ItemRTGPellet;
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

public class RtgMenu extends AbstractContainerMenu {
    private final RtgBlockEntity be;
    private final ContainerData data;

    public RtgMenu(int id, Inventory inv, RtgBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public RtgMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, RtgBlockEntity.class, RtgBlockEntity::new),
                new SimpleContainerData(4));
    }

    private RtgMenu(int id, Inventory inv, RtgBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_RTG.get(), id);
        this.be = be;
        this.data = data;
        int[] xs = {16, 34, 52, 70, 88};
        int[] ys = {18, 36, 54};
        int slot = 0;
        for (int y : ys) {
            for (int x : xs) {
                this.addSlot(new SlotItemHandler(be.getItems(), slot++, x, y) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return stack.getItem() instanceof ItemRTGPellet;
                    }
                });
            }
        }
        addPlayerInventory(inv, 106, 164);
        addDataSlots(this.data);
    }

    private static ContainerData createData(RtgBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getHeat();
                    case 1 -> be.heatMax();
                    case 2 -> be.getEnergy().getEnergyStored();
                    case 3 -> be.getEnergy().getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // heat is written from the server tick; GUI is read-only
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

    public RtgBlockEntity getBlockEntity() {
        return be;
    }

    public int getHeat() {
        return data.get(0);
    }

    public int getHeatMax() {
        return data.get(1);
    }

    public int getEnergy() {
        return data.get(2);
    }

    public int getMaxEnergy() {
        return data.get(3);
    }

    public boolean hasHeat() {
        return getHeat() > 0;
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
        if (index <= 14) {
            if (!this.moveItemStackTo(stack, 15, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 15, false)) {
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
