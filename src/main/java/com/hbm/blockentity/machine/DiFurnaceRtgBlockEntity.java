package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.MachineDiFurnaceRtgBlock;
import com.hbm.inventory.menu.DiFurnaceRtgMenu;
import com.hbm.inventory.recipes.DiFurnaceRecipes;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.registry.ModBlockEntities;
import com.hbm.util.RTGUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * 1.7.10 {@code TileEntityDiFurnaceRTG}: same alloy recipes as the coal blast furnace, RTG heat ≥ 15.
 */
public class DiFurnaceRtgBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TIME_REQUIRED = 1200;
    public static final int MIN_HEAT = 15;
    public static final int SLOT_UPPER = 0;
    public static final int SLOT_LOWER = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int[] RTG_SLOTS = {3, 4, 5, 6, 7, 8};

    private final ItemStackHandler items = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return DiFurnaceRtgBlockEntity.this.isItemValidForSlot(slot, stack);
        }
    };

    private final Map<Direction, LazyOptional<IItemHandler>> sided = new EnumMap<>(Direction.class);
    private final LazyOptional<IItemHandler> unsided = LazyOptional.of(() -> new SidedHandler(null));

    private int progress;
    private int processSpeed;
    private int sideUpper = Direction.UP.get3DDataValue();
    private int sideLower = Direction.UP.get3DDataValue();

    public DiFurnaceRtgBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DI_FURNACE_RTG.get(), pos, state);
        for (Direction dir : Direction.values()) {
            sided.put(dir, LazyOptional.of(() -> new SidedHandler(dir)));
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessSpeed() {
        return processSpeed;
    }

    public int getSideUpper() {
        return sideUpper;
    }

    public int getSideLower() {
        return sideLower;
    }

    public void cycleInputSide(int slot) {
        if (slot == SLOT_UPPER) {
            sideUpper = (sideUpper + 1) % 6;
        } else if (slot == SLOT_LOWER) {
            sideLower = (sideLower + 1) % 6;
        }
        onChanged();
    }

    public boolean isProcessing() {
        return progress > 0;
    }

    public boolean hasPower() {
        return processSpeed >= MIN_HEAT;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.diFurnaceRTG");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new DiFurnaceRtgMenu(id, inv, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DiFurnaceRtgBlockEntity be) {
        int oldProgress = be.progress;
        be.processSpeed = RTGUtil.updateRTGs(be.items, RTG_SLOTS);
        boolean lit = be.isProcessing() || (be.canProcess() && be.hasPower());
        if (be.canProcess() && be.hasPower()) {
            be.progress += be.processSpeed;
            if (be.progress >= TIME_REQUIRED) {
                be.processItem();
                be.progress = 0;
            }
        } else {
            be.progress = 0;
        }
        if (state.getValue(MachineDiFurnaceRtgBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MachineDiFurnaceRtgBlock.LIT, lit), Block.UPDATE_ALL);
        }
        if (oldProgress != be.progress) {
            be.setChanged();
        }
    }

    public boolean canProcess() {
        ItemStack a = items.getStackInSlot(SLOT_UPPER);
        ItemStack b = items.getStackInSlot(SLOT_LOWER);
        ItemStack output = DiFurnaceRecipes.getOutput(a, b);
        if (output.isEmpty()) {
            return false;
        }
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    private void processItem() {
        ItemStack output = DiFurnaceRecipes.getOutput(items.getStackInSlot(SLOT_UPPER), items.getStackInSlot(SLOT_LOWER));
        if (output.isEmpty()) {
            return;
        }
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, output.copy());
        } else {
            existing.grow(output.getCount());
        }
        items.getStackInSlot(SLOT_UPPER).shrink(1);
        items.getStackInSlot(SLOT_LOWER).shrink(1);
    }

    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == SLOT_OUTPUT) {
            return false;
        }
        if (stack.getItem() instanceof ItemRTGPellet) {
            return slot > SLOT_OUTPUT;
        }
        return slot < SLOT_OUTPUT;
    }

    private boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        if (!isItemValidForSlot(slot, stack)) {
            return false;
        }
        if (side != null) {
            int face = side.get3DDataValue();
            if (slot == SLOT_UPPER && sideUpper != face) {
                return false;
            }
            if (slot == SLOT_LOWER && sideLower != face) {
                return false;
            }
        }
        return true;
    }

    private boolean canExtract(int slot, ItemStack stack) {
        if (slot > SLOT_OUTPUT) {
            return !(stack.getItem() instanceof ItemRTGPellet);
        }
        return slot == SLOT_OUTPUT;
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putShort("progress", (short) progress);
        tag.putShort("speed", (short) processSpeed);
        tag.putByteArray("modes", new byte[] {(byte) sideUpper, (byte) sideLower});
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        progress = tag.getShort("progress");
        processSpeed = tag.getShort("speed");
        byte[] modes = tag.getByteArray("modes");
        if (modes.length >= 2) {
            sideUpper = modes[0];
            sideLower = modes[1];
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        unsided.invalidate();
        sided.values().forEach(LazyOptional::invalidate);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return unsided.cast();
            }
            return sided.get(side).cast();
        }
        return super.getCapability(cap, side);
    }

    private final class SidedHandler implements IItemHandler {
        private final @Nullable Direction side;

        private SidedHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return items.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!canInsert(slot, stack, side)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack existing = items.getStackInSlot(slot);
            if (!canExtract(slot, existing)) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return canInsert(slot, stack, side);
        }
    }
}
