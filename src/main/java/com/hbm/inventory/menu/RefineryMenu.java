package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.RefineryBlockEntity;
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

public class RefineryMenu extends AbstractContainerMenu {
    private final RefineryBlockEntity be;
    private final ContainerData data;

    public RefineryMenu(int id, Inventory inv, RefineryBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public RefineryMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, RefineryBlockEntity.class, RefineryBlockEntity::new),
                new SimpleContainerData(12));
    }

    private RefineryMenu(int id, Inventory inv, RefineryBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_REFINERY.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_BATTERY, 186, 72));
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_IN, 8, 99));
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_IN_EMPTY, 8, 119) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_HEAVY_IN, 86, 99));
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_HEAVY_OUT, 86, 119) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_NAPHTHA_IN, 106, 99));
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_NAPHTHA_OUT, 106, 119) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_LIGHT_IN, 126, 99));
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_LIGHT_OUT, 126, 119) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_PETRO_IN, 146, 99));
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_PETRO_OUT, 146, 119) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_SULFUR, 58, 119) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), RefineryBlockEntity.SLOT_FLUID_ID, 186, 106));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 150 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 208));
        }
        addDataSlots(this.data);
    }

    private static ContainerData createData(RefineryBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getInputTank().getFluidAmount();
                    case 3 -> be.getInputTank().getCapacity();
                    case 4 -> be.getHeavy().getFluidAmount();
                    case 5 -> be.getHeavy().getCapacity();
                    case 6 -> be.getNaphtha().getFluidAmount();
                    case 7 -> be.getNaphtha().getCapacity();
                    case 8 -> be.getLight().getFluidAmount();
                    case 9 -> be.getLight().getCapacity();
                    case 10 -> be.getPetroleum().getFluidAmount();
                    case 11 -> be.getPetroleum().getCapacity();
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

    public RefineryBlockEntity getBlockEntity() {
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
            if (index <= 12) {
                if (!this.moveItemStackTo(stack, 13, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemEnergyHelper.isEnergyItem(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(ModItems.FLUID_IDENTIFIER.get())) {
                if (!this.moveItemStackTo(stack, 12, 13, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 1, 2, false)
                    && !this.moveItemStackTo(stack, 3, 4, false)
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
