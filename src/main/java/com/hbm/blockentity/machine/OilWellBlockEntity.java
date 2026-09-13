package com.hbm.blockentity.machine;

import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.OilWellMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModFluids;
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

import java.util.HashSet;

/**
 * 1.7.10 {@code TileEntityMachineOilWell}. Upgrades, afterburn, radon/asbestos skipped.
 */
public class OilWellBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_OIL_IN = 1;
    public static final int SLOT_OIL_OUT = 2;
    public static final int SLOT_GAS_IN = 3;
    public static final int SLOT_GAS_OUT = 4;
    public static final int SLOT_COUNT = 8;

    public static final int ENERGY_CAPACITY = 100_000;
    public static final int ENERGY_PER_TICK = 100;
    public static final int DELAY = 50;
    public static final int OIL_PER_DEPOSIT = 500;
    public static final int GAS_MIN = 100;
    public static final int GAS_MAX = 500;
    public static final double DRAIN_CHANCE = 0.05D;
    public static final int TANK_CAPACITY = 64_000;
    public static final int DRILL_DEPTH = 5;
    public static final int SUCK_RANGE = 64;

    private final ModEnergyStorage energy = new ModEnergyStorage(ENERGY_CAPACITY, ENERGY_CAPACITY, 0, this::onChanged);
    private final FluidTank oil = typedTank(true);
    private final FluidTank gas = typedTank(false);
    private final IFluidHandler fluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return tank == 0 ? oil.getFluid() : tank == 1 ? gas.getFluid() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 || tank == 1 ? TANK_CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            if (isOil(resource)) {
                return oil.drain(resource, action);
            }
            if (isGas(resource)) {
                return gas.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!oil.getFluid().isEmpty()) {
                return oil.drain(maxDrain, action);
            }
            return gas.drain(maxDrain, action);
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
                case SLOT_OIL_IN, SLOT_GAS_IN -> true;
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
            if (slot != SLOT_BATTERY && slot != SLOT_OIL_IN && slot != SLOT_GAS_IN) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_OIL_OUT && slot != SLOT_GAS_OUT) {
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

    private int indicator;

    public OilWellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_WELL.get(), pos, state);
    }

    private FluidTank typedTank(boolean crude) {
        return new FluidTank(TANK_CAPACITY) {
            @Override
            protected void onContentsChanged() {
                onChanged();
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                return crude ? isOil(stack) : isGas(stack);
            }
        };
    }

    private static boolean isOil(FluidStack stack) {
        return !stack.isEmpty() && stack.getFluid() == ModFluids.OIL.source.get();
    }

    private static boolean isGas(FluidStack stack) {
        return !stack.isEmpty() && stack.getFluid() == ModFluids.GAS.source.get();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public FluidTank getOil() {
        return oil;
    }

    public FluidTank getGas() {
        return gas;
    }

    public int getIndicator() {
        return indicator;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.oilWell");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new OilWellMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OilWellBlockEntity be) {
        if (!level.isClientSide) {
            be.serverTick(level, pos);
        }
    }

    private void serverTick(Level level, BlockPos pos) {
        ItemEnergyHelper.dischargeItemIntoBuffer(items.getStackInSlot(SLOT_BATTERY), energy);
        LauncherFluidTransfer.fillContainer(items, SLOT_OIL_IN, SLOT_OIL_OUT, oil);
        LauncherFluidTransfer.fillContainer(items, SLOT_GAS_IN, SLOT_GAS_OUT, gas);

        if (energy.getEnergyStored() >= ENERGY_PER_TICK
                && oil.getFluidAmount() < oil.getCapacity()
                && gas.getFluidAmount() < gas.getCapacity()) {
            energy.consume(ENERGY_PER_TICK);
            if (level.getGameTime() % DELAY == 0) {
                indicator = 0;
                boolean acted = false;
                for (int y = pos.getY() - 1; y >= DRILL_DEPTH; y--) {
                    BlockPos at = new BlockPos(pos.getX(), y, pos.getZ());
                    BlockState state = level.getBlockState(at);
                    if (!state.is(ModBlocks.OIL_PIPE.get())) {
                        if (trySuck(level, at)) {
                            acted = true;
                        } else {
                            tryDrill(level, at);
                            acted = true;
                        }
                        break;
                    }
                    if (y == DRILL_DEPTH) {
                        indicator = 1;
                    }
                }
                if (!acted && indicator == 0) {
                    indicator = 1;
                }
            }
        } else {
            indicator = 2;
        }
        onChanged();
    }

    private void tryDrill(Level level, BlockPos at) {
        BlockState state = level.getBlockState(at);
        if (state.getBlock().getExplosionResistance() < 1000.0F) {
            level.setBlock(at, ModBlocks.OIL_PIPE.get().defaultBlockState(), Block.UPDATE_ALL);
        } else {
            indicator = 2;
        }
    }

    private boolean trySuck(Level level, BlockPos at) {
        if (!canSuck(level.getBlockState(at))) {
            return false;
        }
        HashSet<Long> trace = new HashSet<>();
        return suckRec(level, at, 0, trace);
    }

    private static boolean canSuck(BlockState state) {
        return state.is(ModBlocks.ORE_OIL.get()) || state.is(ModBlocks.ORE_OIL_EMPTY.get());
    }

    private boolean suckRec(Level level, BlockPos at, int layer, HashSet<Long> trace) {
        if (!trace.add(at.asLong()) || layer > SUCK_RANGE) {
            return false;
        }
        BlockState state = level.getBlockState(at);
        if (state.is(ModBlocks.ORE_OIL.get())) {
            onSuck(level, at);
            return true;
        }
        if (state.is(ModBlocks.ORE_OIL_EMPTY.get())) {
            Direction[] dirs = Direction.values();
            for (int i = dirs.length - 1; i > 0; i--) {
                int j = level.random.nextInt(i + 1);
                Direction tmp = dirs[i];
                dirs[i] = dirs[j];
                dirs[j] = tmp;
            }
            for (Direction dir : dirs) {
                if (suckRec(level, at.relative(dir), layer + 1, trace)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void onSuck(Level level, BlockPos at) {
        level.playSound(null, worldPosition, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 2.0F, 0.5F);
        int oilAdd = Math.min(OIL_PER_DEPOSIT, oil.getCapacity() - oil.getFluidAmount());
        if (oilAdd > 0) {
            oil.fill(new FluidStack(ModFluids.OIL.source.get(), oilAdd), IFluidHandler.FluidAction.EXECUTE);
        }
        int gasAmt = GAS_MIN + level.random.nextInt(GAS_MAX - GAS_MIN + 1);
        int gasAdd = Math.min(gasAmt, gas.getCapacity() - gas.getFluidAmount());
        if (gasAdd > 0) {
            gas.fill(new FluidStack(ModFluids.GAS.source.get(), gasAdd), IFluidHandler.FluidAction.EXECUTE);
        }
        if (level.random.nextDouble() < DRAIN_CHANCE) {
            level.setBlock(at, ModBlocks.ORE_OIL_EMPTY.get().defaultBlockState(), Block.UPDATE_ALL);
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
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 10, 2));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Oil", oil.writeToNBT(new CompoundTag()));
        tag.put("Gas", gas.writeToNBT(new CompoundTag()));
        tag.putInt("Indicator", indicator);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        if (tag.contains("Oil")) {
            oil.readFromNBT(tag.getCompound("Oil"));
        }
        if (tag.contains("Gas")) {
            gas.readFromNBT(tag.getCompound("Gas"));
        }
        indicator = tag.getInt("Indicator");
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
