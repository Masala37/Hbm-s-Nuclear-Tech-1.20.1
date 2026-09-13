package com.hbm.blockentity.machine;

import com.hbm.config.MachineConfig;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.RtgMenu;
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
 * 1.7.10 {@code TileEntityMachineRTG}: 15 pellet slots, heat × 5 FE/t, 100 kFE buffer.
 */
public class RtgBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 15;
    public static final int[] SLOT_IO = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    public static final int POWER_MAX = 100_000;
    public static final int HEAT_TO_FE = 5;

    private final ModEnergyStorage energy = new ModEnergyStorage(POWER_MAX, 0, POWER_MAX, this::setChanged);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof ItemRTGPellet;
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
            if (!items.isItemValid(slot, stack)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return items.isItemValid(slot, stack);
        }
    };

    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private int heat;

    public RtgBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_RTG.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public int getHeat() {
        return heat;
    }

    public int heatMax() {
        return MachineConfig.rtgDecay() ? 600 : 200;
    }

    public boolean hasHeat() {
        return RTGUtil.hasHeat(items, SLOT_IO);
    }

    public int getHeatScaled(int pixels) {
        int max = heatMax();
        if (max <= 0) {
            return 0;
        }
        return heat * pixels / max;
    }

    public int getPowerScaled(int pixels) {
        if (POWER_MAX <= 0) {
            return 0;
        }
        return (int) ((long) energy.getEnergyStored() * pixels / POWER_MAX);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.rtg");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new RtgMenu(id, inv, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RtgBlockEntity be) {
        EnergyNetworkHelper.pushToNeighbors(level, pos, be.energy, POWER_MAX);

        int heat = RTGUtil.updateRTGs(be.items, SLOT_IO);
        int max = be.heatMax();
        if (heat > max) {
            heat = max;
        }
        be.heat = heat;

        int stored = be.energy.getEnergyStored() + heat * HEAT_TO_FE;
        if (stored > POWER_MAX) {
            stored = POWER_MAX;
        }
        if (stored != be.energy.getEnergyStored()) {
            be.energy.setEnergy(stored);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.putInt("heat", heat);
        tag.put("Items", items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        heat = tag.getInt("heat");
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
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

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyOptional = LazyOptional.of(() -> energy);
        itemOptional = LazyOptional.of(() -> automation);
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
}
