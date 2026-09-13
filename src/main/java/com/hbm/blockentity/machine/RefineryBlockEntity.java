package com.hbm.blockentity.machine;

import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.RefineryMenu;
import com.hbm.inventory.recipes.RefineryRecipes;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachineRefinery}. Explosion, repair, pollution, and crack/DS skipped.
 */
public class RefineryBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_IN = 1;
    public static final int SLOT_IN_EMPTY = 2;
    public static final int SLOT_HEAVY_IN = 3;
    public static final int SLOT_HEAVY_OUT = 4;
    public static final int SLOT_NAPHTHA_IN = 5;
    public static final int SLOT_NAPHTHA_OUT = 6;
    public static final int SLOT_LIGHT_IN = 7;
    public static final int SLOT_LIGHT_OUT = 8;
    public static final int SLOT_PETRO_IN = 9;
    public static final int SLOT_PETRO_OUT = 10;
    public static final int SLOT_SULFUR = 11;
    public static final int SLOT_FLUID_ID = 12;
    public static final int SLOT_COUNT = 13;

    public static final int ENERGY_CAPACITY = 1_000;
    public static final int INPUT_CAP = 64_000;
    public static final int OUTPUT_CAP = 24_000;

    private final ModEnergyStorage energy = new ModEnergyStorage(ENERGY_CAPACITY, ENERGY_CAPACITY, 0, this::onChanged);
    private final FluidTank input = tank(INPUT_CAP, true);
    private final FluidTank heavy = tank(OUTPUT_CAP, false);
    private final FluidTank naphtha = tank(OUTPUT_CAP, false);
    private final FluidTank light = tank(OUTPUT_CAP, false);
    private final FluidTank petroleum = tank(OUTPUT_CAP, false);
    private final FluidTank[] tanks = {input, heavy, naphtha, light, petroleum};
    private final IFluidHandler fluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return tanks.length;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return tank >= 0 && tank < tanks.length ? tanks[tank].getFluid() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank >= 0 && tank < tanks.length ? tanks[tank].getCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0 && isHotOil(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!isHotOil(resource)) {
                return 0;
            }
            return input.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            for (int i = 1; i < tanks.length; i++) {
                FluidStack have = tanks[i].getFluid();
                if (!have.isEmpty() && have.getFluid() == resource.getFluid()) {
                    return tanks[i].drain(resource, action);
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            for (int i = 1; i < tanks.length; i++) {
                if (!tanks[i].getFluid().isEmpty()) {
                    return tanks[i].drain(maxDrain, action);
                }
            }
            return FluidStack.EMPTY;
        }
    };
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
                case SLOT_FLUID_ID -> stack.is(ModItems.FLUID_IDENTIFIER.get());
                case SLOT_IN, SLOT_HEAVY_IN, SLOT_NAPHTHA_IN, SLOT_LIGHT_IN, SLOT_PETRO_IN -> true;
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
            if (slot == SLOT_SULFUR || slot == SLOT_IN_EMPTY || slot == SLOT_HEAVY_OUT
                    || slot == SLOT_NAPHTHA_OUT || slot == SLOT_LIGHT_OUT || slot == SLOT_PETRO_OUT) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_SULFUR && slot != SLOT_IN_EMPTY && slot != SLOT_HEAVY_OUT
                    && slot != SLOT_NAPHTHA_OUT && slot != SLOT_LIGHT_OUT && slot != SLOT_PETRO_OUT) {
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
    private LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);

    private int sulfur;
    private boolean isOn;

    public RefineryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_REFINERY.get(), pos, state);
    }

    private FluidTank tank(int cap, boolean inputTank) {
        return new FluidTank(cap) {
            @Override
            protected void onContentsChanged() {
                onChanged();
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                return inputTank && isHotOil(stack);
            }
        };
    }

    private static boolean isHotOil(FluidStack stack) {
        return !stack.isEmpty() && stack.getFluid() == ModFluids.HOTOIL.source.get();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public FluidTank getInputTank() {
        return input;
    }

    public FluidTank getHeavy() {
        return heavy;
    }

    public FluidTank getNaphtha() {
        return naphtha;
    }

    public FluidTank getLight() {
        return light;
    }

    public FluidTank getPetroleum() {
        return petroleum;
    }

    public boolean isOn() {
        return isOn;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.machineRefinery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new RefineryMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RefineryBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        ItemEnergyHelper.dischargeItemIntoBuffer(be.items.getStackInSlot(SLOT_BATTERY), be.energy);
        LauncherFluidTransfer.emptyContainer(be.items, SLOT_IN, SLOT_IN_EMPTY, be.input);
        LauncherFluidTransfer.fillContainer(be.items, SLOT_HEAVY_IN, SLOT_HEAVY_OUT, be.heavy);
        LauncherFluidTransfer.fillContainer(be.items, SLOT_NAPHTHA_IN, SLOT_NAPHTHA_OUT, be.naphtha);
        LauncherFluidTransfer.fillContainer(be.items, SLOT_LIGHT_IN, SLOT_LIGHT_OUT, be.light);
        LauncherFluidTransfer.fillContainer(be.items, SLOT_PETRO_IN, SLOT_PETRO_OUT, be.petroleum);
        be.refine();
        be.onChanged();
    }

    private void refine() {
        isOn = false;
        if (energy.getEnergyStored() < RefineryRecipes.POWER_PER_OP
                || input.getFluidAmount() < RefineryRecipes.INPUT_MB) {
            return;
        }
        if (heavy.getFluidAmount() + RefineryRecipes.HEAVY_MB > heavy.getCapacity()
                || naphtha.getFluidAmount() + RefineryRecipes.NAPHTHA_MB > naphtha.getCapacity()
                || light.getFluidAmount() + RefineryRecipes.LIGHT_MB > light.getCapacity()
                || petroleum.getFluidAmount() + RefineryRecipes.PETRO_MB > petroleum.getCapacity()) {
            return;
        }
        input.drain(RefineryRecipes.INPUT_MB, IFluidHandler.FluidAction.EXECUTE);
        heavy.fill(new FluidStack(ModFluids.HEAVYOIL.source.get(), RefineryRecipes.HEAVY_MB),
                IFluidHandler.FluidAction.EXECUTE);
        naphtha.fill(new FluidStack(ModFluids.NAPHTHA.source.get(), RefineryRecipes.NAPHTHA_MB),
                IFluidHandler.FluidAction.EXECUTE);
        light.fill(new FluidStack(ModFluids.LIGHTOIL.source.get(), RefineryRecipes.LIGHT_MB),
                IFluidHandler.FluidAction.EXECUTE);
        petroleum.fill(new FluidStack(ModFluids.PETROLEUM.source.get(), RefineryRecipes.PETRO_MB),
                IFluidHandler.FluidAction.EXECUTE);
        energy.consume(RefineryRecipes.POWER_PER_OP);
        sulfur++;
        if (sulfur >= RefineryRecipes.SULFUR_EVERY) {
            sulfur -= RefineryRecipes.SULFUR_EVERY;
            ItemStack out = items.getStackInSlot(SLOT_SULFUR);
            ItemStack sulfurStack = new ItemStack(ModItems.SULFUR.get());
            if (out.isEmpty()) {
                items.setStackInSlot(SLOT_SULFUR, sulfurStack);
            } else if (ItemStack.isSameItemSameTags(out, sulfurStack)
                    && out.getCount() < out.getMaxStackSize()) {
                out.grow(1);
                items.setStackInSlot(SLOT_SULFUR, out);
            }
        }
        isOn = true;
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 9, 3));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Input", input.writeToNBT(new CompoundTag()));
        tag.put("Heavy", heavy.writeToNBT(new CompoundTag()));
        tag.put("Naphtha", naphtha.writeToNBT(new CompoundTag()));
        tag.put("Light", light.writeToNBT(new CompoundTag()));
        tag.put("Petroleum", petroleum.writeToNBT(new CompoundTag()));
        tag.putInt("Sulfur", sulfur);
        tag.putBoolean("IsOn", isOn);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        if (tag.contains("Input")) {
            input.readFromNBT(tag.getCompound("Input"));
        }
        if (tag.contains("Heavy")) {
            heavy.readFromNBT(tag.getCompound("Heavy"));
        }
        if (tag.contains("Naphtha")) {
            naphtha.readFromNBT(tag.getCompound("Naphtha"));
        }
        if (tag.contains("Light")) {
            light.readFromNBT(tag.getCompound("Light"));
        }
        if (tag.contains("Petroleum")) {
            petroleum.readFromNBT(tag.getCompound("Petroleum"));
        }
        sulfur = tag.getInt("Sulfur");
        isOn = tag.getBoolean("IsOn");
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
