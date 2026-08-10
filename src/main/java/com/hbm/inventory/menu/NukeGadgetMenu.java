package com.hbm.inventory.menu;

import com.hbm.blockentity.bomb.NukeGadgetBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class NukeGadgetMenu extends AbstractContainerMenu {
    private final NukeGadgetBlockEntity be;
    private final ContainerLevelAccess access;

    public NukeGadgetMenu(int id, Inventory inv, NukeGadgetBlockEntity be) {
        super(ModMenus.NUKE_GADGET.get(), id);
        this.be = be;
        this.access = ContainerLevelAccess.create(inv.player.level(), be.getBlockPos());

        this.addSlot(new SlotItemHandler(be.getItems(), 0, 26, 35));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 8, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 44, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 8, 53));
        this.addSlot(new SlotItemHandler(be.getItems(), 4, 44, 53));
        this.addSlot(new SlotItemHandler(be.getItems(), 5, 98, 35));

        addPlayerInventory(inv, 84, 142);
    }

    public NukeGadgetMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, NukeGadgetBlockEntity.class, NukeGadgetBlockEntity::new));
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

    public NukeGadgetBlockEntity getBlockEntity() {
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
            if (index < NukeGadgetBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, NukeGadgetBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, NukeGadgetBlockEntity.SLOT_COUNT, false)) {
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
