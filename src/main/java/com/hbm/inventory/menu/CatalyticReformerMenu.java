package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.CatalyticReformerBlockEntity;
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

public class CatalyticReformerMenu extends AbstractContainerMenu {
    private final CatalyticReformerBlockEntity be;
    private final ContainerData data;

    public CatalyticReformerMenu(int id, Inventory inv, CatalyticReformerBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public CatalyticReformerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, CatalyticReformerBlockEntity.class,
                CatalyticReformerBlockEntity::new), new SimpleContainerData(10));
    }

    private CatalyticReformerMenu(int id, Inventory inv, CatalyticReformerBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_CATALYTIC_REFORMER.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_BATTERY, 17, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_IN, 35, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_IN_EMPTY, 35, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_OUT1_IN, 107, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_OUT1_EMPTY, 107, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_OUT2_IN, 125, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_OUT2_EMPTY, 125, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_OUT3_IN, 143, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_OUT3_EMPTY, 143, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_FLUID_ID, 17, 108));
        this.addSlot(new SlotItemHandler(be.getItems(), CatalyticReformerBlockEntity.SLOT_CATALYST, 71, 36));
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

    private static ContainerData createData(CatalyticReformerBlockEntity be) {
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
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 10;
            }
        };
    }

    public CatalyticReformerBlockEntity getBlockEntity() {
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
            if (index <= 10) {
                if (!this.moveItemStackTo(stack, 11, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemEnergyHelper.isEnergyItem(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(ModItems.FLUID_IDENTIFIER.get())) {
                if (!this.moveItemStackTo(stack, 9, 10, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (CatalyticReformerBlockEntity.isCatalyticConverter(stack)) {
                if (!this.moveItemStackTo(stack, 10, 11, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 1, 2, false)
                    && !this.moveItemStackTo(stack, 3, 4, false)
                    && !this.moveItemStackTo(stack, 5, 6, false)
                    && !this.moveItemStackTo(stack, 7, 8, false)) {
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
