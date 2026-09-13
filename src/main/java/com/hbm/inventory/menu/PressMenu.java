package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.PressBlockEntity;
import com.hbm.items.machine.ItemStamp;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class PressMenu extends AbstractContainerMenu {
    private final PressBlockEntity be;
    private final ContainerData data;

    public PressMenu(int id, Inventory inv, PressBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public PressMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, PressBlockEntity.class, PressBlockEntity::new),
                new SimpleContainerData(4));
    }

    private PressMenu(int id, Inventory inv, PressBlockEntity be, ContainerData data) {
        super(ModMenus.PRESS.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), 0, 26, 53));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 80, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 80, 53));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 140, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        for (int i = 0; i < 9; i++) {
            this.addSlot(new SlotItemHandler(be.getItems(), 4 + i, 8 + i * 18, 84));
        }
        addPlayerInventory(inv, 120, 178);
        addDataSlots(this.data);
    }

    private static ContainerData createData(PressBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getSpeed();
                    case 1 -> be.getBurnTime();
                    case 2 -> be.getPress();
                    case 3 -> PressBlockEntity.MAX_PRESS;
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

    public PressBlockEntity getBlockEntity() {
        return be;
    }

    public int getSpeed() {
        return data.get(0);
    }

    public int getBurnTime() {
        return data.get(1);
    }

    public int getPressProgress() {
        return data.get(2);
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
        if (index <= 12) {
            if (!this.moveItemStackTo(stack, 13, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ForgeHooks.getBurnTime(result, RecipeType.SMELTING) > 0) {
            if (!this.moveItemStackTo(stack, 0, 1, false) && !this.moveItemStackTo(stack, 4, 13, false)) {
                return ItemStack.EMPTY;
            }
        } else if (result.getItem() instanceof ItemStamp) {
            if (!this.moveItemStackTo(stack, 1, 2, false) && !this.moveItemStackTo(stack, 4, 13, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 2, 3, false) && !this.moveItemStackTo(stack, 4, 13, false)) {
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
