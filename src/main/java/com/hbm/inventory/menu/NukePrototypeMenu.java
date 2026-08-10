package com.hbm.inventory.menu;

import com.hbm.blockentity.bomb.NukePrototypeBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class NukePrototypeMenu extends AbstractContainerMenu {
    private final NukePrototypeBlockEntity be;

    public NukePrototypeMenu(int id, Inventory inv, NukePrototypeBlockEntity be) {
        super(ModMenus.NUKE_PROTOTYPE.get(), id);
        this.be = be;

        this.addSlot(new SlotItemHandler(be.getItems(), 0, 8, 35));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 26, 35));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 44, 26));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 44, 44));
        this.addSlot(new SlotItemHandler(be.getItems(), 4, 62, 26));
        this.addSlot(new SlotItemHandler(be.getItems(), 5, 62, 44));
        this.addSlot(new SlotItemHandler(be.getItems(), 6, 80, 26));
        this.addSlot(new SlotItemHandler(be.getItems(), 7, 80, 44));
        this.addSlot(new SlotItemHandler(be.getItems(), 8, 98, 26));
        this.addSlot(new SlotItemHandler(be.getItems(), 9, 98, 44));
        this.addSlot(new SlotItemHandler(be.getItems(), 10, 116, 26));
        this.addSlot(new SlotItemHandler(be.getItems(), 11, 116, 44));
        this.addSlot(new SlotItemHandler(be.getItems(), 12, 134, 35));
        this.addSlot(new SlotItemHandler(be.getItems(), 13, 152, 35));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    public NukePrototypeMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, NukePrototypeBlockEntity.class, NukePrototypeBlockEntity::new));
    }

    public NukePrototypeBlockEntity getBlockEntity() {
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
            if (index < NukePrototypeBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, NukePrototypeBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, NukePrototypeBlockEntity.SLOT_COUNT, false)) {
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
