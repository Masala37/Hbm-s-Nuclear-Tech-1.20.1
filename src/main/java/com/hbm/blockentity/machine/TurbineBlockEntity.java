package com.hbm.blockentity.machine;

import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.fluid.IoFluidHandler;
import com.hbm.fluid.SteamCycle;
import com.hbm.inventory.menu.TurbineMenu;
import com.hbm.registry.ModBlockEntities;
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
 * 1.7.10 {@code TileEntityMachineTurbine}. OpenComputers skipped.
 */
public class TurbineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_ID_IN = 0;
    public static final int SLOT_ID_OUT = 1;
    public static final int SLOT_IN = 2;
    public static final int SLOT_IN_EMPTY = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_OUT = 5;
    public static final int SLOT_OUT_EMPTY = 6;
    public static final int SLOT_COUNT = 7;
    public static final int MAX_POWER = 1_000_000;
    public static final int INPUT_CAP = 64_000;
    public static final int OUTPUT_CAP = 128_000;
    public static final int MAX_STEAM_PER_TICK = 6_000;
    public static final double EFFICIENCY = 0.85D;

    private final ModEnergyStorage energy = new ModEnergyStorage(MAX_POWER, 0, MAX_POWER, this::onChanged);
    private final FluidTank input = new FluidTank(INPUT_CAP) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return SteamCycle.turbineStep(stack.getFluid()) != null;
        }
    };
    private final FluidTank output = new FluidTank(OUTPUT_CAP) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }
    };
    private final IFluidHandler fluids = new IoFluidHandler(input, output,
            stack -> SteamCycle.turbineStep(stack.getFluid()) != null);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_BATTERY) {
                return ItemEnergyHelper.isEnergyItem(stack);
            }
            return slot == SLOT_IN || slot == SLOT_OUT;
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
            if (slot != SLOT_BATTERY) {
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
            return slot == SLOT_BATTERY && items.isItemValid(slot, stack);
        }
    };

    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    public TurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_TURBINE.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public FluidTank getInput() {
        return input;
    }

    public FluidTank getOutput() {
        return output;
    }

    public IFluidHandler getFluidHandler() {
        return fluids;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.machineTurbine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new TurbineMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TurbineBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        EnergyNetworkHelper.pushToNeighbors(level, pos, be.energy, MAX_POWER);
        LauncherFluidTransfer.emptyContainer(be.items, SLOT_IN, SLOT_IN_EMPTY, be.input);
        ItemEnergyHelper.chargeItemFromBuffer(be.items.getStackInSlot(SLOT_BATTERY), be.energy);
        be.energy.setEnergy((int) (be.energy.getEnergyStored() * 0.95D));

        SteamCycle.CoolStep step = SteamCycle.turbineStep(be.input.getFluid().getFluid());
        if (step != null && !be.input.getFluid().isEmpty()) {
            if (be.output.getFluid().isEmpty() || be.output.getFluid().getFluid() == step.output()) {
                int ops = SteamCycle.turbineOps(be.input.getFluidAmount(), be.output.getFluidAmount(),
                        be.output.getCapacity(), step.amountReq(), step.amountProduced(), MAX_STEAM_PER_TICK);
                if (ops > 0) {
                    be.input.drain(ops * step.amountReq(), IFluidHandler.FluidAction.EXECUTE);
                    be.output.fill(new FluidStack(step.output(), ops * step.amountProduced()),
                            IFluidHandler.FluidAction.EXECUTE);
                    int generated = (int) (ops * step.heatEnergy() * step.turbineEfficiency(EFFICIENCY));
                    be.energy.setEnergy(Math.min(MAX_POWER, be.energy.getEnergyStored() + generated));
                }
            }
        }
        LauncherFluidTransfer.fillContainer(be.items, SLOT_OUT, SLOT_OUT_EMPTY, be.output);
        be.onChanged();
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
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Input", input.writeToNBT(new CompoundTag()));
        tag.put("Output", output.writeToNBT(new CompoundTag()));
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
        if (tag.contains("Output")) {
            output.readFromNBT(tag.getCompound("Output"));
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
        fluidOptional.invalidate();
        itemOptional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
