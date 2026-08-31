package com.hbm.inventory.menu;

import api.hbm.item.IDesignatorItem;
import com.hbm.blockentity.machine.LaunchPadRustedBlockEntity;
import com.hbm.registry.ModItems;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class LaunchPadRustedMenu extends AbstractContainerMenu {
    private final LaunchPadRustedBlockEntity be;

    public LaunchPadRustedMenu(int id, Inventory inv, LaunchPadRustedBlockEntity be) {
        super(ModMenus.LAUNCH_PAD_RUSTED.get(), id);
        this.be = be;
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadRustedBlockEntity.SLOT_OUTPUT, 26, 72) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadRustedBlockEntity.SLOT_CODE, 116, 45) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(ModItems.LAUNCH_CODE.get());
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadRustedBlockEntity.SLOT_KEY, 134, 45) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(ModItems.LAUNCH_KEY.get());
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadRustedBlockEntity.SLOT_DESIGNATOR, 26, 99) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof IDesignatorItem;
            }
        });
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 154 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 212));
        }
    }

    public LaunchPadRustedMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, LaunchPadRustedBlockEntity.class,
                LaunchPadRustedBlockEntity::new));
    }

    public LaunchPadRustedBlockEntity getBlockEntity() {
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
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < 4) {
            if (!this.moveItemStackTo(stack, 4, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved;
            if (stack.is(ModItems.LAUNCH_CODE.get())) {
                moved = this.moveItemStackTo(stack, 1, 2, false);
            } else if (stack.is(ModItems.LAUNCH_KEY.get())) {
                moved = this.moveItemStackTo(stack, 2, 3, false);
            } else if (stack.getItem() instanceof IDesignatorItem) {
                moved = this.moveItemStackTo(stack, 3, 4, false);
            } else {
                return ItemStack.EMPTY;
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }
}
