package com.hbm.inventory.menu;

import com.hbm.blockentity.machine.MissileAssemblyBlockEntity;
import com.hbm.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Legacy {@code ContainerMachineMissileAssembly} slot layout.
 */
public class MissileAssemblyMenu extends AbstractContainerMenu {
    private final MissileAssemblyBlockEntity be;

    public MissileAssemblyMenu(int id, Inventory inv, MissileAssemblyBlockEntity be) {
        super(ModMenus.MISSILE_ASSEMBLY.get(), id);
        this.be = be;

        // Exact legacy coords: chip / warhead / fuselage / fins / thruster / output
        this.addSlot(new SlotItemHandler(be.getItems(), MissileAssemblyBlockEntity.SLOT_CHIP, 8, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), MissileAssemblyBlockEntity.SLOT_WARHEAD, 26, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), MissileAssemblyBlockEntity.SLOT_FUSELAGE, 44, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), MissileAssemblyBlockEntity.SLOT_FINS, 62, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), MissileAssemblyBlockEntity.SLOT_THRUSTER, 80, 36));
        this.addSlot(new SlotItemHandler(be.getItems(), MissileAssemblyBlockEntity.SLOT_OUTPUT, 152, 36) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(inv);
    }

    public MissileAssemblyMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, NukeMenuHelper.readBlockEntity(inv, buf, MissileAssemblyBlockEntity.class,
                MissileAssemblyBlockEntity::new));
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 198));
        }
    }

    public MissileAssemblyBlockEntity getBlockEntity() {
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
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 6) {
                if (!this.moveItemStackTo(stack, 6, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 5, false)) {
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
