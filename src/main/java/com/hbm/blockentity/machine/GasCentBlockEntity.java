package com.hbm.blockentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.GasCentMenu;
import com.hbm.inventory.recipes.GasCentrifugeRecipes;
import com.hbm.inventory.recipes.GasCentrifugeRecipes.PseudoFluidType;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.material.Fluid;
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
 * 1.7.10 {@code TileEntityMachineGasCent}. Speed upgrade skipped (150 ticks / 200 FE/t).
 */
public class GasCentBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_OUTPUT_0 = 0;
    public static final int SLOT_OUTPUT_3 = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_FLUID_ID = 5;
    public static final int SLOT_UPGRADE = 6;
    public static final int SLOT_COUNT = 7;

    public static final int MAX_POWER = 100_000;
    public static final int PROCESSING_SPEED = 150;
    public static final int CONSUMPTION = 200;
    public static final int TANK_CAPACITY = 2_000;
    public static final int PSEUDO_CAPACITY = 8_000;
    public static final int LEUF6_DUMP = 600;

    private final ModEnergyStorage energy = new ModEnergyStorage(MAX_POWER, MAX_POWER, 0, this::onChanged);
    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty() && stack.getFluid() == tankType;
        }
    };
    private final IFluidHandler fluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluid();
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tankIndex, @NotNull FluidStack stack) {
            return tank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return tank.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
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
            return GasCentBlockEntity.this.mayPlace(slot, stack);
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
            return stack;
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
            return false;
        }
    };

    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automation);
    private LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);

    private Fluid tankType;
    private PseudoFluidType inputType = GasCentrifugeRecipes.byName(GasCentrifugeRecipes.NONE);
    private PseudoFluidType outputType = GasCentrifugeRecipes.byName(GasCentrifugeRecipes.NONE);
    private int inputFill;
    private int outputFill;
    private int progress;
    private boolean isProgressing;
    private boolean hadIdentifier;
    private int soundCycle;

    public GasCentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_GASCENT.get(), pos, state);
        applyDefaultTypes();
    }

    private void applyDefaultTypes() {
        Fluid first = GasCentrifugeRecipes.firstConversion();
        tankType = first;
        PseudoFluidType pseudo = GasCentrifugeRecipes.conversion(first);
        if (pseudo != null) {
            inputType = pseudo;
            outputType = pseudo.outputType();
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public Fluid getTankType() {
        return tankType != null ? tankType : GasCentrifugeRecipes.firstConversion();
    }

    public FluidTank getTank() {
        return tank;
    }

    public PseudoFluidType getInputType() {
        return inputType == null ? GasCentrifugeRecipes.byName(GasCentrifugeRecipes.NONE) : inputType;
    }

    public PseudoFluidType getOutputType() {
        return outputType == null ? GasCentrifugeRecipes.byName(GasCentrifugeRecipes.NONE) : outputType;
    }

    public int getInputFill() {
        return inputFill;
    }

    public int getOutputFill() {
        return outputFill;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isProgressing() {
        return isProgressing;
    }

    public int getProgressScaled(int pixels) {
        return (progress * pixels) / PROCESSING_SPEED;
    }

    public int getPowerScaled(int pixels) {
        return (int) ((long) energy.getEnergyStored() * pixels / Math.max(1, energy.getMaxEnergyStored()));
    }

    public void setTankType(Fluid type) {
        if (type == null || type == tankType || !GasCentrifugeRecipes.isConversion(type)) {
            return;
        }
        tankType = type;
        tank.setFluid(FluidStack.EMPTY);
        PseudoFluidType pseudo = GasCentrifugeRecipes.conversion(type);
        inputType = pseudo;
        outputType = pseudo.outputType();
        inputFill = 0;
        outputFill = 0;
        progress = 0;
        onChanged();
    }

    public boolean mayPlace(int slot, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
            case SLOT_FLUID_ID -> stack.is(ModItems.FLUID_IDENTIFIER.get());
            default -> false;
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.gasCentrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new GasCentMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GasCentBlockEntity be) {
        if (!level.isClientSide) {
            be.serverTick(level, pos);
        }
    }

    private void serverTick(Level level, BlockPos pos) {
        if (tankType == null) {
            tankType = GasCentrifugeRecipes.firstConversion();
        }
        EnergyNetworkHelper.pullFromNeighbors(level, pos, energy, MAX_POWER);
        EnergyNetworkHelper.pullFromNeighbors(level, pos.below(), energy, MAX_POWER);
        ItemEnergyHelper.chargeFromItem(items.getStackInSlot(SLOT_BATTERY), energy, MAX_POWER);

        boolean now = items.getStackInSlot(SLOT_FLUID_ID).is(ModItems.FLUID_IDENTIFIER.get());
        if (now && !hadIdentifier) {
            setTankType(GasCentrifugeRecipes.cycleNext(getTankType()));
        }
        hadIdentifier = now;

        if (GasCentrifugeRecipes.isConversionTarget(getInputType())) {
            attemptConversion();
        }

        if (canEnrich()) {
            isProgressing = true;
            progress++;
            int stored = energy.getEnergyStored();
            energy.consume(Math.min(CONSUMPTION, stored));
            if (stored < CONSUMPTION) {
                progress = 0;
            }
            if (progress >= PROCESSING_SPEED) {
                enrich();
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
            isProgressing = false;
            progress = 0;
            soundCycle = 0;
        }

        if (level.getGameTime() % 10L == 0L) {
            if (!attemptTransfer() && getInputType().name().equals("LEUF6")) {
                dumpLeuf6();
            }
        }
        onChanged();
    }

    private void attemptConversion() {
        if (inputFill >= PSEUDO_CAPACITY || tank.getFluidAmount() <= 0) {
            return;
        }
        int fill = Math.min(PSEUDO_CAPACITY - inputFill, tank.getFluidAmount());
        tank.drain(fill, IFluidHandler.FluidAction.EXECUTE);
        inputFill += fill;
    }

    private boolean canEnrich() {
        if (energy.getEnergyStored() <= 0) {
            return false;
        }
        PseudoFluidType type = getInputType();
        if (type.isNone() || type.fluidConsumed() <= 0 || inputFill < type.fluidConsumed()) {
            return false;
        }
        if (outputFill + type.fluidProduced() > PSEUDO_CAPACITY) {
            return false;
        }
        if (type.highSpeed()) {
            return false;
        }
        ItemStack[] outputs = type.outputStacks();
        return outputs.length > 0 && hasSpace(outputs);
    }

    private void enrich() {
        PseudoFluidType type = getInputType();
        ItemStack[] outputs = type.outputStacks();
        progress = 0;
        inputFill -= type.fluidConsumed();
        outputFill += type.fluidProduced();
        addOutputs(outputs);
    }

    private boolean attemptTransfer() {
        if (level == null) {
            return false;
        }
        int facing = DummyableMeta.coreFacing(getBlockState().hasProperty(BlockDummyable.META)
                ? getBlockState().getValue(BlockDummyable.META)
                : DummyableMeta.coreMeta(DummyableMeta.SOUTH));
        BlockPos behind = worldPosition.offset(-DummyableMeta.offsetX(facing), 0, -DummyableMeta.offsetZ(facing));
        BlockEntity te = level.getBlockEntity(behind);
        if (!(te instanceof GasCentBlockEntity other)) {
            return false;
        }
        if (other.getTankType() != getTankType()) {
            return false;
        }
        if (other.getInputType() != getOutputType() && !getOutputType().isNone()) {
            other.inputType = getOutputType();
            other.outputType = getOutputType().outputType();
            other.inputFill = 0;
            other.outputFill = 0;
        }
        if (other.inputFill < PSEUDO_CAPACITY && outputFill > 0) {
            int fill = Math.min(PSEUDO_CAPACITY - other.inputFill, outputFill);
            outputFill -= fill;
            other.inputFill += fill;
            other.onChanged();
        }
        return true;
    }

    private void dumpLeuf6() {
        if (outputFill < LEUF6_DUMP) {
            return;
        }
        ItemStack[] converted = new ItemStack[] {
                stack("hbm:nugget_uranium_fuel", 6),
                stack("hbm:fluorite", 1)
        };
        if (converted[0].isEmpty() || converted[1].isEmpty() || !hasSpace(converted)) {
            return;
        }
        outputFill -= LEUF6_DUMP;
        addOutputs(converted);
    }

    private boolean hasSpace(ItemStack[] outputs) {
        ItemStack[] copy = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            copy[i] = items.getStackInSlot(i).copy();
        }
        for (ItemStack produced : outputs) {
            if (produced == null || produced.isEmpty()) {
                continue;
            }
            int remaining = produced.getCount();
            for (int i = 0; i < 4 && remaining > 0; i++) {
                ItemStack slot = copy[i];
                if (slot.isEmpty()) {
                    ItemStack placed = produced.copy();
                    placed.setCount(remaining);
                    copy[i] = placed;
                    remaining = 0;
                    break;
                }
                if (ItemStack.isSameItemSameTags(slot, produced)
                        && slot.getCount() + remaining <= slot.getMaxStackSize()) {
                    slot.grow(remaining);
                    remaining = 0;
                    break;
                }
                if (ItemStack.isSameItemSameTags(slot, produced)
                        && slot.getCount() < slot.getMaxStackSize()) {
                    int room = slot.getMaxStackSize() - slot.getCount();
                    int used = Math.min(room, remaining);
                    slot.grow(used);
                    remaining -= used;
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private void addOutputs(ItemStack[] outputs) {
        for (ItemStack produced : outputs) {
            if (produced == null || produced.isEmpty()) {
                continue;
            }
            ItemStack remaining = produced.copy();
            for (int i = 0; i < 4 && !remaining.isEmpty(); i++) {
                ItemStack slot = items.getStackInSlot(i);
                if (slot.isEmpty()) {
                    items.setStackInSlot(i, remaining);
                    remaining = ItemStack.EMPTY;
                    break;
                }
                if (ItemStack.isSameItemSameTags(slot, remaining)
                        && slot.getCount() < slot.getMaxStackSize()) {
                    int used = Math.min(slot.getMaxStackSize() - slot.getCount(), remaining.getCount());
                    slot.grow(used);
                    remaining.shrink(used);
                }
            }
        }
    }

    private static ItemStack stack(String id, int count) {
        var item = BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.ResourceLocation.tryParse(id));
        return item.map(value -> new ItemStack(value, count)).orElse(ItemStack.EMPTY);
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 5, 1));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        if (tankType != null) {
            var key = BuiltInRegistries.FLUID.getKey(tankType);
            if (key != null) {
                tag.putString("TankType", key.toString());
            }
        }
        tag.putString("inputType", getInputType().name());
        tag.putString("outputType", getOutputType().name());
        tag.putInt("inputFill", inputFill);
        tag.putInt("outputFill", outputFill);
        tag.putInt("progress", progress);
        tag.putBoolean("isProgressing", isProgressing);
        tag.putBoolean("HadIdentifier", hadIdentifier);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        if (tag.contains("tank")) {
            tank.readFromNBT(tag.getCompound("tank"));
        }
        if (tag.contains("TankType")) {
            var key = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("TankType"));
            tankType = key == null ? GasCentrifugeRecipes.firstConversion() : BuiltInRegistries.FLUID.get(key);
        } else {
            tankType = GasCentrifugeRecipes.firstConversion();
        }
        inputType = GasCentrifugeRecipes.byName(tag.getString("inputType"));
        outputType = GasCentrifugeRecipes.byName(tag.getString("outputType"));
        inputFill = tag.getInt("inputFill");
        outputFill = tag.getInt("outputFill");
        progress = tag.getInt("progress");
        isProgressing = tag.getBoolean("isProgressing");
        hadIdentifier = tag.contains("HadIdentifier")
                ? tag.getBoolean("HadIdentifier")
                : items.getStackInSlot(SLOT_FLUID_ID).is(ModItems.FLUID_IDENTIFIER.get());
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
        fluidOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyOptional = LazyOptional.of(() -> energy);
        itemOptional = LazyOptional.of(() -> automation);
        fluidOptional = LazyOptional.of(() -> fluids);
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
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
