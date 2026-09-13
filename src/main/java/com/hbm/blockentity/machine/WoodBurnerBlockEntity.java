package com.hbm.blockentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.fluid.FlammableHeatEnergy;
import com.hbm.inventory.menu.WoodBurnerMenu;
import com.hbm.inventory.recipes.WoodBurnerBurnTime;
import com.hbm.items.machine.InfiniteFluidBarrelItem;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
 * 1.7.10 {@code TileEntityMachineWoodBurner}. Ash and pollution skipped (no powder_ash / pollution).
 */
public class WoodBurnerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_ASH = 1;
    public static final int SLOT_FLUID_ID = 2;
    public static final int SLOT_FLUID_IN = 3;
    public static final int SLOT_FLUID_OUT = 4;
    public static final int SLOT_BATTERY = 5;
    public static final int SLOT_COUNT = 6;

    public static final int ENERGY_CAPACITY = 100_000;
    public static final int SOLID_FE = 100;
    public static final int TANK_CAPACITY = 16_000;
    public static final int LIQUID_BURN_MB = 2;

    private final ModEnergyStorage energy = new ModEnergyStorage(ENERGY_CAPACITY, 0, ENERGY_CAPACITY, this::onChanged);
    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return FlammableHeatEnergy.perBucket(stack.getFluid()) > 0L;
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
                case SLOT_FUEL -> WoodBurnerBurnTime.burnTime(stack) > 0;
                case SLOT_FLUID_ID -> stack.is(ModItems.FLUID_IDENTIFIER.get());
                case SLOT_FLUID_IN -> true;
                case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
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
            if (slot != SLOT_FUEL || !items.isItemValid(slot, stack)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_ASH) {
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

    private int burnTime;
    private int maxBurnTime;
    private boolean liquidBurn;
    private boolean isOn;
    private int powerGen;

    public WoodBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_WOOD_BURNER.get(), pos, state);
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

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public boolean isLiquidBurn() {
        return liquidBurn;
    }

    public boolean isOn() {
        return isOn;
    }

    public int getPowerGen() {
        return powerGen;
    }

    public void toggleOn() {
        isOn = !isOn;
        onChanged();
    }

    public void toggleLiquidBurn() {
        liquidBurn = !liquidBurn;
        onChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.machineWoodBurner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new WoodBurnerMenu(id, inv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WoodBurnerBlockEntity be) {
        if (level.isClientSide) {
            be.clientTick(level, pos, state);
        } else {
            be.serverTick(level, pos, state);
        }
    }

    private void clientTick(Level level, BlockPos pos, BlockState state) {
        if (powerGen <= 0) {
            return;
        }
        int facing = DummyableMeta.coreFacing(state.getValue(BlockDummyable.META));
        int rot = DummyableMeta.rotateYClockwise(facing);
        double x = pos.getX() + 0.5 - DummyableMeta.offsetX(facing) + DummyableMeta.offsetX(rot);
        double y = pos.getY() + 4;
        double z = pos.getZ() + 0.5 - DummyableMeta.offsetZ(facing) + DummyableMeta.offsetZ(rot);
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.05, 0.0);
    }

    private void serverTick(Level level, BlockPos pos, BlockState state) {
        powerGen = 0;
        if (items.getStackInSlot(SLOT_FLUID_IN).getItem() instanceof InfiniteFluidBarrelItem) {
            InfiniteFluidBarrelItem.fillTank(tank, ModFluids.WOODOIL.source.get());
        } else {
            LauncherFluidTransfer.emptyContainer(items, SLOT_FLUID_IN, SLOT_FLUID_OUT, tank);
        }
        ItemEnergyHelper.chargeItemFromBuffer(items.getStackInSlot(SLOT_BATTERY), energy);

        if (!liquidBurn) {
            if (burnTime <= 0) {
                ItemStack fuel = items.getStackInSlot(SLOT_FUEL);
                int burn = WoodBurnerBurnTime.burnTime(fuel);
                if (burn > 0) {
                    maxBurnTime = burnTime = burn;
                    ItemStack extracted = items.extractItem(SLOT_FUEL, 1, false);
                    if (items.getStackInSlot(SLOT_FUEL).isEmpty() && extracted.hasCraftingRemainingItem()) {
                        items.setStackInSlot(SLOT_FUEL, extracted.getCraftingRemainingItem());
                    }
                }
            } else if (energy.getEnergyStored() < ENERGY_CAPACITY && isOn) {
                burnTime--;
                powerGen += SOLID_FE;
            }
        } else if (energy.getEnergyStored() < ENERGY_CAPACITY && tank.getFluidAmount() > 0 && isOn) {
            FluidStack fluid = tank.getFluid();
            int toBurn = Math.min(fluid.getAmount(), LIQUID_BURN_MB);
            int fe = FlammableHeatEnergy.burnFe(fluid.getFluid(), toBurn);
            if (fe > 0 && toBurn > 0) {
                tank.drain(toBurn, IFluidHandler.FluidAction.EXECUTE);
                powerGen += fe;
            }
        }

        if (powerGen > 0) {
            int room = ENERGY_CAPACITY - energy.getEnergyStored();
            energy.setEnergy(energy.getEnergyStored() + Math.min(powerGen, room));
        }

        int facing = DummyableMeta.coreFacing(state.getValue(BlockDummyable.META));
        int rot = DummyableMeta.rotateYClockwise(facing);
        BlockPos extraA = pos.offset(-DummyableMeta.offsetX(facing), 0, -DummyableMeta.offsetZ(facing));
        BlockPos extraB = extraA.offset(DummyableMeta.offsetX(rot), 0, DummyableMeta.offsetZ(rot));
        EnergyNetworkHelper.pushToNeighbors(level, extraA, energy, ENERGY_CAPACITY);
        EnergyNetworkHelper.pushToNeighbors(level, extraB, energy, ENERGY_CAPACITY);

        onChanged();
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 6, 2));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        energy.write(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.put("Items", items.serializeNBT());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("MaxBurnTime", maxBurnTime);
        tag.putBoolean("IsOn", isOn);
        tag.putBoolean("LiquidBurn", liquidBurn);
        tag.putInt("PowerGen", powerGen);
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
        burnTime = tag.getInt("BurnTime");
        maxBurnTime = tag.getInt("MaxBurnTime");
        isOn = tag.getBoolean("IsOn");
        liquidBurn = tag.getBoolean("LiquidBurn");
        powerGen = tag.getInt("PowerGen");
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
