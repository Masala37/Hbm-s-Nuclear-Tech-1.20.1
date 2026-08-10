package com.hbm.blockentity.bomb;

import com.hbm.inventory.menu.NukeN2Menu;
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
 * N² Mine assembly: twelve {@code n2_charge} cells.
 */
public class NukeN2BlockEntity extends BlockEntity implements AssembledNuke, MenuProvider {
    public static final int SLOT_COUNT = 12;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(ModItems.N2_CHARGE.get());
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);

    public NukeN2BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUKE_N2.get(), pos, state);
    }

    public int chargeCount() {
        int count = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (items.getStackInSlot(i).is(ModItems.N2_CHARGE.get())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isReady() {
        return chargeCount() == SLOT_COUNT;
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
        return Component.translatable(isReady() ? "block.hbm.nuke_n2.ready" : "block.hbm.nuke_n2.incomplete");
    }

    @Override
    public int findInsertSlot(Item item) {
        if (item != ModItems.N2_CHARGE.get()) {
            return -1;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (items.getStackInSlot(i).isEmpty()) {
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
        return Component.translatable("block.hbm.nuke_n2");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new NukeN2Menu(id, inv, this);
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
