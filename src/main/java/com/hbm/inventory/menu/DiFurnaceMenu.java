package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.DiFurnaceBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class DiFurnaceMenu extends AbstractContainerMenu {
    private final DiFurnaceBlockEntity be;
    private final ContainerData data;

    public DiFurnaceMenu(int id, Inventory inv, DiFurnaceBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public DiFurnaceMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, DiFurnaceBlockEntity.class, DiFurnaceBlockEntity::new),
                new SimpleContainerData(6));
    }

    private DiFurnaceMenu(int id, Inventory inv, DiFurnaceBlockEntity be, ContainerData data) {
        super(ModMenus.DI_FURNACE.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), 0, 80, 18));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 80, 54));
        this.addSlot(new SlotItemHandler(be.getItems(), 2, 8, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), 3, 134, 36) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        addPlayerInventory(inv, 84, 142);
        addDataSlots(this.data);
    }

    private static ContainerData createData(DiFurnaceBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getProgress();
                    case 1 -> be.getFuel();
                    case 2 -> be.canProcess() ? 1 : 0;
                    case 3 -> be.getSideUpper();
                    case 4 -> be.getSideLower();
                    case 5 -> be.getSideFuel();
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

    public DiFurnaceBlockEntity getBlockEntity() {
        return be;
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getFuel() {
        return data.get(1);
    }

    public boolean canProcess() {
        return data.get(2) != 0;
    }

    public int getSideUpper() {
        return data.get(3);
    }

    public int getSideLower() {
        return data.get(4);
    }

    public int getSideFuel() {
        return data.get(5);
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId >= 0 && slotId < 3 && button == 1 && clickType == ClickType.PICKUP) {
            Slot slot = this.getSlot(slotId);
            if (!slot.hasItem() && getCarried().isEmpty()) {
                if (!player.level().isClientSide) {
                    be.cycleInputSide(slotId);
                }
                broadcastChanges();
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
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
        if (index <= 3) {
            if (!this.moveItemStackTo(stack, 4, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 3, false)) {
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
