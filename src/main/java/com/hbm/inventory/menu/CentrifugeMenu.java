package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.CentrifugeBlockEntity;
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

public class CentrifugeMenu extends AbstractContainerMenu {
    private final CentrifugeBlockEntity be;
    private final ContainerData data;

    public CentrifugeMenu(int id, Inventory inv, CentrifugeBlockEntity be) {
        this(id, inv, be, createData(be));
    }

    public CentrifugeMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, CentrifugeBlockEntity.class, CentrifugeBlockEntity::new),
                new SimpleContainerData(3));
    }

    private CentrifugeMenu(int id, Inventory inv, CentrifugeBlockEntity be, ContainerData data) {
        super(ModMenus.MACHINE_CENTRIFUGE.get(), id);
        this.be = be;
        this.data = data;
        this.addSlot(new SlotItemHandler(be.getItems(), CentrifugeBlockEntity.SLOT_INPUT, 36, 50));
        this.addSlot(new SlotItemHandler(be.getItems(), CentrifugeBlockEntity.SLOT_BATTERY, 9, 50) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
        });
        for (int i = 0; i < 4; i++) {
            final int slot = CentrifugeBlockEntity.SLOT_OUTPUT_0 + i;
            this.addSlot(new SlotItemHandler(be.getItems(), slot, 63 + i * 20, 50) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        }
        this.addSlot(new SlotItemHandler(be.getItems(), CentrifugeBlockEntity.SLOT_UPGRADE_0, 149, 22) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CentrifugeBlockEntity.SLOT_UPGRADE_1, 149, 40) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        addPlayerInventory(inv, 104, 162);
        addDataSlots(this.data);
    }

    private static ContainerData createData(CentrifugeBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getEnergy().getEnergyStored();
                    case 1 -> be.getEnergy().getMaxEnergyStored();
                    case 2 -> be.getProgress();
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
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, hotbarY));
        }
    }

    public CentrifugeBlockEntity getBlockEntity() {
        return be;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
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
        if (index <= 7) {
            if (!this.moveItemStackTo(stack, 8, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.isEnergyItem(result)) {
            if (!this.moveItemStackTo(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, 1, false)) {
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
