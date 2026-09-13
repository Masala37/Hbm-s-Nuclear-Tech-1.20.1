package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.OilWellBlockEntity;
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

public class OilWellMenu extends AbstractContainerMenu {
    private final OilWellBlockEntity be;
    private final ContainerData data;

    public OilWellMenu(int id, Inventory inv, OilWellBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public OilWellMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, OilWellBlockEntity.class, OilWellBlockEntity::new),
                new SimpleContainerData(7));
    }

    private OilWellMenu(int id, Inventory inv, OilWellBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_WELL.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), OilWellBlockEntity.SLOT_BATTERY, 8, 53));
        this.addSlot(new SlotItemHandler(be.getItems(), OilWellBlockEntity.SLOT_OIL_IN, 80, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), OilWellBlockEntity.SLOT_OIL_OUT, 80, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), OilWellBlockEntity.SLOT_GAS_IN, 125, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), OilWellBlockEntity.SLOT_GAS_OUT, 125, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 5, 152, 17) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 6, 152, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 7, 152, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
        addDataSlots(this.data);
    }

    private static ContainerData createData(OilWellBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getOil().getFluidAmount();
                    case 3 -> be.getOil().getCapacity();
                    case 4 -> be.getGas().getFluidAmount();
                    case 5 -> be.getGas().getCapacity();
                    case 6 -> be.getIndicator();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 7;
            }
        };
    }

    public OilWellBlockEntity getBlockEntity() {
        return be;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getOilAmount() {
        return data.get(2);
    }

    public int getOilCap() {
        return data.get(3);
    }

    public int getGasAmount() {
        return data.get(4);
    }

    public int getGasCap() {
        return data.get(5);
    }

    public int getIndicator() {
        return data.get(6);
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
            if (index <= 7) {
                if (!this.moveItemStackTo(stack, 8, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemEnergyHelper.isEnergyItem(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 1, 2, false)
                    && !this.moveItemStackTo(stack, 3, 4, false)) {
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
