package com.hbm.blockentity.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.conveyor.CraneInventories;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.inventory.menu.CraneExtractorMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityCraneExtractor}: pull from output-side inventory onto the input-side belt.
 */
public class CraneExtractorBlockEntity extends CraneBaseBlockEntity {
    public static final int SLOTS = 20;
    public static final int FILTER_START = 0;
    public static final int FILTER_END = 9;
    public static final int BUFFER_START = 9;
    public static final int BUFFER_END = 18;
    public static final int SLOT_STACK = 18;
    public static final int SLOT_EJECT = 19;

    private final ItemStackHandler items = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_STACK) {
                return isStackUpgrade(stack.getItem());
            }
            if (slot == SLOT_EJECT) {
                return isEjectUpgrade(stack.getItem());
            }
            return slot >= BUFFER_START && slot < BUFFER_END;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot < FILTER_END) {
                return 1;
            }
            return super.getSlotLimit(slot);
        }
    };
    private final IItemHandler buffer = new RangedWrapper(items, BUFFER_START, BUFFER_END);
    private LazyOptional<IItemHandler> bufferOptional = LazyOptional.of(() -> buffer);
    private boolean whitelist;
    private boolean maxEject;

    public CraneExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRANE_EXTRACTOR.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public boolean isMaxEject() {
        return maxEject;
    }

    public void toggleWhitelist() {
        whitelist = !whitelist;
        onChanged();
    }

    public void toggleMaxEject() {
        maxEject = !maxEject;
        onChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CraneExtractorBlockEntity crane) {
        if (level.isClientSide || level.hasNeighborSignal(pos)) {
            return;
        }
        int delay = delayFor(crane.items.getStackInSlot(SLOT_EJECT));
        if (level.getGameTime() % delay != 0) {
            return;
        }
        int amount = amountFor(crane.items.getStackInSlot(SLOT_STACK));
        Direction pullDir = crane.getOutputSide();
        Direction beltDir = crane.getInputSide();
        BlockEntity source = level.getBlockEntity(pos.relative(pullDir));
        BlockPos beltPos = pos.relative(beltDir);
        BlockState beltState = level.getBlockState(beltPos);
        IConveyorBelt belt = beltState.getBlock() instanceof IConveyorBelt b ? b : null;
        IItemHandler inv = CraneInventories.handler(source, pullDir);
        boolean sent = false;
        if (inv != null) {
            sent = crane.extractFrom(inv, amount, belt, beltDir, beltPos);
        }
        if (!sent && belt != null) {
            crane.flushBuffer(amount, belt, beltDir, beltPos);
        }
    }

    private boolean extractFrom(IItemHandler inv, int amount, @Nullable IConveyorBelt belt, Direction beltDir,
                                BlockPos beltPos) {
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            int maxTarget = Math.min(amount, stack.getMaxStackSize());
            if (maxEject && stack.getCount() < maxTarget) {
                continue;
            }
            boolean match = matchesFilter(stack);
            if ((whitelist && match) || (!whitelist && !match)) {
                int toSend = Math.min(amount, stack.getCount());
                if (belt != null) {
                    ItemStack taken = inv.extractItem(i, toSend, false);
                    if (!taken.isEmpty()) {
                        sendItem(taken, belt, beltDir, beltPos);
                        return true;
                    }
                } else {
                    ItemStack sim = inv.extractItem(i, toSend, true);
                    ItemStack leftover = insertBuffer(sim.copy());
                    int moved = sim.getCount() - leftover.getCount();
                    if (moved > 0) {
                        inv.extractItem(i, moved, false);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void flushBuffer(int amount, IConveyorBelt belt, Direction beltDir, BlockPos beltPos) {
        for (int i = BUFFER_START; i < BUFFER_END; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            int maxTarget = Math.min(amount, stack.getMaxStackSize());
            if (maxEject && stack.getCount() < maxTarget) {
                continue;
            }
            int toSend = Math.min(amount, stack.getCount());
            ItemStack taken = items.extractItem(i, toSend, false);
            sendItem(taken, belt, beltDir, beltPos);
            return;
        }
    }

    private ItemStack insertBuffer(ItemStack stack) {
        return CraneInventories.insert(buffer, stack);
    }

    private void sendItem(ItemStack stack, IConveyorBelt belt, Direction beltDir, BlockPos beltPos) {
        if (level == null || stack.isEmpty()) {
            return;
        }
        EntityMovingItem moving = new EntityMovingItem(level);
        Vec3 pos = new Vec3(
                worldPosition.getX() + 0.5D + beltDir.getStepX() * 0.55D,
                worldPosition.getY() + 0.5D + beltDir.getStepY() * 0.55D,
                worldPosition.getZ() + 0.5D + beltDir.getStepZ() * 0.55D);
        Vec3 snap = belt.getClosestSnappingPosition(level, beltPos, pos);
        moving.setPos(snap.x, snap.y, snap.z);
        moving.setItemStack(stack);
        level.addFreshEntity(moving);
        if (level.getBlockState(beltPos).getBlock() instanceof IEnterableBlock enterable
                && enterable.canItemEnter(level, beltPos, beltDir.getOpposite(), moving)) {
            enterable.onItemEnter(level, beltPos, beltDir.getOpposite(), moving);
            moving.discard();
        }
    }

    public boolean matchesFilter(ItemStack stack) {
        for (int i = FILTER_START; i < FILTER_END; i++) {
            if (CraneInventories.matchesFilter(stack, items.getStackInSlot(i))) {
                return true;
            }
        }
        return false;
    }

    private static int delayFor(ItemStack upgrade) {
        Item item = upgrade.getItem();
        if (item == ModItems.UPGRADE_EJECTOR_1.get()) {
            return 10;
        }
        if (item == ModItems.UPGRADE_EJECTOR_2.get()) {
            return 5;
        }
        if (item == ModItems.UPGRADE_EJECTOR_3.get()) {
            return 2;
        }
        return 20;
    }

    private static int amountFor(ItemStack upgrade) {
        Item item = upgrade.getItem();
        if (item == ModItems.UPGRADE_STACK_1.get()) {
            return 4;
        }
        if (item == ModItems.UPGRADE_STACK_2.get()) {
            return 16;
        }
        if (item == ModItems.UPGRADE_STACK_3.get()) {
            return 64;
        }
        return 1;
    }

    public static boolean isStackUpgrade(Item item) {
        return item == ModItems.UPGRADE_STACK_1.get()
                || item == ModItems.UPGRADE_STACK_2.get()
                || item == ModItems.UPGRADE_STACK_3.get();
    }

    public static boolean isEjectUpgrade(Item item) {
        return item == ModItems.UPGRADE_EJECTOR_1.get()
                || item == ModItems.UPGRADE_EJECTOR_2.get()
                || item == ModItems.UPGRADE_EJECTOR_3.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.craneExtractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new CraneExtractorMenu(id, inv, this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        bufferOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        bufferOptional = LazyOptional.of(() -> buffer);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return bufferOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("isWhitelist", whitelist);
        tag.putBoolean("maxEject", maxEject);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        whitelist = tag.getBoolean("isWhitelist");
        maxEject = tag.getBoolean("maxEject");
    }
}
