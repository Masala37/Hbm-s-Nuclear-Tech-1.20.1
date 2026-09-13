package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.WoodBurnerBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.inventory.recipes.WoodBurnerBurnTime;
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
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class WoodBurnerMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 8;
    private final WoodBurnerBlockEntity be;
    private final ContainerData data;

    public WoodBurnerMenu(int id, Inventory inv, WoodBurnerBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public WoodBurnerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, WoodBurnerBlockEntity.class, WoodBurnerBlockEntity::new),
                new SimpleContainerData(DATA_COUNT));
    }

    private WoodBurnerMenu(int id, Inventory inv, WoodBurnerBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_WOOD_BURNER.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), WoodBurnerBlockEntity.SLOT_FUEL, 26, 18));
        this.addSlot(new SlotItemHandler(be.getItems(), WoodBurnerBlockEntity.SLOT_ASH, 26, 54) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), WoodBurnerBlockEntity.SLOT_FLUID_ID, 98, 54));
        this.addSlot(new SlotItemHandler(be.getItems(), WoodBurnerBlockEntity.SLOT_FLUID_IN, 98, 18));
        this.addSlot(new SlotItemHandler(be.getItems(), WoodBurnerBlockEntity.SLOT_FLUID_OUT, 98, 36) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), WoodBurnerBlockEntity.SLOT_BATTERY, 143, 54));
        addPlayerInventory(inv, 104, 162);
        addDataSlots(this.data);
    }

    private static ContainerData createData(WoodBurnerBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getBurnTime();
                    case 3 -> Math.max(be.getMaxBurnTime(), 1);
                    case 4 -> be.getTank().getFluidAmount();
                    case 5 -> be.getTank().getCapacity();
                    case 6 -> be.isOn() ? 1 : 0;
                    case 7 -> be.isLiquidBurn() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
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

    public WoodBurnerBlockEntity getBlockEntity() {
        return be;
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

    public int getMaxBurnTime() {
        return data.get(3);
    }

    public int getFluidAmount() {
        return data.get(4);
    }

    public int getFluidCapacity() {
        return data.get(5);
    }

    public boolean isOn() {
        return data.get(6) != 0;
    }

    public boolean isLiquidBurn() {
        return data.get(7) != 0;
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
            if (index <= 5) {
                if (!this.moveItemStackTo(stack, 6, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemEnergyHelper.isEnergyItem(stack)) {
                if (!this.moveItemStackTo(stack, 5, 6, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(ModItems.FLUID_IDENTIFIER.get())) {
                if (!this.moveItemStackTo(stack, 2, 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (WoodBurnerBurnTime.burnTime(stack) > 0) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (FluidUtil.getFluidHandler(stack).isPresent()) {
                if (!this.moveItemStackTo(stack, 3, 4, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 3, 4, false)) {
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
