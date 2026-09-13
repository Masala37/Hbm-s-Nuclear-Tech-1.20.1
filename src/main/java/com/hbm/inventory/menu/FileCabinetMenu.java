package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.FileCabinetBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/** 1.7 {@code ContainerFileCabinet}: 2×4 cabinet slots. */
public class FileCabinetMenu extends AbstractContainerMenu {
    private final FileCabinetBlockEntity be;

    public FileCabinetMenu(int id, Inventory inv, FileCabinetBlockEntity be) {
        super(ModMenus.FILE_CABINET.get(), id);
        this.be = be;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                this.addSlot(new SlotItemHandler(be.getItems(), col + row * 4, 53 + col * 18, 18 + row * 36));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 88 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 146));
        }
        be.startOpen();
    }

    public FileCabinetMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, FileCabinetBlockEntity.class, FileCabinetBlockEntity::new));
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return HbmMenuHelper.stillValid(player, be);
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 8) {
                if (!this.moveItemStackTo(stack, 8, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 8, false)) {
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

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        be.stopOpen();
    }
}
