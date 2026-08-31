package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.RadarNTBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class RadarNTMenu extends AbstractContainerMenu {
    private final RadarNTBlockEntity be;

    public RadarNTMenu(int id, Inventory inv, RadarNTBlockEntity be) {
        super(ModMenus.RADAR_NT.get(), id);
        this.be = be;

        for (int i = 0; i < 8; i++) {
            this.addSlot(new SlotItemHandler(be.getItems(), i, 26 + i * 18, 17));
        }
        this.addSlot(new SlotItemHandler(be.getItems(), RadarNTBlockEntity.SLOT_LINKER, 26, 44));
        this.addSlot(new SlotItemHandler(be.getItems(), RadarNTBlockEntity.SLOT_BATTERY, 152, 44) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 103 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 161));
        }
    }

    public RadarNTMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, RadarNTBlockEntity.class, RadarNTBlockEntity::new));
    }

    public RadarNTBlockEntity getBlockEntity() {
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
        if (index <= 9) {
            if (!this.moveItemStackTo(stack, 10, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.isEnergyItem(stack)) {
            if (!this.moveItemStackTo(stack, 9, 10, false)) {
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
