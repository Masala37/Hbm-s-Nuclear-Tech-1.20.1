package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.CrystallizerBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
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

public class CrystallizerMenu extends AbstractContainerMenu {
    private final CrystallizerBlockEntity be;
    private final ContainerData data;

    public CrystallizerMenu(int id, Inventory inv, CrystallizerBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public CrystallizerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, CrystallizerBlockEntity.class,
                CrystallizerBlockEntity::new), new SimpleContainerData(4));
    }

    private CrystallizerMenu(int id, Inventory inv, CrystallizerBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_CRYSTALLIZER.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), CrystallizerBlockEntity.SLOT_INPUT, 62, 45));
        this.addSlot(new SlotItemHandler(be.getItems(), CrystallizerBlockEntity.SLOT_BATTERY, 152, 72) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CrystallizerBlockEntity.SLOT_OUTPUT, 113, 45) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CrystallizerBlockEntity.SLOT_FLUID_IN, 17, 18));
        this.addSlot(new SlotItemHandler(be.getItems(), CrystallizerBlockEntity.SLOT_FLUID_EMPTY, 17, 54) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CrystallizerBlockEntity.SLOT_UPGRADE_0, 80, 18) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CrystallizerBlockEntity.SLOT_UPGRADE_1, 98, 18) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CrystallizerBlockEntity.SLOT_FLUID_ID, 35, 72));
        addPlayerInventory(inv, 122, 180);
        addDataSlots(this.data);
    }

    private static ContainerData createData(CrystallizerBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getProgress();
                    case 3 -> be.getDuration();
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

    public CrystallizerBlockEntity getBlockEntity() {
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
        if (index <= 7) {
            if (!this.moveItemStackTo(stack, 8, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.isEnergyItem(result)) {
            if (!this.moveItemStackTo(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (result.is(ModItems.FLUID_IDENTIFIER.get())) {
            if (!this.moveItemStackTo(stack, 7, 8, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 1, false)) {
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
