package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.ElectricFurnaceBlock;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.ElectricFurnaceMenu;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
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

import java.util.Optional;

/**
 * FE-powered furnace. Slot 0 = input, slot 1 = output.
 */
public class ElectricFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 50_000;
    public static final int MAX_RECEIVE = 200;
    public static final int ENERGY_PER_TICK = 20;
    public static final int DEFAULT_COOK_TIME = 100;

    private final ModEnergyStorage energy = new ModEnergyStorage(CAPACITY, MAX_RECEIVE, 0, this::onChanged);
    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0;
        }
    };

    /** Automation-facing handler: insert input only, extract output only. */
    private final IItemHandler automationItems = new IItemHandler() {
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
            if (slot != 0) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 1) {
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

    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automationItems);

    private int cookProgress;
    private int cookTimeTotal = DEFAULT_COOK_TIME;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FURNACE.get(), pos, state);
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getCookProgress() {
        return cookProgress;
    }

    public int getCookTimeTotal() {
        return cookTimeTotal;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.electric_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new ElectricFurnaceMenu(id, inv, this);
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity be) {
        ItemStack input = be.items.getStackInSlot(0);
        ItemStack output = be.items.getStackInSlot(1);
        Optional<SmeltingRecipe> recipe = findRecipe(level, input);

        boolean canCook = false;
        ItemStack result = ItemStack.EMPTY;
        if (recipe.isPresent() && !input.isEmpty()) {
            result = recipe.get().getResultItem(level.registryAccess()).copy();
            canCook = canOutput(output, result);
            be.cookTimeTotal = Math.max(1, recipe.get().getCookingTime());
        }

        boolean lit = false;
        if (canCook && be.energy.getEnergyStored() >= ENERGY_PER_TICK) {
            be.energy.consume(ENERGY_PER_TICK);
            be.cookProgress++;
            lit = true;

            if (be.cookProgress >= be.cookTimeTotal) {
                be.cookProgress = 0;
                input.shrink(1);
                be.items.setStackInSlot(0, input);
                if (output.isEmpty()) {
                    be.items.setStackInSlot(1, result);
                } else {
                    output.grow(result.getCount());
                    be.items.setStackInSlot(1, output);
                }
                be.onChanged();
            }
        } else if (!canCook) {
            be.cookProgress = 0;
        } else {
            // Not enough power: slowly lose progress
            if (be.cookProgress > 0) {
                be.cookProgress = Math.max(0, be.cookProgress - 2);
            }
        }

        if (state.getValue(ElectricFurnaceBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(ElectricFurnaceBlock.LIT, lit), Block.UPDATE_CLIENTS);
        }
    }

    private static Optional<SmeltingRecipe> findRecipe(Level level, ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        Container container = new SimpleContainer(input);
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level);
    }

    private static boolean canOutput(ItemStack output, ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(output, result)) {
            return false;
        }
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("CookProgress", cookProgress);
        tag.putInt("CookTimeTotal", cookTimeTotal);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        cookProgress = tag.getInt("CookProgress");
        cookTimeTotal = Math.max(1, tag.getInt("CookTimeTotal"));
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
