package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.SilexBlockEntity;
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

public class SilexMenu extends AbstractContainerMenu {
    private final SilexBlockEntity be;
    private final ContainerData data;

    public SilexMenu(int id, Inventory inv, SilexBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public SilexMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, SilexBlockEntity.class, SilexBlockEntity::new),
                new SimpleContainerData(4));
    }

    private SilexMenu(int id, Inventory inv, SilexBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_SILEX.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), SilexBlockEntity.SLOT_INPUT, 80, 12));
        this.addSlot(new SlotItemHandler(be.getItems(), SilexBlockEntity.SLOT_FLUID_ID, 8, 24) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(ModItems.FLUID_IDENTIFIER.get());
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), SilexBlockEntity.SLOT_FLUID_IN, 26, 24));
        this.addSlot(new SlotItemHandler(be.getItems(), SilexBlockEntity.SLOT_FLUID_OUT, 44, 24) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), SilexBlockEntity.SLOT_OUTPUT, 116, 90) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 5, 134, 72) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 6, 152, 72) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 7, 134, 90) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 8, 152, 90) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 9, 134, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), 10, 152, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        addPlayerInventory(inv, 140, 198);
        addDataSlots(this.data);
    }

    private static ContainerData createData(SilexBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getTank().getFluidAmount();
                    case 1 -> be.getCurrentFill();
                    case 2 -> be.getProgress();
                    case 3 -> be.getMode().ordinal();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 4;
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

    public SilexBlockEntity getBlockEntity() {
        return be;
    }

    public int getTankFill() {
        return data.get(0);
    }

    public int getCurrentFill() {
        return data.get(1);
    }

    public int getProgress() {
        return data.get(2);
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
        if (index <= 10) {
            if (!this.moveItemStackTo(stack, 11, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (result.is(ModItems.FLUID_IDENTIFIER.get())) {
            if (!this.moveItemStackTo(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 1, false)
                && !this.moveItemStackTo(stack, 2, 3, false)) {
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
