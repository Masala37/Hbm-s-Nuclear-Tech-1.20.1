package com.hbm.blockentity.machine;

import com.hbm.inventory.menu.BlastFurnaceMenu;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.inventory.recipes.BlastFurnaceRecipes.BlastFurnaceRecipe;
import com.hbm.inventory.recipes.IngredientRef;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 {@code TileEntityMachineBlastFurnace} without fluids (speed stays 0.5 without airblast).
 */
public class BlastFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int FUEL_RATE = 800;
    public static final int MAX_FUEL = 1600 * 24;
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_INPUT_A = 1;
    public static final int SLOT_INPUT_B = 2;
    public static final int SLOT_OUTPUT_A = 3;
    public static final int SLOT_OUTPUT_B = 4;

    private final ItemStackHandler items = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_FUEL) {
                return getBurnTime(stack) > 0;
            }
            return slot == SLOT_INPUT_A || slot == SLOT_INPUT_B;
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
            if (slot >= SLOT_OUTPUT_A) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < SLOT_OUTPUT_A) {
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

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private int fuel;
    private float progress;
    private float speed;
    private boolean progressing;

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLAST_FURNACE.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getFuel() {
        return fuel;
    }

    public float getProgress() {
        return progress;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean isProgressing() {
        return progressing;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.blastFurnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new BlastFurnaceMenu(id, inv, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlastFurnaceBlockEntity be) {
        ItemStack fuelStack = be.items.getStackInSlot(SLOT_FUEL);
        if (!fuelStack.isEmpty()) {
            int burn = getBurnTime(fuelStack);
            if (burn > 0 && burn <= MAX_FUEL - be.fuel) {
                be.fuel += burn;
                fuelStack.shrink(1);
            }
        }

        be.speed = 0.0F;
        BlastFurnaceRecipe recipe = BlastFurnaceRecipes.getRecipe(
                be.items.getStackInSlot(SLOT_INPUT_A),
                be.items.getStackInSlot(SLOT_INPUT_B));
        if (recipe != null && be.fuel >= FUEL_RATE && be.hasQuantities(recipe) && be.canOutput(recipe)) {
            be.speed = 0.5F;
            be.progressing = true;
            be.progress += be.speed / Math.max(1, recipe.duration());
            if (be.progress >= 1.0F) {
                be.process(recipe);
                be.progress = 0.0F;
                be.fuel -= FUEL_RATE;
            }
            if (level.random.nextInt(10) == 0) {
                level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F,
                        0.5F + level.random.nextFloat() * 0.25F);
            }
        } else {
            be.progressing = false;
            be.progress = 0.0F;
        }
        be.onChanged();
    }

    public boolean hasQuantities(BlastFurnaceRecipe recipe) {
        ItemStack a = items.getStackInSlot(SLOT_INPUT_A);
        ItemStack b = items.getStackInSlot(SLOT_INPUT_B);
        List<IngredientRef> inputs = recipe.inputs();
        if (inputs.size() == 1) {
            IngredientRef only = inputs.get(0);
            return only.matches(a, false) || only.matches(b, false);
        }
        if (inputs.size() < 2) {
            return false;
        }
        return (inputs.get(0).matches(a, false) && inputs.get(1).matches(b, false))
                || (inputs.get(0).matches(b, false) && inputs.get(1).matches(a, false));
    }

    public boolean canOutput(BlastFurnaceRecipe recipe) {
        List<IngredientRef> outputs = recipe.presentOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            ItemStack produced = outputs.get(i).resultStack();
            if (produced.isEmpty()) {
                return false;
            }
            ItemStack slot = items.getStackInSlot(SLOT_OUTPUT_A + i);
            if (slot.isEmpty()) {
                continue;
            }
            if (!ItemStack.isSameItemSameTags(slot, produced)
                    || slot.getCount() + produced.getCount() > slot.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    public void process(BlastFurnaceRecipe recipe) {
        List<IngredientRef> outputs = recipe.presentOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            ItemStack produced = outputs.get(i).resultStack();
            ItemStack slot = items.getStackInSlot(SLOT_OUTPUT_A + i);
            if (slot.isEmpty()) {
                items.setStackInSlot(SLOT_OUTPUT_A + i, produced.copy());
            } else {
                slot.grow(produced.getCount());
            }
        }
        List<IngredientRef> inputs = recipe.inputs();
        ItemStack a = items.getStackInSlot(SLOT_INPUT_A);
        ItemStack b = items.getStackInSlot(SLOT_INPUT_B);
        if (inputs.size() == 1) {
            IngredientRef only = inputs.get(0);
            if (only.matches(a, false)) {
                a.shrink(only.count());
            } else if (only.matches(b, false)) {
                b.shrink(only.count());
            }
        } else if (inputs.size() >= 2) {
            if (inputs.get(0).matches(a, false) && inputs.get(1).matches(b, false)) {
                a.shrink(inputs.get(0).count());
                b.shrink(inputs.get(1).count());
            } else if (inputs.get(0).matches(b, false) && inputs.get(1).matches(a, false)) {
                b.shrink(inputs.get(0).count());
                a.shrink(inputs.get(1).count());
            }
        }
    }

    public static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty() || stack.hasCraftingRemainingItem()) {
            return 0;
        }
        if (stack.is(ItemTags.LOGS) || stack.is(ItemTags.PLANKS) || stack.is(ItemTags.WOODEN_SLABS)
                || stack.is(ItemTags.WOODEN_STAIRS) || stack.is(ItemTags.WOODEN_FENCES)
                || stack.is(ItemTags.WOODEN_DOORS) || stack.is(ItemTags.WOODEN_TRAPDOORS)
                || stack.is(ItemTags.WOODEN_BUTTONS) || stack.is(ItemTags.WOODEN_PRESSURE_PLATES)
                || stack.is(ItemTags.SAPLINGS)) {
            return 0;
        }
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 7, 2));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("fuel", fuel);
        tag.putFloat("progress", progress);
        tag.putBoolean("progressing", progressing);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        fuel = tag.getInt("fuel");
        progress = tag.getFloat("progress");
        progressing = tag.getBoolean("progressing");
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
        itemOptional.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
