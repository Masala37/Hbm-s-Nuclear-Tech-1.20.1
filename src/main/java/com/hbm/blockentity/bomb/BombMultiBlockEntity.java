package com.hbm.blockentity.bomb;

import com.hbm.inventory.menu.BombMultiMenu;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
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
 * Multi-Purpose Bomb: 4× TNT required; slots 2 &amp; 5 are optional warheads.
 * Types: 0 empty, 1 gunpowder, 2 TNT, 3 cluster, 4 fire, 5 poison, 6 gas.
 */
public class BombMultiBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 6;
    public static final float EXPLOSION_BASE = 8.0F;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);

    public BombMultiBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOMB_MULTI.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isLoaded() {
        return isTnt(0) && isTnt(1) && isTnt(3) && isTnt(4);
    }

    private boolean isTnt(int slot) {
        ItemStack stack = items.getStackInSlot(slot);
        return !stack.isEmpty() && stack.is(Blocks.TNT.asItem());
    }

    /** Payload type for schematic slot 2. */
    public int return2type() {
        return payloadType(2);
    }

    /** Payload type for schematic slot 5. */
    public int return5type() {
        return payloadType(5);
    }

    public int payloadType(int slot) {
        ItemStack stack = items.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        if (item == Items.GUNPOWDER) {
            return 1;
        }
        if (item == Blocks.TNT.asItem()) {
            return 2;
        }
        if (stack.is(ModItems.PELLET_CLUSTER.get())) {
            return 3;
        }
        if (stack.is(ModItems.POWDER_FIRE.get()) || stack.is(ModItems.POWDER_RED_PHOSPHORUS.get())) {
            return 4;
        }
        if (stack.is(ModItems.POWDER_POISON.get())) {
            return 5;
        }
        if (stack.is(ModItems.PELLET_GAS.get())) {
            return 6;
        }
        return 0;
    }

    public void clearSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public ItemStack[] copyStacks() {
        ItemStack[] copy = new ItemStack[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            copy[i] = items.getStackInSlot(i).copy();
        }
        return copy;
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            Containers.dropContents(level, worldPosition, new net.minecraft.world.SimpleContainer(copyStacks()));
            clearSlots();
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.hbm.bomb_multi");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new BombMultiMenu(id, inv, this);
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
