package com.hbm.blockentity.machine;

import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.MixerMenu;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.MixerRecipes.MixerRecipe;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityMachineMixer}. Speed/power/overdrive upgrades skipped.
 */
public class MixerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_FLUID_ID = 2;
    public static final int SLOT_UPGRADE_0 = 3;
    public static final int SLOT_UPGRADE_1 = 4;
    public static final int SLOT_COUNT = 5;

    public static final int MAX_POWER = 10_000;
    public static final int CONSUMPTION = 50;
    public static final int ENERGY_TRANSFER = 10_000;
    public static final int INPUT_CAP = 16_000;
    public static final int OUTPUT_CAP = 24_000;

    private Fluid outputType;
    private boolean hadIdentifier;
    private boolean wasOn;
    public float rotation;
    public float prevRotation;

    private final ModEnergyStorage energy = new ModEnergyStorage(MAX_POWER, MAX_POWER, 0, this::onChanged);
    private final FluidTank input0 = tank(INPUT_CAP, 0);
    private final FluidTank input1 = tank(INPUT_CAP, 1);
    private final FluidTank output = tank(OUTPUT_CAP, 2);
    private final IFluidHandler fluids = new Handler();
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return MixerBlockEntity.this.mayPlace(slot, stack);
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
            if (slot != SLOT_INPUT) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_INPUT) {
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

    private int progress;
    private int processTime = 1;
    private int recipeIndex;

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_MIXER.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public FluidTank getInput0() {
        return input0;
    }

    public FluidTank getInput1() {
        return input1;
    }

    public FluidTank getOutput() {
        return output;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return Math.max(1, processTime);
    }

    public int getRecipeIndex() {
        return recipeIndex;
    }

    public int getRecipeCount() {
        MixerRecipe[] recs = MixerRecipes.getOutput(getOutputType());
        return recs == null ? 0 : recs.length;
    }

    public boolean isOn() {
        return wasOn;
    }

    public Fluid getOutputType() {
        if (outputType != null) {
            return outputType;
        }
        Fluid first = MixerRecipes.firstOutput();
        return first != null ? first : output.getFluid().getFluid();
    }

    public FluidStack tankView(int index) {
        return switch (index) {
            case 0 -> typedView(input0, inputType(0));
            case 1 -> typedView(input1, inputType(1));
            default -> typedView(output, getOutputType());
        };
    }

    public int totalFill() {
        int fill = 0;
        if (inputType(0) != null) {
            fill += input0.getFluidAmount();
        }
        if (inputType(1) != null) {
            fill += input1.getFluidAmount();
        }
        if (getOutputType() != null) {
            fill += output.getFluidAmount();
        }
        return fill;
    }

    public int totalMax() {
        int max = 0;
        if (inputType(0) != null) {
            max += INPUT_CAP;
        }
        if (inputType(1) != null) {
            max += INPUT_CAP;
        }
        if (getOutputType() != null) {
            max += OUTPUT_CAP;
        }
        return max;
    }

    public void setOutputType(Fluid type) {
        if (type == null || !MixerRecipes.isRecipeOutput(type)) {
            return;
        }
        if (getOutputType() == type) {
            return;
        }
        outputType = type;
        recipeIndex = 0;
        output.setFluid(FluidStack.EMPTY);
        applyRecipeTypes(true);
        onChanged();
    }

    public void cycleRecipe() {
        MixerRecipe[] recs = MixerRecipes.getOutput(getOutputType());
        if (recs == null || recs.length == 0) {
            return;
        }
        recipeIndex = (recipeIndex + 1) % recs.length;
        progress = 0;
        applyRecipeTypes(true);
        onChanged();
    }

    public boolean mayPlace(int slot, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
            case SLOT_FLUID_ID -> stack.is(ModItems.FLUID_IDENTIFIER.get());
            case SLOT_INPUT -> {
                MixerRecipe recipe = currentRecipe();
                yield recipe != null && recipe.solid() != null && recipe.solid().matches(stack, true);
            }
            default -> false;
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.machineMixer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new MixerMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MixerBlockEntity be) {
        if (level.isClientSide) {
            be.clientTick();
            return;
        }
        be.serverTick();
    }

    private void clientTick() {
        prevRotation = rotation;
        if (!wasOn) {
            return;
        }
        rotation += 20.0F;
        if (rotation >= 360.0F) {
            rotation -= 360.0F;
            prevRotation -= 360.0F;
        }
    }

    private void serverTick() {
        ItemEnergyHelper.chargeFromItem(items.getStackInSlot(SLOT_BATTERY), energy, MAX_POWER);
        EnergyNetworkHelper.pullFromNeighbors(level, worldPosition, energy, ENERGY_TRANSFER);
        applyIdentifier();
        applyRecipeTypes(false);
        wasOn = canProcess();
        if (wasOn) {
            progress++;
            energy.consume(CONSUMPTION);
            if (progress >= processTime) {
                process();
                progress = 0;
            }
        } else {
            progress = 0;
        }
        onChanged();
    }

    private boolean canProcess() {
        MixerRecipe recipe = currentRecipe();
        if (recipe == null) {
            recipeIndex = 0;
            return false;
        }
        if (recipe.input1() != null && input0.getFluidAmount() < recipe.input1().amount()) {
            return false;
        }
        if (recipe.input2() != null && input1.getFluidAmount() < recipe.input2().amount()) {
            return false;
        }
        if (energy.getEnergyStored() < CONSUMPTION) {
            return false;
        }
        if (recipe.output().amount() + output.getFluidAmount() > OUTPUT_CAP) {
            return false;
        }
        if (recipe.solid() != null && !recipe.solid().matches(items.getStackInSlot(SLOT_INPUT), false)) {
            return false;
        }
        processTime = Math.max(1, recipe.duration());
        return true;
    }

    private void process() {
        MixerRecipe recipe = currentRecipe();
        if (recipe == null) {
            return;
        }
        if (recipe.input1() != null) {
            input0.drain(recipe.input1().amount(), IFluidHandler.FluidAction.EXECUTE);
        }
        if (recipe.input2() != null) {
            input1.drain(recipe.input2().amount(), IFluidHandler.FluidAction.EXECUTE);
        }
        if (recipe.solid() != null) {
            items.getStackInSlot(SLOT_INPUT).shrink(recipe.solid().count());
            if (items.getStackInSlot(SLOT_INPUT).isEmpty()) {
                items.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
            }
        }
        Fluid made = recipe.output().stackFluid();
        if (made != null) {
            output.fill(new FluidStack(made, recipe.output().amount()), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private MixerRecipe currentRecipe() {
        MixerRecipe[] recs = MixerRecipes.getOutput(getOutputType());
        if (recs == null || recs.length == 0) {
            return null;
        }
        recipeIndex = Math.floorMod(recipeIndex, recs.length);
        return recs[recipeIndex];
    }

    private void applyIdentifier() {
        boolean now = items.getStackInSlot(SLOT_FLUID_ID).is(ModItems.FLUID_IDENTIFIER.get());
        if (now && !hadIdentifier) {
            setOutputType(MixerRecipes.cycleNext(getOutputType()));
        }
        hadIdentifier = now;
        if (!MixerRecipes.isRecipeOutput(getOutputType())) {
            Fluid first = MixerRecipes.firstOutput();
            if (first != null) {
                setOutputType(first);
            }
        }
    }

    private void applyRecipeTypes(boolean forceDrain) {
        MixerRecipe recipe = currentRecipe();
        Fluid want0 = recipe != null && recipe.input1() != null ? recipe.input1().stackFluid() : null;
        Fluid want1 = recipe != null && recipe.input2() != null ? recipe.input2().stackFluid() : null;
        retarget(input0, want0, forceDrain);
        retarget(input1, want1, forceDrain);
        Fluid wantOut = getOutputType();
        if (wantOut != null && !output.getFluid().isEmpty() && output.getFluid().getFluid() != wantOut) {
            output.setFluid(FluidStack.EMPTY);
        }
    }

    private static void retarget(FluidTank tank, @Nullable Fluid want, boolean forceDrain) {
        if (want == null) {
            if (forceDrain || !tank.getFluid().isEmpty()) {
                tank.setFluid(FluidStack.EMPTY);
            }
            return;
        }
        if (!tank.getFluid().isEmpty() && tank.getFluid().getFluid() != want) {
            tank.setFluid(FluidStack.EMPTY);
        }
    }

    private @Nullable Fluid inputType(int tank) {
        MixerRecipe recipe = currentRecipe();
        if (recipe == null) {
            return null;
        }
        if (tank == 0) {
            return recipe.input1() != null ? recipe.input1().stackFluid() : null;
        }
        return recipe.input2() != null ? recipe.input2().stackFluid() : null;
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 3, 1));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        tag.put("Input0", input0.writeToNBT(new CompoundTag()));
        tag.put("Input1", input1.writeToNBT(new CompoundTag()));
        tag.put("Output", output.writeToNBT(new CompoundTag()));
        ResourceLocation outId = ForgeRegistries.FLUIDS.getKey(getOutputType());
        if (outId != null) {
            tag.putString("OutputType", outId.toString());
        }
        tag.putInt("Progress", progress);
        tag.putInt("ProcessTime", processTime);
        tag.putInt("RecipeIndex", recipeIndex);
        tag.putBoolean("WasOn", wasOn);
        tag.putBoolean("HadIdentifier", hadIdentifier);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        if (tag.contains("OutputType")) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("OutputType")));
            if (fluid != null) {
                outputType = fluid;
            }
        }
        if (tag.contains("Input0")) {
            input0.readFromNBT(tag.getCompound("Input0"));
        }
        if (tag.contains("Input1")) {
            input1.readFromNBT(tag.getCompound("Input1"));
        }
        if (tag.contains("Output")) {
            output.readFromNBT(tag.getCompound("Output"));
        }
        progress = tag.getInt("Progress");
        processTime = Math.max(1, tag.contains("ProcessTime") ? tag.getInt("ProcessTime") : 1);
        recipeIndex = tag.getInt("RecipeIndex");
        wasOn = tag.getBoolean("WasOn");
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

    public float spinAngle(float partialTick) {
        return Mth.lerp(partialTick, prevRotation, rotation);
    }

    private FluidTank tank(int capacity, int index) {
        return new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                onChanged();
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                if (stack.isEmpty()) {
                    return false;
                }
                if (index == 2) {
                    return stack.getFluid() == getOutputType();
                }
                Fluid want = inputType(index);
                return want != null && stack.getFluid() == want;
            }
        };
    }

    private static FluidStack typedView(FluidTank tank, @Nullable Fluid type) {
        if (!tank.getFluid().isEmpty()) {
            return tank.getFluid();
        }
        if (type != null) {
            return new FluidStack(type, 0);
        }
        return FluidStack.EMPTY;
    }

    private final class Handler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 3;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return tankView(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 2 ? OUTPUT_CAP : INPUT_CAP;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            if (stack.isEmpty() || tank == 2) {
                return false;
            }
            Fluid want = inputType(tank);
            return want != null && stack.getFluid() == want;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return 0;
            }
            Fluid want0 = inputType(0);
            if (want0 != null && resource.getFluid() == want0) {
                return input0.fill(resource, action);
            }
            Fluid want1 = inputType(1);
            if (want1 != null && resource.getFluid() == want1) {
                return input1.fill(resource, action);
            }
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            if (!output.getFluid().isEmpty() && output.getFluid().getFluid() == resource.getFluid()) {
                return output.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return output.drain(maxDrain, action);
        }
    }
}
