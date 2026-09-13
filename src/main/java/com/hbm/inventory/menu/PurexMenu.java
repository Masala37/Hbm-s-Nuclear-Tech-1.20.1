package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.AssemblyMachineBlockEntity;
import com.hbm.blockentity.machine.PurexBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.registry.ModMenus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class PurexMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 12;
    private final PurexBlockEntity be;
    private final ContainerData data;

    public PurexMenu(int id, Inventory inv, PurexBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public PurexMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, PurexBlockEntity.class, PurexBlockEntity::new),
                new SimpleContainerData(DATA_COUNT));
    }

    private PurexMenu(int id, Inventory inv, PurexBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_PUREX.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), PurexBlockEntity.SLOT_BATTERY, 152, 81));
        this.addSlot(new SlotItemHandler(be.getItems(), PurexBlockEntity.SLOT_BLUEPRINT, 35, 126) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return AssemblyMachineBlockEntity.isBlueprint(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), PurexBlockEntity.SLOT_UPGRADE_A, 152, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), PurexBlockEntity.SLOT_UPGRADE_B, 152, 126) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int col = 0; col < 3; col++) {
            this.addSlot(new SlotItemHandler(be.getItems(), PurexBlockEntity.SLOT_INPUT_START + col,
                    8 + col * 18, 90));
        }
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new SlotItemHandler(be.getItems(),
                        PurexBlockEntity.SLOT_OUTPUT_START + col + row * 3,
                        80 + col * 18, 36 + row * 18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        addPlayerInventory(inv, 174, 232);
        addDataSlots(this.data);
    }

    private static ContainerData createData(PurexBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.displayedMaxPower();
                    case 2 -> (int) Math.round(be.getProgress() * 10_000.0D);
                    case 3 -> be.didProcess() ? 1 : 0;
                    case 4, 5, 6 -> be.getInputTanks()[index - 4].getFluidAmount();
                    case 7 -> be.getOutputTanks()[0].getFluidAmount();
                    case 8, 9, 10 -> fluidId(be.getInputTanks()[index - 8].getFluid());
                    case 11 -> fluidId(be.getOutputTanks()[0].getFluid());
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

    private static int fluidId(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return -1;
        }
        return BuiltInRegistries.FLUID.getId(stack.getFluid());
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

    public PurexBlockEntity getBlockEntity() {
        return be;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return Math.max(1, data.get(1));
    }

    public double getProgress() {
        return data.get(2) / 10_000.0D;
    }

    public boolean didProcess() {
        return data.get(3) != 0;
    }

    public FluidStack getInputFluid(int tank) {
        return fluidFrom(data.get(8 + tank), data.get(4 + tank));
    }

    public FluidStack getOutputFluid() {
        return fluidFrom(data.get(11), data.get(7));
    }

    private static FluidStack fluidFrom(int id, int amount) {
        if (id < 0 || amount <= 0) {
            return FluidStack.EMPTY;
        }
        var fluid = BuiltInRegistries.FLUID.byId(id);
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
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
        if (index <= 12) {
            if (!this.moveItemStackTo(stack, 13, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.isEnergyItem(result)) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (AssemblyMachineBlockEntity.isBlueprint(result)) {
            if (!this.moveItemStackTo(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 4, 7, false)) {
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
