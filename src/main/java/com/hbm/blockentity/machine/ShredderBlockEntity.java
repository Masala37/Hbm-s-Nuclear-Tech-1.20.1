package com.hbm.blockentity.machine;

import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.ShredderMenu;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.machine.ItemBlades;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachineShredder}.
 */
public class ShredderBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_POWER = 10_000;
    public static final int PROCESSING_SPEED = 60;
    public static final int SLOT_BLADE_LEFT = 27;
    public static final int SLOT_BLADE_RIGHT = 28;
    public static final int SLOT_BATTERY = 29;

    private final ModEnergyStorage energy = new ModEnergyStorage(MAX_POWER, 200, 0, this::setChanged);
    private final ItemStackHandler items = new ItemStackHandler(30) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot < 9) {
                return !(stack.getItem() instanceof ItemBlades) && !ShredderRecipes.getShredderResult(stack).isEmpty();
            }
            if (slot == SLOT_BATTERY) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
            if (slot == SLOT_BLADE_LEFT || slot == SLOT_BLADE_RIGHT) {
                return stack.getItem() instanceof ItemBlades;
            }
            return false;
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
            if (slot >= 9 && slot != SLOT_BLADE_LEFT && slot != SLOT_BLADE_RIGHT) {
                return stack;
            }
            if (!items.isItemValid(slot, stack)) {
                return stack;
            }
            if (slot >= 0 && slot < 9 && !items.getStackInSlot(slot).isEmpty()) {
                int size = items.getStackInSlot(slot).getCount();
                for (int k = 0; k < 9; k++) {
                    ItemStack other = items.getStackInSlot(k);
                    if (other.isEmpty()) {
                        return stack;
                    }
                    if (ItemStack.isSameItemSameTags(other, stack) && other.getCount() < size) {
                        return stack;
                    }
                }
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= 9 && slot <= 26) {
                return items.extractItem(slot, amount, simulate);
            }
            if (slot == SLOT_BLADE_LEFT || slot == SLOT_BLADE_RIGHT) {
                ItemStack stack = items.getStackInSlot(slot);
                if (stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage()) {
                    return items.extractItem(slot, amount, simulate);
                }
            }
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

    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private int progress;
    private int soundCycle;

    public ShredderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHREDDER.get(), pos, state);
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

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.machineShredder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new ShredderMenu(id, inv, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ShredderBlockEntity be) {
        if (be.progress == 0) {
            be.soundCycle = 0;
        }
        ItemEnergyHelper.chargeFromItem(be.items.getStackInSlot(SLOT_BATTERY), be.energy, 200);
        if (be.energy.getEnergyStored() > 0 && be.canProcess()) {
            be.progress++;
            be.energy.consume(5);
            if (be.progress >= PROCESSING_SPEED) {
                for (int i = SLOT_BLADE_LEFT; i <= SLOT_BLADE_RIGHT; i++) {
                    ItemStack blade = be.items.getStackInSlot(i);
                    if (blade.getMaxDamage() > 0) {
                        blade.setDamageValue(Math.min(blade.getMaxDamage(), blade.getDamageValue() + 1));
                        be.items.setStackInSlot(i, blade);
                    }
                }
                be.progress = 0;
                be.processItem();
            }
            if (be.soundCycle == 0) {
                level.playSound(null, pos, SoundEvents.MINECART_RIDING, SoundSource.BLOCKS, 1.0F, 0.75F);
            }
            be.soundCycle++;
            if (be.soundCycle >= 50) {
                be.soundCycle = 0;
            }
        } else if (be.progress != 0) {
            be.progress = 0;
            be.setChanged();
        }
    }

    public boolean canProcess() {
        int left = getGearLeft();
        int right = getGearRight();
        if (left <= 0 || left >= 3 || right <= 0 || right >= 3) {
            return false;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && hasSpace(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpace(ItemStack stack) {
        ItemStack result = ShredderRecipes.getShredderResult(stack);
        if (result.isEmpty()) {
            return false;
        }
        for (int i = 9; i < 27; i++) {
            ItemStack slot = items.getStackInSlot(i);
            if (slot.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameTags(slot, result) && slot.getCount() + result.getCount() <= slot.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    public void processItem() {
        for (int inp = 0; inp < 9; inp++) {
            ItemStack input = items.getStackInSlot(inp);
            if (input.isEmpty() || !hasSpace(input)) {
                continue;
            }
            ItemStack outp = ShredderRecipes.getShredderResult(input);
            boolean merged = false;
            for (int out = 9; out < 27; out++) {
                ItemStack slot = items.getStackInSlot(out);
                if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, outp)
                        && slot.getCount() + outp.getCount() <= slot.getMaxStackSize()) {
                    slot.grow(outp.getCount());
                    input.shrink(1);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                for (int out = 9; out < 27; out++) {
                    if (items.getStackInSlot(out).isEmpty()) {
                        items.setStackInSlot(out, outp.copy());
                        input.shrink(1);
                        break;
                    }
                }
            }
        }
    }

    public int getGearLeft() {
        return gearState(items.getStackInSlot(SLOT_BLADE_LEFT));
    }

    public int getGearRight() {
        return gearState(items.getStackInSlot(SLOT_BLADE_RIGHT));
    }

    private static int gearState(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlades)) {
            return 0;
        }
        if (stack.getMaxDamage() <= 0) {
            return 1;
        }
        if (stack.getDamageValue() < stack.getMaxDamage() / 2) {
            return 1;
        }
        if (stack.getDamageValue() != stack.getMaxDamage()) {
            return 2;
        }
        return 3;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        progress = tag.getInt("progress");
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
}
