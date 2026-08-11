package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.entity.missile.MissileLaunchRegistry;
import com.hbm.items.tool.DesignatorItem;
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

/**
 * Legacy ContainerLaunchPadLarge slot layout.
 */
public class LaunchPadMenu extends AbstractContainerMenu {
    private final LaunchPadBlockEntity be;
    private final ContainerData data;

    public LaunchPadMenu(int id, Inventory inv, LaunchPadBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public LaunchPadMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, LaunchPadBlockEntity.class, LaunchPadBlockEntity::new),
                new SimpleContainerData(10));
    }

    private LaunchPadMenu(int id, Inventory inv, LaunchPadBlockEntity be, ContainerData data) {
        super(ModMenus.LAUNCH_PAD.get(), id);
        this.be = be;
        this.data = data;

        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadBlockEntity.SLOT_MISSILE, 26, 36) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return MissileLaunchRegistry.isLaunchable(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadBlockEntity.SLOT_DESIGNATOR, 26, 72) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof DesignatorItem;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadBlockEntity.SLOT_BATTERY, 107, 90) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadBlockEntity.SLOT_FUEL_IN, 125, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadBlockEntity.SLOT_FUEL_OUT, 125, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadBlockEntity.SLOT_OX_IN, 143, 90));
        this.addSlot(new SlotItemHandler(be.getItems(), LaunchPadBlockEntity.SLOT_OX_OUT, 143, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(inv);
        addDataSlots(this.data);
    }

    private static ContainerData createData(LaunchPadBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.hasTarget() ? 1 : 0;
                    case 3 -> be.getTarget().getX();
                    case 4 -> be.getTarget().getY();
                    case 5 -> be.getTarget().getZ();
                    case 6 -> be.getFuelTank().getFluidAmount();
                    case 7 -> be.getOxidizerTank().getFluidAmount();
                    case 8 -> be.getState();
                    case 9 -> be.getDelay();
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

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 154 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 212));
        }
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public boolean hasTarget() {
        return data.get(2) != 0;
    }

    public int getTargetX() {
        return data.get(3);
    }

    public int getTargetY() {
        return data.get(4);
    }

    public int getTargetZ() {
        return data.get(5);
    }

    public int getFuelAmount() {
        return data.get(6);
    }

    public int getOxidizerAmount() {
        return data.get(7);
    }

    public int getPadState() {
        return data.get(8);
    }

    public int getDelay() {
        return data.get(9);
    }

    public LaunchPadBlockEntity getBlockEntity() {
        return be;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return MenuValidity.closeEnough(player, be);
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
        if (index < 7) {
            if (!this.moveItemStackTo(stack, 7, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved;
            if (MissileLaunchRegistry.isLaunchable(stack)) {
                moved = this.moveItemStackTo(stack, 0, 1, false);
            } else if (stack.getItem() instanceof DesignatorItem) {
                moved = this.moveItemStackTo(stack, 1, 2, false);
            } else if (ItemEnergyHelper.isEnergyItem(stack)) {
                moved = this.moveItemStackTo(stack, 2, 3, false);
            } else if (FluidUtil.getFluidHandler(stack).isPresent()) {
                moved = this.moveItemStackTo(stack, 3, 4, false)
                        || this.moveItemStackTo(stack, 5, 6, false);
            } else {
                moved = this.moveItemStackTo(stack, 7, this.slots.size(), false);
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }
}
