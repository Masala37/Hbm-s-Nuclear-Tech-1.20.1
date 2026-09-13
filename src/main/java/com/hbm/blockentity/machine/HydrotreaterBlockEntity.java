package com.hbm.blockentity.machine;

import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.inventory.menu.HydrotreaterMenu;
import com.hbm.inventory.recipes.HydrotreatingRecipes;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModItems;
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
 * 1.7.10 {@code TileEntityMachineHydrotreater}. OpenComputers and hydrogen pressurization skipped.
 */
public class HydrotreaterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_IN = 1;
    public static final int SLOT_IN_EMPTY = 2;
    public static final int SLOT_H2_IN = 3;
    public static final int SLOT_H2_EMPTY = 4;
    public static final int SLOT_OUT1_IN = 5;
    public static final int SLOT_OUT1_EMPTY = 6;
    public static final int SLOT_OUT2_IN = 7;
    public static final int SLOT_OUT2_EMPTY = 8;
    public static final int SLOT_FLUID_ID = 9;
    public static final int SLOT_CATALYST = 10;
    public static final int SLOT_COUNT = 11;

    public static final int ENERGY_CAPACITY = 1_000_000;
    public static final int ENERGY_TRANSFER = 100_000;
    public static final int OIL_CAP = 64_000;
    public static final int H2_CAP = 64_000;
    public static final int OUTPUT_CAP = 24_000;

    private Fluid oilType = null;
    private boolean hadIdentifier;
    private final ModEnergyStorage energy = new ModEnergyStorage(ENERGY_CAPACITY, ENERGY_CAPACITY, 0, this::onChanged);
    private final FluidTank oil = tank(OIL_CAP, true, false);
    private final FluidTank hydrogen = tank(H2_CAP, false, true);
    private final FluidTank output1 = tank(OUTPUT_CAP, false, false);
    private final FluidTank output2 = tank(OUTPUT_CAP, false, false);
    private final IFluidHandler fluids = new Handler();
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
                case SLOT_CATALYST -> isCatalyticConverter(stack);
                case SLOT_IN, SLOT_OUT1_IN, SLOT_OUT2_IN -> true;
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
            if (slot == SLOT_IN_EMPTY || slot == SLOT_H2_IN || slot == SLOT_H2_EMPTY
                    || slot == SLOT_OUT1_EMPTY || slot == SLOT_OUT2_EMPTY) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_IN_EMPTY && slot != SLOT_OUT1_EMPTY && slot != SLOT_OUT2_EMPTY) {
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

    public HydrotreaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_HYDROTREATER.get(), pos, state);
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

    public FluidTank getHydrogen() {
        return hydrogen;
    }

    public FluidTank getOutput1() {
        return output1;
    }

    public FluidTank getOutput2() {
        return output2;
    }

    public Fluid getOilType() {
        return oilType != null ? oilType : defaultOil();
    }

    public void setOilType(Fluid type) {
        if (type == null || HydrotreatingRecipes.get(type) == null) {
            return;
        }
        if (getOilType() == type) {
            return;
        }
        int amount = oil.getFluidAmount();
        oilType = type;
        oil.setFluid(amount > 0 ? new FluidStack(type, amount) : FluidStack.EMPTY);
        output1.setFluid(FluidStack.EMPTY);
        output2.setFluid(FluidStack.EMPTY);
        onChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.hydrotreater");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new HydrotreaterMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HydrotreaterBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        ItemEnergyHelper.dischargeItemIntoBuffer(be.items.getStackInSlot(SLOT_BATTERY), be.energy);
        be.pullEnergy(level, pos);
        be.applyIdentifier();
        LauncherFluidTransfer.emptyContainer(be.items, SLOT_IN, SLOT_IN_EMPTY, be.oil);
        LauncherFluidTransfer.fillContainer(be.items, SLOT_OUT1_IN, SLOT_OUT1_EMPTY, be.output1);
        LauncherFluidTransfer.fillContainer(be.items, SLOT_OUT2_IN, SLOT_OUT2_EMPTY, be.output2);
        if (level.getGameTime() % HydrotreatingRecipes.TICK_DELAY == 0) {
            be.reform();
        }
        be.onChanged();
    }

    private void applyIdentifier() {
        boolean now = items.getStackInSlot(SLOT_FLUID_ID).is(ModItems.FLUID_IDENTIFIER.get());
        if (now && !hadIdentifier) {
            setOilType(HydrotreatingRecipes.cycleNext(getOilType()));
        }
        hadIdentifier = now;
        if (HydrotreatingRecipes.get(getOilType()) == null) {
            Fluid first = HydrotreatingRecipes.cycleNext(null);
            if (first != null) {
                setOilType(first);
            }
        }
    }

    private void pullEnergy(Level level, BlockPos pos) {
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

    private void reform() {
        HydrotreatingRecipes.Outputs recipe = HydrotreatingRecipes.get(getOilType());
        if (recipe == null) {
            return;
        }
        if (energy.getEnergyStored() < HydrotreatingRecipes.POWER_PER_OP) {
            return;
        }
        if (oil.getFluidAmount() < HydrotreatingRecipes.INPUT_MB) {
            return;
        }
        if (hydrogen.getFluidAmount() < recipe.hydrogenMb() || !isHydrogen(hydrogen.getFluid().getFluid())) {
            return;
        }
        if (!isCatalyticConverter(items.getStackInSlot(SLOT_CATALYST))) {
            return;
        }
        if (output1.getFluidAmount() + recipe.leftMb() > output1.getCapacity()
                || output2.getFluidAmount() + recipe.rightMb() > output2.getCapacity()) {
            return;
        }
        oil.drain(HydrotreatingRecipes.INPUT_MB, IFluidHandler.FluidAction.EXECUTE);
        hydrogen.drain(recipe.hydrogenMb(), IFluidHandler.FluidAction.EXECUTE);
        output1.fill(new FluidStack(recipe.left(), recipe.leftMb()), IFluidHandler.FluidAction.EXECUTE);
        output2.fill(new FluidStack(recipe.right(), recipe.rightMb()), IFluidHandler.FluidAction.EXECUTE);
        energy.consume(HydrotreatingRecipes.POWER_PER_OP);
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
        energy.write(tag);
        tag.put("Items", items.serializeNBT());
        ResourceLocation key = ForgeRegistries.FLUIDS.getKey(getOilType());
        if (key != null) {
            tag.putString("OilType", key.toString());
        }
        tag.put("Oil", oil.writeToNBT(new CompoundTag()));
        tag.put("Hydrogen", hydrogen.writeToNBT(new CompoundTag()));
        tag.put("Output1", output1.writeToNBT(new CompoundTag()));
        tag.put("Output2", output2.writeToNBT(new CompoundTag()));
        tag.putBoolean("HadIdentifier", hadIdentifier);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.read(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        if (tag.contains("OilType")) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("OilType")));
            if (fluid != null) {
                oilType = fluid;
            }
        }
        if (tag.contains("Oil")) {
            oil.readFromNBT(tag.getCompound("Oil"));
        }
        if (tag.contains("Hydrogen")) {
            hydrogen.readFromNBT(tag.getCompound("Hydrogen"));
        }
        if (tag.contains("Output1")) {
            output1.readFromNBT(tag.getCompound("Output1"));
        }
        if (tag.contains("Output2")) {
            output2.readFromNBT(tag.getCompound("Output2"));
        }
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

    private FluidTank tank(int capacity, boolean oilTank, boolean hydrogenTank) {
        return new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                HydrotreaterBlockEntity.this.onChanged();
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                if (stack.isEmpty()) {
                    return false;
                }
                if (oilTank) {
                    return stack.getFluid() == getOilType();
                }
                if (hydrogenTank) {
                    return isHydrogen(stack.getFluid());
                }
                return true;
            }
        };
    }

    public static boolean isCatalyticConverter(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && RefStrings.MODID.equals(key.getNamespace())
                && "catalytic_converter".equals(key.getPath());
    }

    private static boolean isHydrogen(Fluid fluid) {
        return ModFluids.HYDROGEN.source.isPresent() && fluid == ModFluids.HYDROGEN.source.get();
    }

    private static Fluid defaultOil() {
        if (ModFluids.OIL.source.isPresent() && HydrotreatingRecipes.get(ModFluids.OIL.source.get()) != null) {
            return ModFluids.OIL.source.get();
        }
        return ModFluids.GAS.source.get();
    }

    private final class Handler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 4;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            HydrotreatingRecipes.Outputs recipe = HydrotreatingRecipes.get(getOilType());
            return switch (tank) {
                case 0 -> typedView(oil, getOilType());
                case 1 -> typedView(hydrogen, ModFluids.HYDROGEN.source.get());
                case 2 -> typedView(output1, recipe != null ? recipe.left() : null);
                case 3 -> typedView(output2, recipe != null ? recipe.right() : null);
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return switch (tank) {
                case 0 -> OIL_CAP;
                case 1 -> H2_CAP;
                case 2, 3 -> OUTPUT_CAP;
                default -> 0;
            };
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (tank == 0) {
                return stack.getFluid() == getOilType();
            }
            return tank == 1 && isHydrogen(stack.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return 0;
            }
            if (isHydrogen(resource.getFluid())) {
                return hydrogen.fill(resource, action);
            }
            if (resource.getFluid() == getOilType()) {
                return oil.fill(resource, action);
            }
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            if (!output1.getFluid().isEmpty() && output1.getFluid().getFluid() == resource.getFluid()) {
                return output1.drain(resource, action);
            }
            if (!output2.getFluid().isEmpty() && output2.getFluid().getFluid() == resource.getFluid()) {
                return output2.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!output1.getFluid().isEmpty()) {
                return output1.drain(maxDrain, action);
            }
            return output2.drain(maxDrain, action);
        }
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
}
