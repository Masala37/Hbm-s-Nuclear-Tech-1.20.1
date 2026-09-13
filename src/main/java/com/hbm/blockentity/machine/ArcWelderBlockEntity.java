package com.hbm.blockentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.blocks.machine.MachineArcWelderBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.menu.ArcWelderMenu;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.inventory.recipes.ArcWelderRecipes.ArcWelderRecipe;
import com.hbm.inventory.recipes.IngredientRef;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachineArcWelder}. Speed/power/overdrive upgrades skipped.
 * Fluid-identifier type swap skipped (identifier is a bulk item without NBT).
 */
public class ArcWelderBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT_0 = 0;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_FLUID_ID = 5;
    public static final int SLOT_UPGRADE_0 = 6;
    public static final int SLOT_UPGRADE_1 = 7;
    public static final int SLOT_COUNT = 8;

    public static final int DEFAULT_MAX_POWER = 2_000;
    public static final int DEFAULT_CONSUMPTION = 100;
    public static final int TANK_CAPACITY = 24_000;

    private final ModEnergyStorage energy = new ModEnergyStorage(DEFAULT_MAX_POWER, DEFAULT_MAX_POWER, 0, this::onChanged);
    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }
    };
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ArcWelderBlockEntity.this.mayPlace(slot, stack);
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
            if (slot < SLOT_INPUT_0 || slot > 2 || !items.isItemValid(slot, stack)) {
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
            return items.isItemValid(slot, stack);
        }
    };

    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);
    private LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> tank);

    private int progress;
    private int processTime = 1;
    private int consumption = DEFAULT_CONSUMPTION;
    private ItemStack display = ItemStack.EMPTY;

    public ArcWelderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_ARC_WELDER.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public FluidTank getTank() {
        return tank;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return Math.max(1, processTime);
    }

    public int getConsumption() {
        return consumption;
    }

    public ItemStack getDisplay() {
        return display;
    }

    public boolean mayPlace(int slot, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (slot < 3) {
            return true;
        }
        if (slot == SLOT_BATTERY) {
            return ItemEnergyHelper.isEnergyItem(stack);
        }
        if (slot == SLOT_FLUID_ID) {
            return stack.is(ModItems.FLUID_IDENTIFIER.get());
        }
        return false;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.machineArcWelder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new ArcWelderMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArcWelderBlockEntity be) {
        if (!level.isClientSide) {
            be.serverTick();
        }
    }

    private void serverTick() {
        pullEnergy();
        ItemEnergyHelper.chargeFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getMaxEnergyStored());
        ArcWelderRecipe recipe = ArcWelderRecipes.getRecipe(
                items.getStackInSlot(0), items.getStackInSlot(1), items.getStackInSlot(2));
        int intendedMax;
        if (recipe != null) {
            processTime = Math.max(1, recipe.duration());
            consumption = Math.max(1, recipe.consumption());
            intendedMax = consumption * 20;
            display = recipe.output().resultStack();
            if (display.getCount() > 1) {
                display = display.copy();
                display.setCount(1);
            }
            if (canProcess(recipe)) {
                progress++;
                energy.consume(consumption);
                if (progress >= processTime) {
                    progress = 0;
                    consumeRecipe(recipe);
                    ItemStack made = recipe.output().resultStack();
                    ItemStack out = items.getStackInSlot(SLOT_OUTPUT);
                    if (out.isEmpty()) {
                        items.setStackInSlot(SLOT_OUTPUT, made);
                    } else {
                        out.grow(made.getCount());
                    }
                }
            } else {
                progress = 0;
            }
        } else {
            progress = 0;
            consumption = DEFAULT_CONSUMPTION;
            intendedMax = DEFAULT_MAX_POWER;
            display = ItemStack.EMPTY;
        }
        energy.setCapacity(Math.max(intendedMax, energy.getEnergyStored()));
        onChanged();
    }

    public boolean canProcess(ArcWelderRecipe recipe) {
        if (energy.getEnergyStored() < consumption) {
            return false;
        }
        if (recipe.fluid() != null && !ArcWelderRecipes.matchesFluid(recipe, tank.getFluid())) {
            return false;
        }
        ItemStack made = recipe.output().resultStack();
        if (made.isEmpty()) {
            return false;
        }
        ItemStack out = items.getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(out, made)
                && out.getCount() + made.getCount() <= out.getMaxStackSize();
    }

    private void consumeRecipe(ArcWelderRecipe recipe) {
        for (IngredientRef need : recipe.inputs()) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = items.getStackInSlot(i);
                if (need.matches(stack, false)) {
                    stack.shrink(need.count());
                    if (stack.isEmpty()) {
                        items.setStackInSlot(i, ItemStack.EMPTY);
                    }
                    break;
                }
            }
        }
        if (recipe.fluid() != null) {
            tank.drain(recipe.fluid().amount(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void pullEnergy() {
        if (level == null) {
            return;
        }
        int transfer = Math.max(energy.getMaxEnergyStored(), 1);
        EnergyNetworkHelper.pullFromNeighbors(level, worldPosition, energy, transfer);
        int facing = DummyableMeta.SOUTH;
        if (getBlockState().hasProperty(BlockDummyable.META)) {
            facing = DummyableMeta.coreFacing(getBlockState().getValue(BlockDummyable.META));
        }
        for (MultiblockHandlerXR.Cell cell : MultiblockHandlerXR.dummyCells(
                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                MachineArcWelderBlock.DIM, facing)) {
            EnergyNetworkHelper.pullFromNeighbors(level,
                    new BlockPos(cell.x(), cell.y(), cell.z()), energy, transfer);
        }
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.putInt("MaxPower", energy.getMaxEnergyStored());
        tag.put("Items", items.serializeNBT());
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("Progress", progress);
        tag.putInt("ProcessTime", processTime);
        tag.putInt("Consumption", consumption);
        if (!display.isEmpty()) {
            tag.put("Display", display.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("MaxPower")) {
            energy.setCapacity(Math.max(1, tag.getInt("MaxPower")));
        }
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        if (tag.contains("Tank")) {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
        progress = tag.getInt("Progress");
        processTime = Math.max(1, tag.getInt("ProcessTime"));
        consumption = Math.max(1, tag.getInt("Consumption"));
        display = tag.contains("Display") ? ItemStack.of(tag.getCompound("Display")) : ItemStack.EMPTY;
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
        fluidOptional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
