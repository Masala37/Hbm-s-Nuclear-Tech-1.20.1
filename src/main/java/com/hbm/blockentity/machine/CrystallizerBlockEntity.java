package com.hbm.blockentity.machine;

import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.CrystallizerMenu;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
 * 1.7.10 {@code TileEntityMachineCrystallizer}. Speed/effect/overdrive upgrades skipped.
 */
public class CrystallizerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_FLUID_IN = 3;
    public static final int SLOT_FLUID_EMPTY = 4;
    public static final int SLOT_UPGRADE_0 = 5;
    public static final int SLOT_UPGRADE_1 = 6;
    public static final int SLOT_FLUID_ID = 7;
    public static final int SLOT_COUNT = 8;

    public static final int MAX_POWER = 1_000_000;
    public static final int DEMAND = 1_000;
    public static final int ENERGY_TRANSFER = 100_000;
    public static final int TANK_CAPACITY = 8_000;
    public static final int DEFAULT_DURATION = 600;

    private Fluid acidType = null;
    private boolean hadIdentifier;
    private boolean isOn;
    public float angle;
    public float prevAngle;

    private final ModEnergyStorage energy = new ModEnergyStorage(MAX_POWER, MAX_POWER, 0, this::onChanged);
    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty() && stack.getFluid() == getAcidType();
        }
    };
    private final IFluidHandler fluids = new Handler();
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            onChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return CrystallizerBlockEntity.this.mayPlace(slot, stack);
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
    private LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);

    private int progress;
    private int duration = DEFAULT_DURATION;

    public CrystallizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_CRYSTALLIZER.get(), pos, state);
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

    public int getDuration() {
        return Math.max(1, duration);
    }

    public boolean isOn() {
        return isOn;
    }

    public Fluid getAcidType() {
        return acidType != null ? acidType : defaultAcid();
    }

    public FluidStack tankView() {
        if (!tank.getFluid().isEmpty()) {
            return tank.getFluid();
        }
        return new FluidStack(getAcidType(), 0);
    }

    public void setAcidType(Fluid type) {
        if (type == null || !CrystallizerRecipes.isRecipeFluid(type)) {
            return;
        }
        if (getAcidType() == type) {
            return;
        }
        acidType = type;
        tank.setFluid(FluidStack.EMPTY);
        onChanged();
    }

    public boolean mayPlace(int slot, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_INPUT -> CrystallizerRecipes.getOutput(stack, getAcidType()) != null;
            case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
            case SLOT_FLUID_IN -> true;
            case SLOT_FLUID_ID -> stack.is(ModItems.FLUID_IDENTIFIER.get());
            default -> false;
        };
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.crystallizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new CrystallizerMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrystallizerBlockEntity be) {
        if (level.isClientSide) {
            be.clientTick();
            return;
        }
        be.serverTick();
    }

    private void clientTick() {
        prevAngle = angle;
        if (!isOn) {
            return;
        }
        angle += 5.0F;
        if (angle >= 360.0F) {
            angle -= 360.0F;
            prevAngle -= 360.0F;
        }
        if (level != null && level.random.nextInt(20) == 0) {
            double x = worldPosition.getX() + level.random.nextDouble();
            double z = worldPosition.getZ() + level.random.nextDouble();
            level.addParticle(ParticleTypes.CLOUD, x, worldPosition.getY() + 6.5D, z, 0.0D, 0.1D, 0.0D);
        }
    }

    private void serverTick() {
        isOn = false;
        ItemEnergyHelper.chargeFromItem(items.getStackInSlot(SLOT_BATTERY), energy, MAX_POWER);
        pullEnergy();
        applyIdentifier();
        LauncherFluidTransfer.emptyContainer(items, SLOT_FLUID_IN, SLOT_FLUID_EMPTY, tank);
        CrystallizerRecipe recipe = CrystallizerRecipes.getOutput(items.getStackInSlot(SLOT_INPUT), getAcidType());
        duration = recipe != null ? Math.max(1, recipe.duration()) : DEFAULT_DURATION;
        if (canProcess(recipe)) {
            progress++;
            energy.consume(DEMAND);
            isOn = true;
            if (progress > duration) {
                progress = 0;
                processItem(recipe);
            }
        } else {
            progress = 0;
        }
        onChanged();
    }

    private boolean canProcess(CrystallizerRecipe recipe) {
        if (recipe == null || energy.getEnergyStored() < DEMAND) {
            return false;
        }
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (input.getCount() < recipe.itemAmount()) {
            return false;
        }
        if (tank.getFluidAmount() < recipe.acidAmount()) {
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

    private void processItem(CrystallizerRecipe recipe) {
        ItemStack made = recipe.output().resultStack();
        ItemStack out = items.getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, made);
        } else {
            out.grow(made.getCount());
        }
        tank.drain(recipe.acidAmount(), IFluidHandler.FluidAction.EXECUTE);
        items.getStackInSlot(SLOT_INPUT).shrink(recipe.itemAmount());
        if (items.getStackInSlot(SLOT_INPUT).isEmpty()) {
            items.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
        }
    }

    private void applyIdentifier() {
        boolean now = items.getStackInSlot(SLOT_FLUID_ID).is(ModItems.FLUID_IDENTIFIER.get());
        if (now && !hadIdentifier) {
            setAcidType(CrystallizerRecipes.cycleNext(getAcidType()));
        }
        hadIdentifier = now;
        if (!CrystallizerRecipes.isRecipeFluid(getAcidType())) {
            Fluid first = CrystallizerRecipes.cycleNext(null);
            if (first != null) {
                setAcidType(first);
            }
        }
    }

    private void pullEnergy() {
        BlockPos pos = worldPosition;
        EnergyNetworkHelper.pullFrom(level, pos.offset(2, 0, 1), Direction.WEST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(2, 0, -1), Direction.WEST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-2, 0, 1), Direction.EAST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-2, 0, -1), Direction.EAST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(1, 0, 2), Direction.NORTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-1, 0, 2), Direction.NORTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(1, 0, -2), Direction.SOUTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-1, 0, -2), Direction.SOUTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFromNeighbors(level, pos.offset(1, 0, 1), energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFromNeighbors(level, pos.offset(1, 0, -1), energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFromNeighbors(level, pos.offset(-1, 0, 1), energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFromNeighbors(level, pos.offset(-1, 0, -1), energy, ENERGY_TRANSFER);
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
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        ResourceLocation acidId = ForgeRegistries.FLUIDS.getKey(getAcidType());
        if (acidId != null) {
            tag.putString("AcidType", acidId.toString());
        }
        tag.putInt("Progress", progress);
        tag.putInt("Duration", duration);
        tag.putBoolean("IsOn", isOn);
        tag.putBoolean("HadIdentifier", hadIdentifier);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        if (tag.contains("AcidType")) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("AcidType")));
            if (fluid != null) {
                acidType = fluid;
            }
        }
        if (tag.contains("Tank")) {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
        progress = tag.getInt("Progress");
        duration = Math.max(1, tag.contains("Duration") ? tag.getInt("Duration") : DEFAULT_DURATION);
        isOn = tag.getBoolean("IsOn");
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

    private static Fluid defaultAcid() {
        if (ModFluids.PEROXIDE.source.isPresent()) {
            return ModFluids.PEROXIDE.source.get();
        }
        return ModFluids.SULFURIC_ACID.source.get();
    }

    private final class Handler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tankIndex) {
            return tankView();
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tankIndex, @NotNull FluidStack stack) {
            return !stack.isEmpty() && stack.getFluid() == getAcidType();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty() || resource.getFluid() != getAcidType()) {
                return 0;
            }
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
    }

    public float spinAngle(float partialTick) {
        return Mth.lerp(partialTick, prevAngle, angle);
    }
}
