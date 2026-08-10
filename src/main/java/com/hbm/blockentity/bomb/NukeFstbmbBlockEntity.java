package com.hbm.blockentity.bomb;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.inventory.menu.NukeFstbmbMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
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
 * Balefire bomb assembly: balefire egg + spark/trixite battery, countdown fuse.
 */
public class NukeFstbmbBlockEntity extends BlockEntity implements AssembledNuke, MenuProvider {
    public static final int SLOT_EGG = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_COUNT = 2;
    public static final int DEFAULT_TIMER_TICKS = 18000; // 15 minutes

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_EGG) {
                return stack.is(ModItems.EGG_BALEFIRE.get());
            }
            if (slot == SLOT_BATTERY) {
                return stack.is(ModItems.BATTERY_SPARK.get()) || stack.is(ModItems.BATTERY_TRIXITE.get());
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);

    private boolean started;
    private int timer = DEFAULT_TIMER_TICKS;

    public NukeFstbmbBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUKE_FSTBMB.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NukeFstbmbBlockEntity be) {
        if (!be.isReady()) {
            if (be.started) {
                be.started = false;
                be.setChanged();
                be.sync();
            }
            return;
        }

        if (be.started) {
            be.timer--;
            if (be.timer % 20 == 0) {
                level.playSound(null, pos, ModSounds.FSTBMB_PING.get(), SoundSource.BLOCKS, 5.0F, 1.0F);
                be.setChanged();
                be.sync();
            }
            if (be.timer <= 0) {
                be.detonate();
            }
        }
    }

    public void startCountdown() {
        if (!isReady() || started || level == null || level.isClientSide) {
            return;
        }
        level.playSound(null, worldPosition, ModSounds.FSTBMB_START.get(), SoundSource.BLOCKS, 5.0F, 1.0F);
        started = true;
        setChanged();
        sync();
    }

    public void setTimerSeconds(int seconds) {
        if (started || level == null || level.isClientSide) {
            return;
        }
        timer = Math.max(1, Math.min(999, seconds)) * 20;
        setChanged();
        sync();
    }

    public void detonate() {
        if (level == null || level.isClientSide || !isReady()) {
            return;
        }
        int radius = resolveBlastRadius(BombConfig.balefireRadius.get());
        clearSlots();
        BlockPos pos = worldPosition;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        level.removeBlock(pos, false);
        level.addFreshEntity(EntityBalefire.statFac(level, x, y, z, radius));
        EntityNukeTorex.statFacBale(level, x, y + 0.5D, z, radius);
    }

    public boolean isStarted() {
        return started;
    }

    public int getTimer() {
        return timer;
    }

    public boolean hasEgg() {
        return items.getStackInSlot(SLOT_EGG).is(ModItems.EGG_BALEFIRE.get());
    }

    /** 0 none, 1 spark, 2 trixite */
    public int getBatteryTier() {
        ItemStack stack = items.getStackInSlot(SLOT_BATTERY);
        if (stack.is(ModItems.BATTERY_SPARK.get())) {
            return 1;
        }
        if (stack.is(ModItems.BATTERY_TRIXITE.get())) {
            return 2;
        }
        return 0;
    }

    public String getMinutes() {
        String mins = Integer.toString(timer / 1200);
        return mins.length() == 1 ? "0" + mins : mins;
    }

    public String getSeconds() {
        String secs = Integer.toString((timer / 20) % 60);
        return secs.length() == 1 ? "0" + secs : secs;
    }

    @Override
    public boolean isReady() {
        return hasEgg() && getBatteryTier() > 0;
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
            started = false;
            sync();
        }
    }

    @Override
    public Component statusMessage() {
        return Component.translatable(isReady() ? "block.hbm.nuke_fstbmb.ready" : "block.hbm.nuke_fstbmb.incomplete");
    }

    @Override
    public int findInsertSlot(Item item) {
        if (item == ModItems.EGG_BALEFIRE.get() && items.getStackInSlot(SLOT_EGG).isEmpty()) {
            return SLOT_EGG;
        }
        if ((item == ModItems.BATTERY_SPARK.get() || item == ModItems.BATTERY_TRIXITE.get())
                && items.getStackInSlot(SLOT_BATTERY).isEmpty()) {
            return SLOT_BATTERY;
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
        return Component.translatable("block.hbm.nuke_fstbmb");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new NukeFstbmbMenu(id, inv, this);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("started", started);
        tag.putInt("timer", timer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        started = tag.getBoolean("started");
        timer = tag.contains("timer") ? tag.getInt("timer") : DEFAULT_TIMER_TICKS;
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
