package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.VacuumDistillBlockEntity;
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

public class VacuumDistillMenu extends AbstractContainerMenu {
    private final VacuumDistillBlockEntity be;
    private final ContainerData data;

    public VacuumDistillMenu(int id, Inventory inv, VacuumDistillBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public VacuumDistillMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, VacuumDistillBlockEntity.class,
                VacuumDistillBlockEntity::new), new SimpleContainerData(12));
    }

    private VacuumDistillMenu(int id, Inventory inv, VacuumDistillBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_VACUUM_DISTILL.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_BATTERY, 26, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_IN, 44, 90) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_IN_EMPTY, 44, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_OUT1_IN, 80, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_OUT1_EMPTY, 80, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_OUT2_IN, 98, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_OUT2_EMPTY, 98, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_OUT3_IN, 116, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_OUT3_EMPTY, 116, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_OUT4_IN, 134, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_OUT4_EMPTY, 134, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), VacuumDistillBlockEntity.SLOT_FLUID_ID, 26, 108));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 156 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 214));
        }
        addDataSlots(this.data);
    }

    private static ContainerData createData(VacuumDistillBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getOil().getFluidAmount();
                    case 3 -> be.getOil().getCapacity();
                    case 4 -> be.getOutput1().getFluidAmount();
                    case 5 -> be.getOutput1().getCapacity();
                    case 6 -> be.getOutput2().getFluidAmount();
                    case 7 -> be.getOutput2().getCapacity();
                    case 8 -> be.getOutput3().getFluidAmount();
                    case 9 -> be.getOutput3().getCapacity();
                    case 10 -> be.getOutput4().getFluidAmount();
                    case 11 -> be.getOutput4().getCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 12;
            }
        };
    }

    public VacuumDistillBlockEntity getBlockEntity() {
        return be;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return MenuValidity.boundToBlock(player, be);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index <= 11) {
                if (!this.moveItemStackTo(stack, 12, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemEnergyHelper.isEnergyItem(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(ModItems.FLUID_IDENTIFIER.get())) {
                if (!this.moveItemStackTo(stack, 11, 12, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 3, 4, false)
                    && !this.moveItemStackTo(stack, 5, 6, false)
                    && !this.moveItemStackTo(stack, 7, 8, false)
                    && !this.moveItemStackTo(stack, 9, 10, false)) {
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
}
