package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.FireboxBlockEntity;
import com.hbm.inventory.recipes.FireboxBurnTime;
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

public class FireboxMenu extends AbstractContainerMenu {
    private final FireboxBlockEntity be;
    private final ContainerData data;

    public FireboxMenu(int id, Inventory inv, FireboxBlockEntity be) {
        this(id, inv, be, createData(be));
        be.openLid();
    }

    public FireboxMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, FireboxBlockEntity.class, FireboxBlockEntity::new),
                new SimpleContainerData(6));
    }

    private FireboxMenu(int id, Inventory inv, FireboxBlockEntity be, ContainerData data) {
        super(ModMenus.HEATER_FIREBOX.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), 0, 44, 27));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 62, 27));
        addPlayerInventory(inv, 86, 144);
        addDataSlots(this.data);
    }

    private static ContainerData createData(FireboxBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getHeatStored();
                    case 1 -> FireboxBlockEntity.MAX_HEAT;
                    case 2 -> be.getBurnTime();
                    case 3 -> Math.max(be.getMaxBurnTime(), 1);
                    case 4 -> be.getBurnHeat();
                    case 5 -> be.wasOn() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 6;
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

    public FireboxBlockEntity getBlockEntity() {
        return be;
    }

    public int getHeat() {
        return data.get(0);
    }

    public int getMaxHeat() {
        return data.get(1);
    }

    public int getBurnTime() {
        return data.get(2);
    }

    public int getMaxBurnTime() {
        return data.get(3);
    }

    public int getBurnHeat() {
        return data.get(4);
    }

    public boolean wasOn() {
        return data.get(5) != 0;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        be.closeLid();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return MenuValidity.boundToBlock(player, be);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (FireboxBurnTime.burnTime(stack, FireboxBlockEntity.TIME_MULT) <= 0
                    || !this.moveItemStackTo(stack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }
}
