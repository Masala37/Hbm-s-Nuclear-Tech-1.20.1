package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.FluidBarrelBlockEntity;
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

public class FluidBarrelMenu extends AbstractContainerMenu {
    private final FluidBarrelBlockEntity be;
    private final ContainerData data;

    public FluidBarrelMenu(int id, Inventory inv, FluidBarrelBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public FluidBarrelMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, FluidBarrelBlockEntity.class, FluidBarrelBlockEntity::new),
                new SimpleContainerData(3));
    }

    private FluidBarrelMenu(int id, Inventory inv, FluidBarrelBlockEntity be, ContainerData data) {
        super(ModMenus.FLUID_BARREL.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), FluidBarrelBlockEntity.SLOT_ID_IN, 8, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), FluidBarrelBlockEntity.SLOT_ID_OUT, 8, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), FluidBarrelBlockEntity.SLOT_FILL_IN, 35, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), FluidBarrelBlockEntity.SLOT_FILL_OUT, 35, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), FluidBarrelBlockEntity.SLOT_EMPTY_IN, 125, 17));
        this.addSlot(new SlotItemHandler(be.getItems(), FluidBarrelBlockEntity.SLOT_EMPTY_OUT, 125, 53) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        addPlayerInventory(inv, 84, 142);
        addDataSlots(this.data);
    }

    private static ContainerData createData(FluidBarrelBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getTank().getFluidAmount();
                    case 1 -> be.getTank().getCapacity();
                    case 2 -> be.getMode();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    private void addPlayerInventory(Inventory inv, int invY, int hotbarY) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, hotbarY));
        }
    }

    public int getFluidAmount() {
        return data.get(0);
    }

    public int getFluidCapacity() {
        return data.get(1);
    }

    public int getMode() {
        return data.get(2);
    }

    public FluidBarrelBlockEntity getBlockEntity() {
        return be;
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
            } else if (stack.is(ModItems.FLUID_IDENTIFIER.get())) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (FluidUtil.getFluidHandler(stack).isPresent()) {
                if (!this.moveItemStackTo(stack, 2, 3, false)
                        && !this.moveItemStackTo(stack, 4, 5, false)) {
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

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }
}
