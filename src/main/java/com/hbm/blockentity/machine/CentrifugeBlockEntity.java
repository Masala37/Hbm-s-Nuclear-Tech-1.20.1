package com.hbm.blockentity.machine;

import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.CentrifugeMenu;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachineCentrifuge}. Speed/power/overdrive upgrades skipped.
 */
public class CentrifugeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_OUTPUT_0 = 2;
    public static final int SLOT_OUTPUT_3 = 5;
    public static final int SLOT_UPGRADE_0 = 6;
    public static final int SLOT_UPGRADE_1 = 7;
    public static final int SLOT_COUNT = 8;

    public static final int MAX_POWER = 100_000;
    public static final int PROCESSING_SPEED = 200;
    public static final int BASE_CONSUMPTION = 200;

    private final ModEnergyStorage energy = new ModEnergyStorage(MAX_POWER, MAX_POWER, 0, this::onChanged);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_INPUT -> true;
                case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
                default -> false;
            };
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
            if (slot != SLOT_INPUT) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < SLOT_OUTPUT_0 || slot > SLOT_OUTPUT_3) {
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
            return items.isItemValid(slot, stack);
        }
    };

    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private int progress;
    private int soundCycle;

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_CENTRIFUGE.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isProcessing() {
        return progress > 0;
    }

    public int getProgressScaled(int pixels) {
        return (progress * pixels) / PROCESSING_SPEED;
    }

    public int getPowerScaled(int pixels) {
        return (int) ((long) energy.getEnergyStored() * pixels / Math.max(1, energy.getMaxEnergyStored()));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new CentrifugeMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity be) {
        if (!level.isClientSide) {
            be.serverTick(level, pos);
        }
    }

    private void serverTick(Level level, BlockPos pos) {
        ItemEnergyHelper.chargeFromItem(items.getStackInSlot(SLOT_BATTERY), energy, MAX_POWER);
        if (energy.getEnergyStored() > 0 && isProcessing()) {
            energy.consume(BASE_CONSUMPTION);
        }
        boolean canProcess = canProcess();
        if (energy.getEnergyStored() > 0 && canProcess) {
            progress++;
            if (progress >= PROCESSING_SPEED) {
                progress = 0;
                processItem();
            }
            if (soundCycle == 0) {
                level.playSound(null, pos, ModSounds.require("block.centrifuge_operate"),
                        SoundSource.BLOCKS, 1.0F, 0.75F);
            }
            soundCycle++;
            if (soundCycle >= 50) {
                soundCycle = 0;
            }
        } else {
            progress = 0;
            soundCycle = 0;
        }
        onChanged();
    }

    public boolean canProcess() {
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        ItemStack[] out = CentrifugeRecipes.getOutput(input);
        if (out == null) {
            return false;
        }
        for (int i = 0; i < Math.min(4, out.length); i++) {
            ItemStack produced = out[i];
            if (produced == null || produced.isEmpty()) {
                continue;
            }
            ItemStack slot = items.getStackInSlot(SLOT_OUTPUT_0 + i);
            if (slot.isEmpty()) {
                continue;
            }
            if (ItemStack.isSameItemSameTags(slot, produced)
                    && slot.getCount() + produced.getCount() <= produced.getMaxStackSize()) {
                continue;
            }
            return false;
        }
        return true;
    }

    private void processItem() {
        ItemStack[] out = CentrifugeRecipes.getOutput(items.getStackInSlot(SLOT_INPUT));
        if (out == null) {
            return;
        }
        for (int i = 0; i < Math.min(4, out.length); i++) {
            ItemStack produced = out[i];
            if (produced == null || produced.isEmpty()) {
                continue;
            }
            ItemStack slot = items.getStackInSlot(SLOT_OUTPUT_0 + i);
            if (slot.isEmpty()) {
                items.setStackInSlot(SLOT_OUTPUT_0 + i, produced.copy());
            } else {
                slot.grow(produced.getCount());
            }
        }
        items.getStackInSlot(SLOT_INPUT).shrink(1);
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 4, 1));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        progress = tag.getInt("Progress");
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
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        itemOptional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
