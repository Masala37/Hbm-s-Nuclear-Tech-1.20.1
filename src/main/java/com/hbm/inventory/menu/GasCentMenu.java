package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.GasCentBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.inventory.recipes.GasCentrifugeRecipes;
import com.hbm.inventory.recipes.GasCentrifugeRecipes.PseudoFluidType;
import com.hbm.registry.ModItems;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class GasCentMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 8;
    private final GasCentBlockEntity be;
    private final ContainerData data;

    public GasCentMenu(int id, Inventory inv, GasCentBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public GasCentMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, GasCentBlockEntity.class, GasCentBlockEntity::new),
                new SimpleContainerData(DATA_COUNT));
    }

    private GasCentMenu(int id, Inventory inv, GasCentBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_GASCENT.get(), id);
        this.be = be;
        this.data = data;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                this.addSlot(new SlotItemHandler(be.getItems(), col + row * 2, 71 + col * 18, 53 + row * 18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        this.addSlot(new SlotItemHandler(be.getItems(), GasCentBlockEntity.SLOT_BATTERY, 182, 71) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), GasCentBlockEntity.SLOT_FLUID_ID, 91, 15) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(ModItems.FLUID_IDENTIFIER.get());
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), GasCentBlockEntity.SLOT_UPGRADE, 69, 15) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addPlayerInventory(inv, 122, 180);
        addDataSlots(this.data);
    }

    private static ContainerData createData(GasCentBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getProgress();
                    case 3 -> be.getInputFill();
                    case 4 -> be.getOutputFill();
                    case 5 -> typeId(be.getInputType());
                    case 6 -> typeId(be.getOutputType());
                    case 7 -> fluidId(be.getTankType());
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

    private static int typeId(PseudoFluidType type) {
        int i = 0;
        for (String name : GasCentrifugeRecipes.types().keySet()) {
            if (name.equals(type.name())) {
                return i;
            }
            i++;
        }
        return 0;
    }

    private static PseudoFluidType typeFromId(int id) {
        int i = 0;
        for (PseudoFluidType type : GasCentrifugeRecipes.types().values()) {
            if (i == id) {
                return type;
            }
            i++;
        }
        return GasCentrifugeRecipes.byName(GasCentrifugeRecipes.NONE);
    }

    private static int fluidId(Fluid fluid) {
        if (fluid == null) {
            return -1;
        }
        return BuiltInRegistries.FLUID.getId(fluid);
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

    public GasCentBlockEntity getBlockEntity() {
        return be;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return Math.max(1, data.get(1));
    }

    public int getProgress() {
        return data.get(2);
    }

    public int getInputFill() {
        return data.get(3);
    }

    public int getOutputFill() {
        return data.get(4);
    }

    public PseudoFluidType getInputType() {
        return typeFromId(data.get(5));
    }

    public PseudoFluidType getOutputType() {
        return typeFromId(data.get(6));
    }

    public FluidStack getTankFluid() {
        int id = data.get(7);
        if (id < 0) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.byId(id);
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, 1);
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
        if (index <= 6) {
            if (!this.moveItemStackTo(stack, 7, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.isEnergyItem(result)) {
            if (!this.moveItemStackTo(stack, 4, 5, false)) {
                return ItemStack.EMPTY;
            }
        } else if (result.is(ModItems.FLUID_IDENTIFIER.get())) {
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
