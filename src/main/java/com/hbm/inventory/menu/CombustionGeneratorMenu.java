package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.CombustionGeneratorBlockEntity;
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

public class CombustionGeneratorMenu extends AbstractContainerMenu {
    private final CombustionGeneratorBlockEntity be;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public CombustionGeneratorMenu(int id, Inventory inv, CombustionGeneratorBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public CombustionGeneratorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, CombustionGeneratorBlockEntity.class, CombustionGeneratorBlockEntity::new),
                new SimpleContainerData(4));
    }

    private CombustionGeneratorMenu(int id, Inventory inv, CombustionGeneratorBlockEntity be, ContainerData data) {
        super(ModMenus.COMBUSTION_GENERATOR.get(), id);
        this.be = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(inv.player.level(), be.getBlockPos());

        this.addSlot(new SlotItemHandler(be.getItems(), 0, 56, 35));
        addPlayerInventory(inv, 104, 162);
        addDataSlots(this.data);
    }

    private static ContainerData createData(CombustionGeneratorBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getBurnTime();
                    case 3 -> be.getBurnTimeTotal();
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

    public int getBurnTime() {
        return data.get(2);
    }

    public int getBurnTimeTotal() {
        return data.get(3);
    }

    public CombustionGeneratorBlockEntity getBlockEntity() {
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
            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 1, false)) {
                if (index < 28) {
                    if (!this.moveItemStackTo(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, 1, 28, false)) {
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
