package com.hbm.blockentity.machine;

import com.hbm.energy.ConnectionPriority;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.IEnergyPriority;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.MachineBatteryMenu;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachineBattery}: 1M FE buffer, charge/discharge slots,
 * redstone input/buffer/output/none modes, and charge priority.
 */
public class MachineBatteryBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 1_000_000;
    public static final int MAX_RECEIVE = 5_000;
    public static final int MAX_EXTRACT = 1_667;
    public static final int SLOT_DISCHARGE = 0;
    public static final int SLOT_CHARGE = 1;
    public static final int MODE_INPUT = 0;
    public static final int MODE_BUFFER = 1;
    public static final int MODE_OUTPUT = 2;
    public static final int MODE_NONE = 3;

    private final ModEnergyStorage energy = new ModEnergyStorage(CAPACITY, MAX_RECEIVE, MAX_EXTRACT, this::onEnergyChanged);
    private final GatedEnergy gated = new GatedEnergy();
    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            onEnergyChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ItemEnergyHelper.isEnergyItem(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private final IItemHandler automation = new IItemHandler() {
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
            if (!ItemEnergyHelper.isEnergyItem(stack)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack held = items.getStackInSlot(slot);
            if (slot == SLOT_DISCHARGE && !ItemEnergyHelper.isDrained(held)) {
                return ItemStack.EMPTY;
            }
            if (slot == SLOT_CHARGE && !ItemEnergyHelper.isFilled(held)) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ItemEnergyHelper.isEnergyItem(stack);
        }
    };
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> gated);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private short redLow = MODE_INPUT;
    private short redHigh = MODE_OUTPUT;
    private ConnectionPriority priority = ConnectionPriority.LOW;
    private final long[] log = new long[20];
    private long delta;

    public MachineBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_BATTERY.get(), pos, state);
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public short getRedLow() {
        return redLow;
    }

    public short getRedHigh() {
        return redHigh;
    }

    public ConnectionPriority getPriority() {
        return priority;
    }

    public long getDelta() {
        return delta;
    }

    public int relevantMode() {
        boolean powered = level != null && level.hasNeighborSignal(worldPosition);
        return powered ? redHigh : redLow;
    }

    public int getComparatorPower() {
        int stored = energy.getEnergyStored();
        if (stored <= 0) {
            return 0;
        }
        double frac = (double) stored / (double) energy.getMaxEnergyStored() * 15.0D;
        return Mth.clamp((int) frac + 1, 0, 15);
    }

    public void cycleButton(int button) {
        if (button == 0) {
            redLow = cycleMode(redLow);
        } else if (button == 1) {
            redHigh = cycleMode(redHigh);
        } else if (button == 2) {
            priority = cyclePriority(priority);
        } else {
            return;
        }
        onEnergyChanged();
    }

    public static short cycleMode(short mode) {
        return (short) ((mode + 1) % 4);
    }

    public static ConnectionPriority cyclePriority(ConnectionPriority current) {
        return switch (current) {
            case LOW -> ConnectionPriority.NORMAL;
            case NORMAL -> ConnectionPriority.HIGH;
            default -> ConnectionPriority.LOW;
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.machine_battery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new MachineBatteryMenu(id, inv, this);
    }

    private void onEnergyChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBatteryBlockEntity be) {
        int prev = be.energy.getEnergyStored();
        ItemEnergyHelper.chargeItemFromBuffer(be.items.getStackInSlot(SLOT_CHARGE), be.energy);
        int mode = be.relevantMode();
        if (mode == MODE_OUTPUT || mode == MODE_BUFFER) {
            EnergyNetworkHelper.pushToNeighbors(level, pos, be.gated, MAX_EXTRACT);
        }
        ItemEnergyHelper.dischargeItemIntoBuffer(be.items.getStackInSlot(SLOT_DISCHARGE), be.energy);
        long avg = (be.energy.getEnergyStored() + (long) prev) / 2L;
        be.delta = avg - be.log[0];
        System.arraycopy(be.log, 1, be.log, 0, be.log.length - 1);
        be.log[be.log.length - 1] = avg;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.putShort("redLow", redLow);
        tag.putShort("redHigh", redHigh);
        tag.putByte("priority", (byte) priority.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        redLow = tag.getShort("redLow");
        redHigh = tag.contains("redHigh") ? tag.getShort("redHigh") : MODE_OUTPUT;
        int ordinal = tag.contains("priority") ? tag.getByte("priority") : ConnectionPriority.LOW.ordinal();
        if (ordinal < 0 || ordinal >= ConnectionPriority.values().length) {
            ordinal = ConnectionPriority.LOW.ordinal();
        }
        priority = ConnectionPriority.values()[ordinal];
        if (priority == ConnectionPriority.LOWEST || priority == ConnectionPriority.HIGHEST) {
            priority = ConnectionPriority.LOW;
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
        energyOptional.invalidate();
        itemOptional.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    private final class GatedEnergy implements IEnergyStorage, IEnergyPriority {
        private boolean allowsReceive() {
            int mode = relevantMode();
            return mode == MODE_INPUT || mode == MODE_BUFFER;
        }

        private boolean allowsExtract() {
            int mode = relevantMode();
            return mode == MODE_OUTPUT || mode == MODE_BUFFER;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!allowsReceive()) {
                return 0;
            }
            return energy.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!allowsExtract()) {
                return 0;
            }
            return energy.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return energy.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energy.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return allowsExtract() && energy.canExtract();
        }

        @Override
        public boolean canReceive() {
            return allowsReceive() && energy.canReceive();
        }

        @Override
        public ConnectionPriority energyPriority() {
            return priority;
        }
    }
}
