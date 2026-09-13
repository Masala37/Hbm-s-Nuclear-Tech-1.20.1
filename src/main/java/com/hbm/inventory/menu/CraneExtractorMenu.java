package com.hbm.inventory.menu;

import com.hbm.blockentity.network.CraneExtractorBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CraneExtractorMenu extends AbstractContainerMenu {
    private final CraneExtractorBlockEntity be;
    private final ContainerData data;

    public CraneExtractorMenu(int id, Inventory inv, CraneExtractorBlockEntity be) {
        this(id, inv, be, data(be));
    }

    public CraneExtractorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, CraneExtractorBlockEntity.class,
                CraneExtractorBlockEntity::new), new SimpleContainerData(2));
    }

    private CraneExtractorMenu(int id, Inventory inv, CraneExtractorBlockEntity be, ContainerData data) {
        super(ModMenus.CRANE_EXTRACTOR.get(), id);
        this.be = be;
        this.data = data;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new FilterSlot(be.getItems(), col + row * 3, 71 + col * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new SlotItemHandler(be.getItems(),
                        CraneExtractorBlockEntity.BUFFER_START + col + row * 3,
                        8 + col * 18, 17 + row * 18));
            }
        }
        this.addSlot(new SlotItemHandler(be.getItems(), CraneExtractorBlockEntity.SLOT_STACK, 152, 23) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return CraneExtractorBlockEntity.isStackUpgrade(stack.getItem());
            }
        });
        this.addSlot(new SlotItemHandler(be.getItems(), CraneExtractorBlockEntity.SLOT_EJECT, 152, 47) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return CraneExtractorBlockEntity.isEjectUpgrade(stack.getItem());
            }
        });
        addPlayerInventory(inv, 103, 161);
        addDataSlots(this.data);
    }

    private static ContainerData data(CraneExtractorBlockEntity be) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    return be.isWhitelist() ? 1 : 0;
                }
                return be.isMaxEject() ? 1 : 0;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public CraneExtractorBlockEntity getBlockEntity() {
        return be;
    }

    public boolean isWhitelist() {
        return data.get(0) != 0;
    }

    public boolean isMaxEject() {
        return data.get(1) != 0;
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId >= 0 && slotId < 9 && clickType != ClickType.QUICK_CRAFT) {
            Slot slot = getSlot(slotId);
            ItemStack held = getCarried();
            if (button == 1 && slot.hasItem()) {
                slot.set(ItemStack.EMPTY);
            } else if (!held.isEmpty()) {
                ItemStack ghost = held.copy();
                ghost.setCount(1);
                slot.set(ghost);
            } else {
                slot.set(ItemStack.EMPTY);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void addPlayerInventory(Inventory inv, int invY, int hotbarY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 26 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 26 + col * 18, hotbarY));
        }
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
        if (index < 9) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();
        int machineEnd = CraneExtractorBlockEntity.SLOTS;
        if (index < machineEnd) {
            if (!this.moveItemStackTo(stack, machineEnd, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (CraneExtractorBlockEntity.isStackUpgrade(result.getItem())) {
            if (!this.moveItemStackTo(stack, CraneExtractorBlockEntity.SLOT_STACK,
                    CraneExtractorBlockEntity.SLOT_STACK + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (CraneExtractorBlockEntity.isEjectUpgrade(result.getItem())) {
            if (!this.moveItemStackTo(stack, CraneExtractorBlockEntity.SLOT_EJECT,
                    CraneExtractorBlockEntity.SLOT_EJECT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, CraneExtractorBlockEntity.BUFFER_START,
                CraneExtractorBlockEntity.BUFFER_END, false)) {
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
