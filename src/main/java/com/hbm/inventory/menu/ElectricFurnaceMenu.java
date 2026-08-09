package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.ElectricFurnaceBlockEntity;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ElectricFurnaceMenu extends AbstractContainerMenu {
    private final ElectricFurnaceBlockEntity be;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ElectricFurnaceMenu(int id, Inventory inv, ElectricFurnaceBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public ElectricFurnaceMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (ElectricFurnaceBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(4));
    }

    private ElectricFurnaceMenu(int id, Inventory inv, ElectricFurnaceBlockEntity be, ContainerData data) {
        super(ModMenus.ELECTRIC_FURNACE.get(), id);
        this.be = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());

        this.addSlot(new SlotItemHandler(be.getItems(), 0, 56, 35));
        this.addSlot(new SlotItemHandler(be.getItems(), 1, 116, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(inv, 104, 162);
        addDataSlots(this.data);
    }

    private static ContainerData createData(ElectricFurnaceBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getCookProgress();
                    case 3 -> be.getCookTimeTotal();
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
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, hotbarY));
        }
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getCookProgress() {
        return data.get(2);
    }

    public int getCookTimeTotal() {
        return data.get(3);
    }

    public ElectricFurnaceBlockEntity getBlockEntity() {
        return be;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(access, player, ModBlocks.ELECTRIC_FURNACE.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 1) {
                if (!this.moveItemStackTo(stack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, result);
            } else if (index == 0) {
                if (!this.moveItemStackTo(stack, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.moveItemStackTo(stack, 0, 1, false)) {
                    // moved into input
                } else if (index < 29) {
                    if (!this.moveItemStackTo(stack, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
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
