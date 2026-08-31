package com.hbm.blockentity.machine;

import api.hbm.item.IDesignatorItem;
import com.hbm.api.bomb.IBomb;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.entity.missile.MissileSystemRules;
import com.hbm.handler.MissileStruct;
import com.hbm.items.machine.InfiniteFluidBarrelItem;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModSounds;
import com.hbm.tileentity.IRadarCommandReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CustomLauncherBlockEntity extends BlockEntity implements MenuProvider, IRadarCommandReceiver {
    public static final int SLOT_MISSILE = 0;
    public static final int SLOT_DESIGNATOR = 1;
    public static final int SLOT_FUEL_IN = 2;
    public static final int SLOT_OX_IN = 3;
    public static final int SLOT_SOLID = 4;
    public static final int SLOT_BATTERY = 5;
    public static final int SLOT_FUEL_OUT = 6;
    public static final int SLOT_OX_OUT = 7;

    public static final int ENERGY_CAPACITY = MissileSystemRules.LAUNCHER_MAX_POWER;
    public static final int ENERGY_TRANSFER = 5_000;
    public static final int LAUNCH_COST = MissileSystemRules.LAUNCHER_LAUNCH_COST;

    protected final int tankCapacity;
    public final int maxSolid;

    private final ItemStackHandler items = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClient();
        }

        @Override
        public int getSlotLimit(int slot) {
            return switch (slot) {
                case SLOT_MISSILE, SLOT_DESIGNATOR, SLOT_BATTERY -> 1;
                default -> 64;
            };
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_MISSILE -> stack.getItem() instanceof ItemCustomMissile;
                case SLOT_DESIGNATOR -> stack.getItem() instanceof IDesignatorItem;
                case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
                case SLOT_SOLID -> isRocketFuel(stack);
                case SLOT_FUEL_IN, SLOT_OX_IN -> FluidUtil.getFluidHandler(stack).isPresent()
                        || stack.getItem() instanceof InfiniteFluidBarrelItem;
                default -> false;
            };
        }
    };

    protected final ModEnergyStorage energy = new ModEnergyStorage(
            ENERGY_CAPACITY, ENERGY_TRANSFER, 0, this::onChanged);

    private Fluid fuelType;
    private Fluid oxidizerType;

    private final FluidTank fuelTank;
    private final FluidTank oxidizerTank;

    private final IFluidHandler combinedFluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? fuelTank.getFluid() : oxidizerTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tankCapacity;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 ? isFuelFluid(stack.getFluid()) : isOxidizerFluid(stack.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            if (isFuelFluid(resource.getFluid())) {
                return fuelTank.fill(resource, action);
            }
            if (isOxidizerFluid(resource.getFluid())) {
                return oxidizerTank.fill(resource, action);
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    };

    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> combinedFluids);

    protected int solid;
    protected boolean wasPowered;

    protected CustomLauncherBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                          int tankCapacity, int maxSolid) {
        super(type, pos, state);
        this.tankCapacity = tankCapacity;
        this.maxSolid = maxSolid;
        this.fuelTank = new FluidTank(tankCapacity) {
            @Override
            protected void onContentsChanged() {
                onChanged();
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                return isFuelFluid(stack.getFluid());
            }
        };
        this.oxidizerTank = new FluidTank(tankCapacity) {
            @Override
            protected void onContentsChanged() {
                onChanged();
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                return isOxidizerFluid(stack.getFluid());
            }
        };
    }

    protected abstract boolean needsDesignatorForCanLaunch();

    protected abstract PartSize requiredTop();

    protected abstract void pullConnections(Level level);

    protected abstract boolean canConnectSide(@Nullable Direction side);

    protected abstract void tryCompleteStructure(Level level);

    protected abstract float smokeSpread();

    protected abstract int redstoneRadius();

    public ItemStackHandler getItems() {
        return items;
    }

    public ModEnergyStorage getEnergy() {
        return energy;
    }

    public FluidTank getFuelTank() {
        return fuelTank;
    }

    public FluidTank getOxidizerTank() {
        return oxidizerTank;
    }

    public int getSolid() {
        return solid;
    }

    public int getTankCapacity() {
        return tankCapacity;
    }

    public MissileStruct getLoad() {
        return ItemCustomMissile.getStruct(items.getStackInSlot(SLOT_MISSILE));
    }

    public static boolean isRocketFuel(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && "hbm".equals(key.getNamespace()) && "rocket_fuel".equals(key.getPath());
    }

    public boolean fillFromInfiniteBarrel() {
        updateTypes();
        boolean fuel = InfiniteFluidBarrelItem.fillTank(fuelTank, fuelType);
        boolean ox = InfiniteFluidBarrelItem.fillTank(oxidizerTank, oxidizerType);
        return fuel || ox;
    }

    public boolean isFuelFluid(Fluid fluid) {
        return fuelType != null && fluid == fuelType;
    }

    public boolean isOxidizerFluid(Fluid fluid) {
        return oxidizerType != null && fluid == oxidizerType;
    }

    public boolean isMissileValid() {
        MissileStruct struct = getLoad();
        if (struct == null || struct.fuselage == null) {
            return false;
        }
        return struct.fuselage.top == requiredTop();
    }

    public boolean hasDesignator() {
        ItemStack stack = items.getStackInSlot(SLOT_DESIGNATOR);
        BlockPos pad = worldPosition;
        return stack.getItem() instanceof IDesignatorItem designator
                && designator.isReady(level, stack, pad.getX(), pad.getY(), pad.getZ());
    }

    public int solidState() {
        return lampFor(true);
    }

    public int liquidState() {
        return lampFor(false);
    }

    public int oxidizerState() {
        MissileStruct struct = getLoad();
        if (struct == null || struct.fuselage == null || struct.fuselage.attributes == null) {
            return -1;
        }
        FuelType fuel = (FuelType) struct.fuselage.attributes[0];
        float need = (Float) struct.fuselage.attributes[1];
        return MissileSystemRules.fuelLamp(
                MissileSystemRules.usesOxidizer(fuel.name()), oxidizerTank.getFluidAmount(), need);
    }

    private int lampFor(boolean solidKind) {
        MissileStruct struct = getLoad();
        if (struct == null || struct.fuselage == null || struct.fuselage.attributes == null) {
            return -1;
        }
        FuelType fuel = (FuelType) struct.fuselage.attributes[0];
        float need = (Float) struct.fuselage.attributes[1];
        if (solidKind) {
            return MissileSystemRules.fuelLamp(MissileSystemRules.usesSolidFuel(fuel.name()), solid, need);
        }
        return MissileSystemRules.fuelLamp(
                MissileSystemRules.usesLiquidFuel(fuel.name()), fuelTank.getFluidAmount(), need);
    }

    public boolean hasFuel() {
        return MissileSystemRules.hasLauncherFuel(solidState(), liquidState(), oxidizerState());
    }

    public boolean canLaunch() {
        boolean power = MissileSystemRules.launcherPowerReady(energy.getEnergyStored(), ENERGY_CAPACITY);
        boolean missile = isMissileValid();
        boolean fuel = hasFuel();
        if (needsDesignatorForCanLaunch()) {
            return MissileSystemRules.compactCanLaunch(power, missile, hasDesignator(), fuel);
        }
        return MissileSystemRules.tableCanLaunch(power, missile, fuel);
    }

    @Override
    public boolean sendCommandPosition(int x, int y, int z) {
        if (!canLaunch()) {
            return false;
        }
        launchTo(x, z);
        return true;
    }

    @Override
    public boolean sendCommandEntity(Entity target) {
        return sendCommandPosition((int) Math.floor(target.getX()), (int) Math.floor(target.getY()),
                (int) Math.floor(target.getZ()));
    }

    public IBomb.BombReturnCode launchFromDesignator() {
        if (level == null || level.isClientSide) {
            return IBomb.BombReturnCode.UNDEFINED;
        }
        ItemStack designator = items.getStackInSlot(SLOT_DESIGNATOR);
        BlockPos pad = worldPosition;
        if (!(designator.getItem() instanceof IDesignatorItem designatorItem)
                || !designatorItem.isReady(level, designator, pad.getX(), pad.getY(), pad.getZ())) {
            return IBomb.BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        Vec3 target = designatorItem.getCoords(level, designator, pad.getX(), pad.getY(), pad.getZ());
        launchTo((int) Math.floor(target.x), (int) Math.floor(target.z));
        return IBomb.BombReturnCode.LAUNCHED;
    }

    public void launchTo(int targetX, int targetZ) {
        if (level == null || level.isClientSide) {
            return;
        }
        ItemStack missileStack = items.getStackInSlot(SLOT_MISSILE);
        MissileStruct struct = ItemCustomMissile.getStruct(missileStack);
        if (struct == null || struct.fuselage == null) {
            return;
        }

        BlockPos pos = worldPosition;
        level.playSound(null, pos, ModSounds.MISSILE_TAKEOFF.get(), SoundSource.BLOCKS, 10.0F, 1.0F);

        int[] aim = ItemCustomMissile.applyInaccuracy(
                missileStack, pos.getX(), pos.getZ(), targetX, targetZ, level.random);
        EntityMissileCustom missile = new EntityMissileCustom(
                level,
                pos.getX() + 0.5D,
                pos.getY() + 2.5D,
                pos.getZ() + 0.5D,
                aim[0], pos.getY(), aim[1],
                struct);
        level.addFreshEntity(missile);

        subtractFuel();
        items.setStackInSlot(SLOT_MISSILE, ItemStack.EMPTY);
        setChanged();
        syncToClient();
    }

    private void subtractFuel() {
        MissileStruct struct = getLoad();
        if (struct == null || struct.fuselage == null || struct.fuselage.attributes == null) {
            return;
        }
        FuelType fuel = (FuelType) struct.fuselage.attributes[0];
        int amount = (int) (float) struct.fuselage.attributes[1];
        switch (fuel) {
            case KEROSENE, HYDROGEN, BALEFIRE -> {
                fuelTank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                oxidizerTank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
            }
            case XENON -> fuelTank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
            case SOLID -> solid = Math.max(0, solid - amount);
            default -> {
            }
        }
        energy.consume(LAUNCH_COST);
    }

    public void updateTypes() {
        MissileStruct struct = getLoad();
        if (struct == null || struct.fuselage == null || struct.fuselage.attributes == null) {
            return;
        }
        FuelType fuel = (FuelType) struct.fuselage.attributes[0];
        switch (fuel) {
            case KEROSENE -> {
                setFuelType(ModFluids.KEROSENE.source.get());
                setOxidizerType(ModFluids.PEROXIDE.source.get());
            }
            case HYDROGEN -> {
                setFuelType(ModFluids.HYDROGEN.source.get());
                setOxidizerType(ModFluids.OXYGEN.source.get());
            }
            case XENON -> setFuelType(ModFluids.XENON.source.get());
            case BALEFIRE -> {
                setFuelType(ModFluids.BALEFIRE.source.get());
                setOxidizerType(ModFluids.PEROXIDE.source.get());
            }
            default -> {
            }
        }
    }

    private void setFuelType(Fluid fluid) {
        if (fuelType != fluid) {
            fuelType = fluid;
            if (!fuelTank.getFluid().isEmpty() && fuelTank.getFluid().getFluid() != fluid) {
                fuelTank.setFluid(FluidStack.EMPTY);
            }
        }
    }

    private void setOxidizerType(Fluid fluid) {
        if (oxidizerType != fluid) {
            oxidizerType = fluid;
            if (!oxidizerTank.getFluid().isEmpty() && oxidizerTank.getFluid().getFluid() != fluid) {
                oxidizerTank.setFluid(FluidStack.EMPTY);
            }
        }
    }

    public void checkRedstone(boolean powered) {
        if (powered && canLaunch()) {
            launchFromDesignator();
        }
        wasPowered = powered;
    }

    protected void pollRedstone(Level level) {
        int radius = redstoneRadius();
        BlockPos pos = worldPosition;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (level.hasNeighborSignal(pos.offset(dx, 0, dz))) {
                    if (canLaunch()) {
                        launchFromDesignator();
                    }
                    return;
                }
            }
        }
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            for (int i = 0; i < items.getSlots(); i++) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        items.getStackInSlot(i));
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    protected void onChanged() {
        setChanged();
        syncToClient();
    }

    protected void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    protected void processFluidSlots() {
        transferFluidItem(SLOT_FUEL_IN, SLOT_FUEL_OUT, fuelTank, fuelType);
        transferFluidItem(SLOT_OX_IN, SLOT_OX_OUT, oxidizerTank, oxidizerType);
    }

    private void transferFluidItem(int inSlot, int outSlot, FluidTank tank, Fluid accepted) {
        ItemStack in = items.getStackInSlot(inSlot);
        if (in.isEmpty()) {
            return;
        }
        if (in.getItem() instanceof InfiniteFluidBarrelItem) {
            InfiniteFluidBarrelItem.fillTank(tank, accepted);
            return;
        }
        ItemStack single = in.copyWithCount(1);
        var result = FluidUtil.tryEmptyContainer(single, tank, 1000, null, true);
        if (!result.isSuccess()) {
            return;
        }
        ItemStack remainder = result.getResult();
        in.shrink(1);
        items.setStackInSlot(inSlot, in.isEmpty() ? ItemStack.EMPTY : in);
        if (!remainder.isEmpty()) {
            ItemStack out = items.getStackInSlot(outSlot);
            if (out.isEmpty()) {
                items.setStackInSlot(outSlot, remainder);
            } else if (ItemStack.isSameItemSameTags(out, remainder) && out.getCount() < out.getMaxStackSize()) {
                out.grow(1);
                items.setStackInSlot(outSlot, out);
            } else if (level != null && !level.isClientSide) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1.0D,
                        worldPosition.getZ(), remainder);
            }
        }
    }

    private void ingestSolidFuel() {
        ItemStack stack = items.getStackInSlot(SLOT_SOLID);
        if (!isRocketFuel(stack) || solid + MissileSystemRules.ROCKET_FUEL_PER_ITEM > maxSolid) {
            return;
        }
        stack.shrink(1);
        items.setStackInSlot(SLOT_SOLID, stack.isEmpty() ? ItemStack.EMPTY : stack);
        solid += MissileSystemRules.ROCKET_FUEL_PER_ITEM;
        onChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CustomLauncherBlockEntity be) {
        be.tryCompleteStructure(level);
        be.updateTypes();
        be.pullConnections(level);
        ItemEnergyHelper.chargeFromItem(be.items.getStackInSlot(SLOT_BATTERY), be.energy, ENERGY_TRANSFER);
        be.processFluidSlots();
        be.ingestSolidFuel();
        be.pollRedstone(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CustomLauncherBlockEntity be) {
        if (!level.isClientSide) {
            return;
        }
        com.hbm.HbmNuclearTechMod.proxy.tickCustomLauncherSmoke(level, pos, be.smokeSpread());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        energy.write(tag);
        tag.put("FuelTank", fuelTank.writeToNBT(new CompoundTag()));
        tag.put("OxTank", oxidizerTank.writeToNBT(new CompoundTag()));
        tag.putInt("solidfuel", solid);
        tag.putBoolean("wasPowered", wasPowered);
        if (fuelType != null) {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fuelType);
            if (id != null) {
                tag.putString("FuelType", id.toString());
            }
        }
        if (oxidizerType != null) {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(oxidizerType);
            if (id != null) {
                tag.putString("OxType", id.toString());
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        energy.read(tag);
        if (tag.contains("FuelType")) {
            fuelType = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("FuelType")));
        }
        if (tag.contains("OxType")) {
            oxidizerType = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("OxType")));
        }
        if (tag.contains("FuelTank")) {
            fuelTank.readFromNBT(tag.getCompound("FuelTank"));
        }
        if (tag.contains("OxTank")) {
            oxidizerTank.readFromNBT(tag.getCompound("OxTank"));
        }
        solid = tag.getInt("solidfuel");
        wasPowered = tag.getBoolean("wasPowered");
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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!canConnectSide(side)) {
            return super.getCapability(cap, side);
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        fluidOptional.invalidate();
    }

    @Override
    public abstract @NotNull Component getDisplayName();

    @Nullable
    @Override
    public abstract AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player);
}
