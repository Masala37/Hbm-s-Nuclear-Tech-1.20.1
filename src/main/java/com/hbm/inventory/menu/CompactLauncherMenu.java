package com.hbm.inventory.menu;

import api.hbm.item.IDesignatorItem;
import com.hbm.blockentity.machine.CompactLauncherBlockEntity;
import com.hbm.blockentity.machine.CustomLauncherBlockEntity;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CompactLauncherMenu extends AbstractContainerMenu {
    protected final CustomLauncherBlockEntity be;
    protected final ContainerData data;

    public CompactLauncherMenu(int id, Inventory inv, CustomLauncherBlockEntity be) {
        this(ModMenus.COMPACT_LAUNCHER.get(), id, inv, be, createData(be));
    }

    public CompactLauncherMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, CompactLauncherBlockEntity.class,
                CompactLauncherBlockEntity::new));
    }

    protected CompactLauncherMenu(MenuType<?> type, int id, Inventory inv, CustomLauncherBlockEntity be,
                                   ContainerData data) {
        super(type, id);
        this.be = be;
        this.data = data;

        this.addSlot(new SlotItemHandler(be.getItems(), CustomLauncherBlockEntity.SLOT_MISSILE, 26, 36) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof ItemCustomMissile;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CustomLauncherBlockEntity.SLOT_DESIGNATOR, 26, 72) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof IDesignatorItem;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CustomLauncherBlockEntity.SLOT_FUEL_IN, 116, 72));
        this.addSlot(new SlotItemHandler(be.getItems(), CustomLauncherBlockEntity.SLOT_OX_IN, 134, 72));
        this.addSlot(new SlotItemHandler(be.getItems(), CustomLauncherBlockEntity.SLOT_SOLID, 152, 90) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return CustomLauncherBlockEntity.isRocketFuel(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CustomLauncherBlockEntity.SLOT_BATTERY, 116, 108) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CustomLauncherBlockEntity.SLOT_FUEL_OUT, 116, 90) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CustomLauncherBlockEntity.SLOT_OX_OUT, 134, 90) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 198));
        }
        addDataSlots(this.data);
    }

    protected static ContainerData createData(CustomLauncherBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getSolid();
                    case 3 -> be.maxSolid;
                    case 4 -> be.liquidState();
                    case 5 -> be.oxidizerState();
                    case 6 -> be.solidState();
                    case 7 -> be.isMissileValid() ? 1 : 0;
                    case 8 -> be.hasDesignator() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 9;
            }
        };
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getSolid() {
        return data.get(2);
    }

    public int getMaxSolid() {
        return data.get(3);
    }

    public int liquidState() {
        return data.get(4);
    }

    public int oxidizerState() {
        return data.get(5);
    }

    public int solidState() {
        return data.get(6);
    }

    public boolean missileValid() {
        return data.get(7) != 0;
    }

    public boolean hasDesignator() {
        return data.get(8) != 0;
    }

    public CustomLauncherBlockEntity getBlockEntity() {
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
        if (index < 8) {
            if (!this.moveItemStackTo(stack, 8, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved;
            if (stack.getItem() instanceof ItemCustomMissile) {
                moved = this.moveItemStackTo(stack, 0, 1, false);
            } else if (stack.getItem() instanceof IDesignatorItem) {
                moved = this.moveItemStackTo(stack, 1, 2, false);
            } else if (CustomLauncherBlockEntity.isRocketFuel(stack)) {
                moved = this.moveItemStackTo(stack, 4, 5, false);
            } else if (ItemEnergyHelper.isEnergyItem(stack)) {
                moved = this.moveItemStackTo(stack, 5, 6, false);
            } else if (FluidUtil.getFluidHandler(stack).isPresent()) {
                moved = this.moveItemStackTo(stack, 2, 3, false)
                        || this.moveItemStackTo(stack, 3, 4, false);
            } else {
                moved = this.moveItemStackTo(stack, 8, this.slots.size(), false);
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
