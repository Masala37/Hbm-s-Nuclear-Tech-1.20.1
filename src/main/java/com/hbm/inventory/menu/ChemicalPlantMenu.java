package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.AssemblyMachineBlockEntity;
import com.hbm.blockentity.machine.ChemicalPlantBlockEntity;
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
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ChemicalPlantMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 16;
    private final ChemicalPlantBlockEntity be;
    private final ContainerData data;

    public ChemicalPlantMenu(int id, Inventory inv, ChemicalPlantBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public ChemicalPlantMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, ChemicalPlantBlockEntity.class,
                ChemicalPlantBlockEntity::new), new SimpleContainerData(DATA_COUNT));
    }

    private ChemicalPlantMenu(int id, Inventory inv, ChemicalPlantBlockEntity be, ContainerData data) {
        super(ModMenus.CHEMICAL_PLANT.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_BATTERY, 152, 81));
        this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_BLUEPRINT, 35, 126) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return AssemblyMachineBlockEntity.isBlueprint(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_UPGRADE_A, 152, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return AssemblyMachineBlockEntity.isUpgradeItem(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_UPGRADE_B, 152, 126) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return AssemblyMachineBlockEntity.isUpgradeItem(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int col = 0; col < 3; col++) {
            this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_INPUT_START + col,
                    8 + col * 18, 99));
        }
        for (int col = 0; col < 3; col++) {
            this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_OUTPUT_START + col,
                    80 + col * 18, 99) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        }
        for (int col = 0; col < 3; col++) {
            this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_FLUID_IN_START + col,
                    8 + col * 18, 54) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return FluidUtil.getFluidHandler(stack).isPresent();
                }
            });
        }
        for (int col = 0; col < 3; col++) {
            this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_FLUID_IN_EMPTY + col,
                    8 + col * 18, 72) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        }
        for (int col = 0; col < 3; col++) {
            this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_FLUID_OUT_START + col,
                    80 + col * 18, 54) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return FluidUtil.getFluidHandler(stack).isPresent();
                }
            });
        }
        for (int col = 0; col < 3; col++) {
            this.addSlot(new SlotItemHandler(be.getItems(), ChemicalPlantBlockEntity.SLOT_FLUID_OUT_EMPTY + col,
                    80 + col * 18, 72) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        }
        addPlayerInventory(inv, 174, 232);
        addDataSlots(this.data);
    }

    private static ContainerData createData(ChemicalPlantBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.displayedMaxPower();
                    case 2 -> (int) Math.round(be.getProgress() * 10_000.0D);
                    case 3 -> be.didProcess() ? 1 : 0;
                    case 4, 5, 6 -> be.getInputTanks()[index - 4].getFluidAmount();
                    case 7, 8, 9 -> be.getOutputTanks()[index - 7].getFluidAmount();
                    case 10, 11, 12 -> fluidId(be.getInputTanks()[index - 10].getFluid());
                    case 13, 14, 15 -> fluidId(be.getOutputTanks()[index - 13].getFluid());
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

    public ChemicalPlantBlockEntity getBlockEntity() {
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
        return fluidFrom(data.get(10 + tank), data.get(4 + tank));
    }

    public FluidStack getOutputFluid(int tank) {
        return fluidFrom(data.get(13 + tank), data.get(7 + tank));
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
        if (index <= 21) {
            if (!this.moveItemStackTo(stack, 22, this.slots.size(), true)) {
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
        } else if (AssemblyMachineBlockEntity.isUpgradeItem(result)) {
            if (!this.moveItemStackTo(stack, 2, 4, false)) {
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
