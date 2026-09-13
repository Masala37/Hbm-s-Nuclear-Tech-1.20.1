package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.SolderingStationBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.registry.ModItems;
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

public class SolderingStationMenu extends AbstractContainerMenu {
    private final SolderingStationBlockEntity be;
    private final ContainerData data;

    public SolderingStationMenu(int id, Inventory inv, SolderingStationBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public SolderingStationMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, SolderingStationBlockEntity.class,
                SolderingStationBlockEntity::new), new SimpleContainerData(6));
    }

    private SolderingStationMenu(int id, Inventory inv, SolderingStationBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_SOLDERING_STATION.get(), id);
        this.be = be;
        this.data = data;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                final int slot = row * 3 + col;
                this.addSlot(new SlotItemHandler(be.getItems(), slot, 17 + col * 18, 18 + row * 18));
            }
        }
        this.addSlot(new SlotItemHandler(be.getItems(), SolderingStationBlockEntity.SLOT_OUTPUT, 107, 27) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), SolderingStationBlockEntity.SLOT_BATTERY, 152, 72) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), SolderingStationBlockEntity.SLOT_FLUID_ID, 17, 63));
        this.addSlot(new SlotItemHandler(be.getItems(), SolderingStationBlockEntity.SLOT_UPGRADE_0, 89, 63) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), SolderingStationBlockEntity.SLOT_UPGRADE_1, 107, 63) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        addPlayerInventory(inv, 122, 180);
        addDataSlots(this.data);
    }

    private static ContainerData createData(SolderingStationBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getProgress();
                    case 3 -> be.getProcessTime();
                    case 4 -> be.getConsumption();
                    case 5 -> be.isCollisionPrevention() ? 1 : 0;
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

    public SolderingStationBlockEntity getBlockEntity() {
        return be;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getProgress() {
        return data.get(2);
    }

    public int getProcessTime() {
        return Math.max(1, data.get(3));
    }

    public int getConsumption() {
        return data.get(4);
    }

    public boolean isCollisionPrevention() {
        return data.get(5) != 0;
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
        if (index <= 10) {
            if (!this.moveItemStackTo(stack, 11, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.isEnergyItem(result)) {
            if (!this.moveItemStackTo(stack, 7, 8, false)) {
                return ItemStack.EMPTY;
            }
        } else if (result.is(ModItems.FLUID_IDENTIFIER.get())) {
            if (!this.moveItemStackTo(stack, 8, 9, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SolderingRecipes.isTopping(result)) {
            if (!this.moveItemStackTo(stack, 0, 3, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SolderingRecipes.isPcb(result)) {
            if (!this.moveItemStackTo(stack, 3, 5, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SolderingRecipes.isSolder(result)) {
            if (!this.moveItemStackTo(stack, 5, 6, false)) {
                return ItemStack.EMPTY;
            }
        } else {
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
