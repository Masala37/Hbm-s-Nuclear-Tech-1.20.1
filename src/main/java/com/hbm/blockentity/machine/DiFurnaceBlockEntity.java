package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.MachineDiFurnaceBlock;
import com.hbm.inventory.menu.DiFurnaceMenu;
import com.hbm.inventory.recipes.DiFurnaceRecipes;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * 1.7.10 {@code TileEntityDiFurnace}. Pollution / smoke are skipped until that system is ported.
 */
public class DiFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_FUEL = 12_800;
    public static final int PROCESSING_SPEED = 400;
    public static final int EXTENSION_SPEED = 3;
    public static final int SLOT_UPPER = 0;
    public static final int SLOT_LOWER = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_OUTPUT = 3;

    private final ItemStackHandler items = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot != SLOT_OUTPUT;
        }
    };

    private final Map<Direction, LazyOptional<IItemHandler>> sided = new EnumMap<>(Direction.class);
    private final LazyOptional<IItemHandler> unsided = LazyOptional.of(() -> new SidedHandler(null));

    private int progress;
    private int fuel;
    private int sideFuel = Direction.UP.get3DDataValue();
    private int sideUpper = Direction.UP.get3DDataValue();
    private int sideLower = Direction.UP.get3DDataValue();

    public DiFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DI_FURNACE.get(), pos, state);
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

    public int getFuel() {
        return fuel;
    }

    public int getSideUpper() {
        return sideUpper;
    }

    public int getSideLower() {
        return sideLower;
    }

    public int getSideFuel() {
        return sideFuel;
    }

    public void cycleInputSide(int slot) {
        if (slot == SLOT_UPPER) {
            sideUpper = (sideUpper + 1) % 6;
        } else if (slot == SLOT_LOWER) {
            sideLower = (sideLower + 1) % 6;
        } else if (slot == SLOT_FUEL) {
            sideFuel = (sideFuel + 1) % 6;
        }
        onChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.diFurnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new DiFurnaceMenu(id, inv, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DiFurnaceBlockEntity be) {
        int oldProgress = be.progress;
        int oldFuel = be.fuel;

        ItemStack fuelStack = be.items.getStackInSlot(SLOT_FUEL);
        int power = getItemPower(fuelStack);
        if (power > 0 && be.fuel <= MAX_FUEL - power) {
            be.fuel += power;
            ItemStack remainder = fuelStack.getCraftingRemainingItem();
            fuelStack.shrink(1);
            if (fuelStack.isEmpty() && !remainder.isEmpty()) {
                be.items.setStackInSlot(SLOT_FUEL, remainder);
            }
        }

        boolean working = false;
        if (be.canProcess()) {
            be.fuel = Math.max(0, be.fuel - 1);
            boolean extension = level.getBlockState(pos.above()).is(ModBlocks.MACHINE_DIFURNACE_EXTENSION.get());
            be.progress += cookStep(extension);
            working = true;
            if (be.progress >= PROCESSING_SPEED) {
                be.progress -= PROCESSING_SPEED;
                be.processItem();
            }
        } else {
            be.progress = 0;
        }

        if (state.getValue(MachineDiFurnaceBlock.LIT) != working) {
            level.setBlock(pos, state.setValue(MachineDiFurnaceBlock.LIT, working), Block.UPDATE_ALL);
        }
        if (oldProgress != be.progress || oldFuel != be.fuel) {
            be.setChanged();
        }
    }

    public static int cookStep(boolean extension) {
        return extension ? EXTENSION_SPEED : 1;
    }

    public boolean canProcess() {
        if (fuel <= 0) {
            return false;
        }
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

    public static int getItemPower(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) {
            return 0;
        }
        return switch (id.toString()) {
            case "minecraft:coal", "minecraft:charcoal" -> 200;
            case "minecraft:coal_block" -> 2000;
            case "hbm:block_coke" -> 4000;
            case "minecraft:lava_bucket" -> 12800;
            case "minecraft:blaze_rod" -> 1000;
            case "minecraft:blaze_powder" -> 300;
            case "hbm:lignite", "hbm:powder_lignite" -> 150;
            case "hbm:powder_coal", "hbm:briquette" -> 200;
            case "hbm:coke", "hbm:solid_fuel" -> 400;
            default -> 0;
        };
    }

    private boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        if (slot == SLOT_OUTPUT || !items.isItemValid(slot, stack)) {
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
            if (slot == SLOT_FUEL && sideFuel != face) {
                return false;
            }
        }
        return true;
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
        tag.putInt("powerTime", fuel);
        tag.putInt("cookTime", progress);
        tag.putByteArray("modes", new byte[] {(byte) sideFuel, (byte) sideUpper, (byte) sideLower});
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        fuel = tag.getInt("powerTime");
        progress = tag.getInt("cookTime");
        byte[] modes = tag.getByteArray("modes");
        if (modes.length >= 3) {
            sideFuel = modes[0];
            sideUpper = modes[1];
            sideLower = modes[2];
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
            if (slot != SLOT_OUTPUT) {
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
