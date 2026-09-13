package com.hbm.blockentity.network;

import api.hbm.conveyor.IConveyorItem;
import com.hbm.conveyor.CraneInventories;
import com.hbm.inventory.menu.CraneInserterMenu;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
 * 1.7.10 {@code TileEntityCraneInserter}: 21-slot buffer that pushes into the output neighbor.
 */
public class CraneInserterBlockEntity extends CraneBaseBlockEntity {
    public static final int SLOTS = 21;

    private final ItemStackHandler items = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);
    private boolean destroyer = true;

    public CraneInserterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRANE_INSERTER.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isDestroyer() {
        return destroyer;
    }

    public void toggleDestroyer() {
        destroyer = !destroyer;
        onChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CraneInserterBlockEntity crane) {
        if (level.isClientSide || level.hasNeighborSignal(pos)) {
            return;
        }
        Direction output = crane.getOutputSide();
        BlockEntity neighbor = level.getBlockEntity(pos.relative(output));
        IItemHandler dest = CraneInventories.handler(neighbor, output);
        if (dest == null) {
            return;
        }
        if (tryInsert(crane, dest, false)) {
            return;
        }
        tryInsert(crane, dest, true);
    }

    private static boolean tryInsert(CraneInserterBlockEntity crane, IItemHandler dest, boolean singles) {
        for (int i = 0; i < crane.items.getSlots(); i++) {
            ItemStack stack = crane.items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack toSend = stack.copy();
            if (singles) {
                toSend.setCount(1);
            }
            ItemStack leftover = CraneInventories.insert(dest, toSend);
            int moved = toSend.getCount() - leftover.getCount();
            if (moved > 0) {
                crane.items.extractItem(i, moved, false);
                return true;
            }
        }
        return false;
    }

    public void acceptFromBelt(IConveyorItem moving) {
        if (level == null || moving == null || moving.getItemStack().isEmpty()) {
            return;
        }
        ItemStack toAdd = moving.getItemStack().copy();
        if (!level.hasNeighborSignal(worldPosition)) {
            Direction output = getOutputSide();
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(output));
            IItemHandler dest = CraneInventories.handler(neighbor, output);
            toAdd = CraneInventories.insert(dest, toAdd);
        }
        if (!toAdd.isEmpty()) {
            toAdd = CraneInventories.insert(items, toAdd);
        }
        if (!toAdd.isEmpty() && !destroyer) {
            level.addFreshEntity(new ItemEntity(level,
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, toAdd));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.craneInserter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new CraneInserterMenu(id, inv, this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemOptional = LazyOptional.of(() -> items);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("destroyer", destroyer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        destroyer = tag.getBoolean("destroyer");
    }
}
