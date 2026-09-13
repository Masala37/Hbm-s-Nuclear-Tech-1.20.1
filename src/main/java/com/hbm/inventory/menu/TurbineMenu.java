package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.TurbineBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
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

public class TurbineMenu extends AbstractContainerMenu {
    private final TurbineBlockEntity be;
    private final ContainerData data;

    public TurbineMenu(int id, Inventory inv, TurbineBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public TurbineMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, TurbineBlockEntity.class, TurbineBlockEntity::new),
                new SimpleContainerData(6));
    }

    private TurbineMenu(int id, Inventory inv, TurbineBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_TURBINE.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), TurbineBlockEntity.SLOT_ID_IN, 8, 17) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), TurbineBlockEntity.SLOT_ID_OUT, 8, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), TurbineBlockEntity.SLOT_IN, 44, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), TurbineBlockEntity.SLOT_IN_EMPTY, 44, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), TurbineBlockEntity.SLOT_BATTERY, 98, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), TurbineBlockEntity.SLOT_OUT, 152, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), TurbineBlockEntity.SLOT_OUT_EMPTY, 152, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        addPlayerInventory(inv, 84, 142);
        addDataSlots(this.data);
    }

    private static ContainerData createData(TurbineBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getInput().getFluidAmount();
                    case 3 -> be.getInput().getCapacity();
                    case 4 -> be.getOutput().getFluidAmount();
                    case 5 -> be.getOutput().getCapacity();
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

    public TurbineBlockEntity getBlockEntity() {
        return be;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getInputAmount() {
        return data.get(2);
    }

    public int getInputCap() {
        return data.get(3);
    }

    public int getOutputAmount() {
        return data.get(4);
    }

    public int getOutputCap() {
        return data.get(5);
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
            if (index <= 6) {
                if (!this.moveItemStackTo(stack, 7, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemEnergyHelper.isEnergyItem(stack)) {
                if (!this.moveItemStackTo(stack, 4, 5, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 2, 3, false)
                    && !this.moveItemStackTo(stack, 5, 6, false)) {
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
