package com.hbm.blockentity.machine;

import com.hbm.HbmNuclearTechMod;
import com.hbm.blocks.machine.DieselGeneratorBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.DieselGeneratorMenu;
import com.hbm.items.machine.InfiniteFluidBarrelItem;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModItems;
import com.hbm.registry.ModSounds;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid-fueled generator (legacy TileEntityMachineDiesel).
 * Burns diesel / gasoline / light oil at 1 mB/t into FE; redstone disables output.
 */
public class DieselGeneratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_FUEL_IN = 0;
    public static final int SLOT_FUEL_OUT = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_ID_IN = 3;
    public static final int SLOT_ID_OUT = 4;
    public static final int SLOT_COUNT = 5;
    public static final int ENERGY_CAPACITY = 50_000;
    public static final int MAX_EXTRACT = 1_000;
    public static final int TANK_CAPACITY = 16_000;

    private final ModEnergyStorage energy = new ModEnergyStorage(ENERGY_CAPACITY, 0, MAX_EXTRACT, this::onChanged);
    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return getFuelPower(stack.getFluid()) > 0;
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
                case SLOT_FUEL_IN -> isFluidItem(stack);
                case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
                case SLOT_ID_IN -> stack.is(ModItems.FLUID_IDENTIFIER.get());
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
            if (slot != SLOT_FUEL_IN && slot != SLOT_BATTERY) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_FUEL_OUT && slot != SLOT_BATTERY) {
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
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> tank);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);

    private boolean running;

    public DieselGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DIESEL_GENERATOR.get(), pos, state);
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

    public IFluidHandler getFluidHandler() {
        return tank;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean hasFuelForRender() {
        return getFuelPower(tank.getFluid().getFluid()) > 0 && tank.getFluidAmount() > 0;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.diesel_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new DieselGeneratorMenu(id, inv, this);
    }

    /**
     * FE produced per mB consumed (legacy HE/1000 * grade efficiency).
     */
    public static int getFuelPower(Fluid fluid) {
        if (fluid == ModFluids.DIESEL.source.get()) {
            return 375; // HIGH 500_000 / 1000 * 0.75
        }
        if (fluid == ModFluids.GASOLINE.source.get()) {
            return 750; // HIGH 1_000_000 / 1000 * 0.75
        }
        if (fluid == ModFluids.LIGHTOIL.source.get()) {
            return 250; // MEDIUM 500_000 / 1000 * 0.5
        }
        return 0;
    }

    static boolean isFluidItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof InfiniteFluidBarrelItem) {
            return true;
        }
        return FluidUtil.getFluidHandler(stack).isPresent();
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DieselGeneratorBlockEntity be) {
        boolean wasRunning = be.running;
        be.running = false;

        if (be.items.getStackInSlot(SLOT_FUEL_IN).getItem() instanceof InfiniteFluidBarrelItem) {
            InfiniteFluidBarrelItem.fillTank(be.tank, ModFluids.DIESEL.source.get());
        } else {
            LauncherFluidTransfer.emptyContainer(be.items, SLOT_FUEL_IN, SLOT_FUEL_OUT, be.tank);
        }
        ItemEnergyHelper.chargeItemFromBuffer(be.items.getStackInSlot(SLOT_BATTERY), be.energy);

        // Legacy: redstone signal shuts the engine down.
        if (!level.hasNeighborSignal(pos)) {
            FluidStack fuel = be.tank.getFluid();
            int powerPerMb = getFuelPower(fuel.getFluid());
            if (powerPerMb > 0 && fuel.getAmount() > 0 && be.energy.getEnergyStored() < be.energy.getMaxEnergyStored()) {
                be.tank.drain(1, IFluidHandler.FluidAction.EXECUTE);
                int room = be.energy.getMaxEnergyStored() - be.energy.getEnergyStored();
                be.energy.setEnergy(be.energy.getEnergyStored() + Math.min(powerPerMb, room));
                be.running = true;
            }
        }

        EnergyNetworkHelper.pushToNeighbors(level, pos, be.energy, MAX_EXTRACT);

        if (state.getValue(DieselGeneratorBlock.LIT) != be.running) {
            level.setBlock(pos, state.setValue(DieselGeneratorBlock.LIT, be.running), Block.UPDATE_CLIENTS);
        }

        if (wasRunning != be.running) {
            be.onChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DieselGeneratorBlockEntity be) {
        HbmNuclearTechMod.proxy.tickMachineLoop(level, pos, state.getBlock(), DieselGeneratorBlock.LIT,
                ModSounds.ENGINE.get(), 1.0F);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("Running", running);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Tank")) {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        running = tag.getBoolean("Running");
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

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
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
