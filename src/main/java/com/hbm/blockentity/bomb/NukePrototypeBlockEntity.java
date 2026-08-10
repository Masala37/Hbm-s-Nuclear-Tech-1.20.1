package com.hbm.blockentity.bomb;

import com.hbm.inventory.menu.NukePrototypeMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Prototype assembly: 4× SAS3, 4× U quad, 4× Pb quad, 2× Np-237 quad.
 */
public class NukePrototypeBlockEntity extends BlockEntity implements AssembledNuke, MenuProvider {
    public static final int SLOT_COUNT = 14;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(requiredItem(slot));
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);

    public NukePrototypeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUKE_PROTOTYPE.get(), pos, state);
    }

    public static Item requiredItem(int slot) {
        return switch (slot) {
            case 0, 1, 12, 13 -> ModItems.CELL_SAS3.get();
            case 2, 3, 10, 11 -> ModItems.ROD_QUAD_URANIUM.get();
            case 4, 5, 8, 9 -> ModItems.ROD_QUAD_LEAD.get();
            case 6, 7 -> ModItems.ROD_QUAD_NP237.get();
            default -> throw new IllegalArgumentException("Invalid Prototype slot: " + slot);
        };
    }

    @Override
    public boolean isReady() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(requiredItem(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void dropContents() {
        if (level != null && !level.isClientSide) {
            Containers.dropContents(level, worldPosition, new net.minecraft.world.SimpleContainer(copyStacks()));
            clearSlots();
        }
    }

    @Override
    public Component statusMessage() {
        return Component.translatable(isReady() ? "block.hbm.nuke_prototype.ready" : "block.hbm.nuke_prototype.incomplete");
    }

    @Override
    public int findInsertSlot(Item item) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (item == requiredItem(i) && items.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    public ItemStack[] copyStacks() {
        ItemStack[] copy = new ItemStack[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            copy[i] = items.getStackInSlot(i).copy();
        }
        return copy;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.nuke_prototype");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new NukePrototypeMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
    }
}
