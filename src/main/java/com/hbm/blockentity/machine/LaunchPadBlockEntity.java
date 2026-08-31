package com.hbm.blockentity.machine;

import api.hbm.item.IDesignatorItem;
import com.hbm.api.bomb.IBomb;
import com.hbm.blocks.machine.LaunchPadBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.entity.missile.EntityMissileAntiBallistic;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.MissileLaunchRegistry;
import com.hbm.entity.missile.MissileSystemRules;
import com.hbm.inventory.menu.LaunchPadMenu;
import com.hbm.items.weapon.MissileItem;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModSounds;
import com.hbm.tileentity.IRadarCommandReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Silo launch pad — legacy TileEntityLaunchPadBase parity (1×1 block, 3×3 silo mesh).
 */
public class LaunchPadBlockEntity extends BlockEntity implements MenuProvider, IRadarCommandReceiver {
    public static final int SLOT_MISSILE = 0;
    public static final int SLOT_DESIGNATOR = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_FUEL_IN = 3;
    public static final int SLOT_FUEL_OUT = 4;
    public static final int SLOT_OX_IN = 5;
    public static final int SLOT_OX_OUT = 6;

    public static final int ENERGY_CAPACITY = 100_000;
    public static final int ENERGY_TRANSFER = 5_000;
    public static final int LAUNCH_COST = MissileSystemRules.PAD_LAUNCH_COST;
    public static final int TANK_CAPACITY = MissileSystemRules.PAD_TANK;
    public static final int TIER1_FUEL_COST = 4_000;
    public static final int COOLDOWN_TICKS = MissileSystemRules.PAD_COOLDOWN;

    public static final int STATE_MISSING = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_READY = 2;

    private final ItemStackHandler items = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == SLOT_DESIGNATOR) {
                syncTargetFromDesignator();
            }
            setChanged();
            syncToClient();
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_MISSILE || slot == SLOT_DESIGNATOR || slot == SLOT_BATTERY ? 1 : 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_MISSILE -> MissileLaunchRegistry.isLaunchable(stack);
                case SLOT_DESIGNATOR -> stack.getItem() instanceof IDesignatorItem;
                case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
                case SLOT_FUEL_IN, SLOT_OX_IN -> FluidUtil.getFluidHandler(stack).isPresent()
                        || stack.getItem() instanceof com.hbm.items.machine.InfiniteFluidBarrelItem;
                default -> false;
            };
        }
    };

    private final ModEnergyStorage energy = new ModEnergyStorage(
            ENERGY_CAPACITY, ENERGY_TRANSFER, 0, this::onChanged);

    private final FluidTank fuelTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return isFuelFluid(stack.getFluid());
        }
    };

    private final FluidTank oxidizerTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return isOxidizerFluid(stack.getFluid());
        }
    };

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
            return TANK_CAPACITY;
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

    private final IItemHandler hopperItems = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(SLOT_MISSILE);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return items.insertItem(SLOT_MISSILE, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return items.isItemValid(SLOT_MISSILE, stack);
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> hopperItems);
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> combinedFluids);

    private boolean hasTarget;
    private int targetX;
    private int targetY;
    private int targetZ;
    protected boolean wasPowered;
    protected int delay = COOLDOWN_TICKS;
    protected int state = STATE_MISSING;
    private Direction facing = Direction.NORTH;

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.LAUNCH_PAD.get(), pos, state);
    }

    public LaunchPadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Silo mesh is 3×3 (OBJ ±1.5) and standing missiles are tall; 1.7.10 AABB
     * is x-2..+3, y+15.
     */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 2.0D, worldPosition.getY(), worldPosition.getZ() - 2.0D,
                worldPosition.getX() + 3.0D, worldPosition.getY() + 15.0D, worldPosition.getZ() + 3.0D);
    }

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

    public Direction getFacing() {
        return facing == null ? Direction.NORTH : facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        setChanged();
        syncToClient();
    }

    public double getLaunchOffset() {
        return 1.0D;
    }

    public boolean isReadyForLaunch() {
        return delay <= 0;
    }

    public int getState() {
        return state;
    }

    public int getDelay() {
        return delay;
    }

    public boolean hasTarget() {
        syncTargetFromDesignator();
        return hasTarget;
    }

    public BlockPos getTarget() {
        syncTargetFromDesignator();
        return new BlockPos(targetX, targetY, targetZ);
    }

    public void setTarget(BlockPos pos) {
        this.hasTarget = true;
        this.targetX = pos.getX();
        this.targetY = pos.getY();
        this.targetZ = pos.getZ();
        setChanged();
    }

    protected void syncTargetFromDesignator() {
        ItemStack stack = items.getStackInSlot(SLOT_DESIGNATOR);
        BlockPos pad = worldPosition;
        if (stack.getItem() instanceof IDesignatorItem designator
                && designator.isReady(level, stack, pad.getX(), pad.getY(), pad.getZ())) {
            Vec3 coords = designator.getCoords(level, stack, pad.getX(), pad.getY(), pad.getZ());
            hasTarget = true;
            targetX = (int) Math.floor(coords.x);
            targetY = (int) Math.floor(coords.y);
            targetZ = (int) Math.floor(coords.z);
        } else {
            hasTarget = false;
        }
    }

    public boolean isFuelFluid(Fluid fluid) {
        Fluid expected = expectedFuelFluid();
        return expected != null && fluid == expected;
    }

    public boolean isOxidizerFluid(Fluid fluid) {
        Fluid expected = expectedOxidizerFluid();
        return expected != null && fluid == expected;
    }

    /**
     * Legacy {@code TileEntityLaunchPadBase.setFuel} — tank types follow the loaded missile.
     * Empty / solid-fuel slots accept nothing (1.7 tanks stay {@code Fluids.NONE}).
     */
    @Nullable
    private Fluid expectedFuelFluid() {
        ItemStack missile = items.getStackInSlot(SLOT_MISSILE);
        if (!(missile.getItem() instanceof MissileItem mi) || !mi.requiresFluidFuel()) {
            return null;
        }
        return switch (mi.getTier()) {
            case TIER1 -> ModFluids.ETHANOL.source.get();
            case TIER2, STEALTH, ROBIN, TIER3 -> ModFluids.KEROSENE.source.get();
            case TIER4 -> ModFluids.KEROSENE_REFORM.source.get();
            default -> null;
        };
    }

    @Nullable
    private Fluid expectedOxidizerFluid() {
        ItemStack missile = items.getStackInSlot(SLOT_MISSILE);
        if (!(missile.getItem() instanceof MissileItem mi) || !mi.requiresFluidFuel()) {
            return null;
        }
        return switch (mi.getTier()) {
            case TIER1, TIER2, STEALTH, ROBIN -> ModFluids.PEROXIDE.source.get();
            case TIER3, TIER4 -> ModFluids.OXYGEN.source.get();
            default -> null;
        };
    }

    public boolean isMissileValid() {
        return MissileLaunchRegistry.isLaunchable(items.getStackInSlot(SLOT_MISSILE));
    }

    /**
     * Legacy {@code ItemMissile.fuelCap}: MICRO/Tier0 / ABM solid = 0 (power only);
     * V2 4000 ethanol; Strong/Stealth/Robin 8000 kerosene; Huge 12000 kerosene;
     * Atlas 16000 jet fuel.
     */
    public int getRequiredFuelAmount() {
        ItemStack missile = items.getStackInSlot(SLOT_MISSILE);
        if (missile.getItem() instanceof MissileItem mi) {
            return mi.getFuelCap();
        }
        return isMissileValid() ? TIER1_FUEL_COST : 0;
    }

    public boolean hasTankFuel() {
        int cost = getRequiredFuelAmount();
        if (cost <= 0) {
            return true;
        }
        return fuelTank.getFluidAmount() >= cost
                && oxidizerTank.getFluidAmount() >= cost
                && isFuelFluid(fuelTank.getFluid().getFluid())
                && isOxidizerFluid(oxidizerTank.getFluid().getFluid());
    }

    public boolean hasFuel() {
        return energy.getEnergyStored() >= LAUNCH_COST && hasTankFuel();
    }

    public boolean tryInsertMissile(ItemStack stack) {
        if (!MissileLaunchRegistry.isLaunchable(stack) || !items.getStackInSlot(SLOT_MISSILE).isEmpty()) {
            return false;
        }
        items.setStackInSlot(SLOT_MISSILE, stack.split(1));
        return true;
    }

    public boolean tryInsertDesignator(ItemStack stack) {
        if (!(stack.getItem() instanceof IDesignatorItem) || !items.getStackInSlot(SLOT_DESIGNATOR).isEmpty()) {
            return false;
        }
        items.setStackInSlot(SLOT_DESIGNATOR, stack.split(1));
        return true;
    }

    public boolean trySetTargetFromDesignator(ItemStack stack) {
        BlockPos pad = worldPosition;
        if (!(stack.getItem() instanceof IDesignatorItem designator)
                || !designator.isReady(level, stack, pad.getX(), pad.getY(), pad.getZ())) {
            return false;
        }
        Vec3 coords = designator.getCoords(level, stack, pad.getX(), pad.getY(), pad.getZ());
        setTarget(new BlockPos((int) Math.floor(coords.x), (int) Math.floor(coords.y), (int) Math.floor(coords.z)));
        return true;
    }

    public boolean needsDesignator() {
        return !MissileLaunchRegistry.isAntiBallistic(items.getStackInSlot(SLOT_MISSILE));
    }

    public boolean canLaunch() {
        syncTargetFromDesignator();
        return radarCanLaunch()
                && (!needsDesignator() || hasTarget);
    }

    public boolean radarCanLaunch() {
        return isMissileValid() && hasFuel() && isReadyForLaunch();
    }

    @Override
    public boolean sendCommandPosition(int x, int y, int z) {
        return launchTo(x, z, null);
    }

    @Override
    public boolean sendCommandEntity(Entity target) {
        return launchTo((int) Math.floor(target.getX()), (int) Math.floor(target.getZ()), target);
    }

    /**
     * Legacy {@code TileEntityLaunchPadBase.launchFromDesignator} — detonator / redstone entry.
     */
    public IBomb.BombReturnCode launchFromDesignator() {
        if (level == null || level.isClientSide) {
            return IBomb.BombReturnCode.UNDEFINED;
        }
        syncTargetFromDesignator();
        if (!canLaunch()) {
            return IBomb.BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        return spawnLaunch(targetX, targetZ, null) ? IBomb.BombReturnCode.LAUNCHED : IBomb.BombReturnCode.ERROR_MISSING_COMPONENT;
    }

    public boolean launch() {
        if (level == null || level.isClientSide || !canLaunch()) {
            return false;
        }
        return spawnLaunch(targetX, targetZ, null);
    }

    private boolean launchTo(int targetX, int targetZ, Entity track) {
        if (level == null || level.isClientSide || !radarCanLaunch()) {
            return false;
        }
        return spawnLaunch(targetX, targetZ, track);
    }

    private boolean spawnLaunch(int targetX, int targetZ, Entity track) {
        ItemStack missileStack = items.getStackInSlot(SLOT_MISSILE).copy();
        boolean abm = MissileLaunchRegistry.isAntiBallistic(missileStack);
        var spawner = abm ? null : MissileLaunchRegistry.getSpawner(missileStack.getItem());
        if (!abm && spawner == null) {
            return false;
        }

        int fuelCost = getRequiredFuelAmount();
        items.setStackInSlot(SLOT_MISSILE, ItemStack.EMPTY);
        energy.consume(LAUNCH_COST);
        if (fuelCost > 0) {
            fuelTank.drain(fuelCost, IFluidHandler.FluidAction.EXECUTE);
            oxidizerTank.drain(fuelCost, IFluidHandler.FluidAction.EXECUTE);
        }
        delay = COOLDOWN_TICKS;

        BlockPos pos = worldPosition;
        level.playSound(null, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                ModSounds.MISSILE_TAKEOFF.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        double y = pos.getY() + getLaunchOffset();
        if (abm) {
            EntityMissileAntiBallistic abmEntity = new EntityMissileAntiBallistic(
                    level, pos.getX() + 0.5D, y, pos.getZ() + 0.5D);
            if (track != null) {
                abmEntity.tracking = track;
            }
            level.addFreshEntity(abmEntity);
        } else {
            EntityMissileBaseNT missile = spawner.spawn(
                    level,
                    pos.getX() + 0.5D,
                    y,
                    pos.getZ() + 0.5D,
                    targetX, targetY, targetZ);
            level.addFreshEntity(missile);
        }
        onLaunched();
        setChanged();
        syncToClient();
        return true;
    }

    protected void onLaunched() {
    }

    public void checkRedstone(boolean powered) {
        if (powered && !wasPowered) {
            launchFromDesignator();
        }
        wasPowered = powered;
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

    public Component statusMessage() {
        syncTargetFromDesignator();
        if (!isMissileValid()) {
            return Component.literal("Not ready — need launchable missile");
        }
        if (energy.getEnergyStored() < LAUNCH_COST) {
            return Component.literal("Need " + LAUNCH_COST + " FE (have " + energy.getEnergyStored() + ")");
        }
        if (!hasTankFuel()) {
            int cost = getRequiredFuelAmount();
            ItemStack missile = items.getStackInSlot(SLOT_MISSILE);
            Component fuelName = missile.getItem() instanceof MissileItem mi
                    ? Component.translatable(mi.getFuelLangKey())
                    : Component.translatable("item.missile.fuel.ethanol_peroxide");
            return Component.literal("Not ready — need " + cost + " mB ").append(fuelName);
        }
        if (needsDesignator() && !hasTarget) {
            return Component.literal("No target — designator required");
        }
        if (delay > 0) {
            return Component.literal("Loading... (" + delay + ")");
        }
        return Component.literal("Ready → " + targetX + ", " + targetY + ", " + targetZ);
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

    protected void updateState() {
        if (!isMissileValid() || !hasFuel()) {
            state = STATE_MISSING;
            delay = COOLDOWN_TICKS;
        } else if (delay > 0) {
            state = STATE_LOADING;
        } else {
            state = STATE_READY;
        }
    }

    protected void processFluidSlots() {
        transferFluidItem(SLOT_FUEL_IN, SLOT_FUEL_OUT, fuelTank);
        transferFluidItem(SLOT_OX_IN, SLOT_OX_OUT, oxidizerTank);
    }

    private void transferFluidItem(int inSlot, int outSlot, FluidTank tank) {
        ItemStack in = items.getStackInSlot(inSlot);
        if (in.isEmpty()) {
            return;
        }

        // Legacy infinite barrel: fill without consuming
        if (in.getItem() instanceof com.hbm.items.machine.InfiniteFluidBarrelItem) {
            FluidStack content = tank.getFluid();
            Fluid targetFluid;
            if (!content.isEmpty()) {
                targetFluid = content.getFluid();
            } else if (tank == fuelTank) {
                targetFluid = ModFluids.ETHANOL.source.get();
            } else if (tank == oxidizerTank) {
                targetFluid = ModFluids.PEROXIDE.source.get();
            } else {
                return;
            }
            if (tank.isFluidValid(new FluidStack(targetFluid, 1))) {
                tank.fill(new FluidStack(targetFluid, Math.min(1000, tank.getSpace())),
                        IFluidHandler.FluidAction.EXECUTE);
            }
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

    /** 1.7.10 {@code TileEntityLaunchPad.getConPos} — cables one block outside the 3×3. */
    protected static void pullConPos(Level level, BlockPos pos, LaunchPadBlockEntity be) {
        be.pullEnergyPorts(level);
    }

    protected void pullEnergyPorts(Level level) {
        BlockPos pos = worldPosition;
        EnergyNetworkHelper.pullFrom(level, pos.offset(2, 0, -1), Direction.WEST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(2, 0, 1), Direction.WEST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-2, 0, -1), Direction.EAST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-2, 0, 1), Direction.EAST, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-1, 0, 2), Direction.NORTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(1, 0, 2), Direction.NORTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-1, 0, -2), Direction.SOUTH, energy, ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(1, 0, -2), Direction.SOUTH, energy, ENERGY_TRANSFER);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LaunchPadBlockEntity be) {
        LaunchPadBlock.tryCompleteStructure(level, pos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 && dz != 0) {
                    EnergyNetworkHelper.pullFromNeighbors(level, pos.offset(dx, 0, dz), be.energy, ENERGY_TRANSFER);
                }
            }
        }
        pullConPos(level, pos, be);
        ItemEnergyHelper.chargeFromItem(be.items.getStackInSlot(SLOT_BATTERY), be.energy, ENERGY_TRANSFER);
        be.processFluidSlots();

        if (be.delay > 0 && be.isMissileValid() && be.hasFuel()) {
            be.delay--;
        }
        int prev = be.state;
        be.updateState();
        if (prev != be.state) {
            be.setChanged();
            be.syncToClient();
        }
    }

    /**
     * Legacy client TE tick: while a missile is still above the pad, emit {@code launchSmoke}
     * (ground-hugging ParticleSmokePlume jets).
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, LaunchPadBlockEntity be) {
        if (!level.isClientSide) {
            return;
        }
        com.hbm.HbmNuclearTechMod.proxy.tickLaunchPadSmoke(level, pos);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.launch_pad");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new LaunchPadMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        energy.write(tag);
        tag.put("FuelTank", fuelTank.writeToNBT(new CompoundTag()));
        tag.put("OxTank", oxidizerTank.writeToNBT(new CompoundTag()));
        tag.putBoolean("hasTarget", hasTarget);
        tag.putInt("targetX", targetX);
        tag.putInt("targetY", targetY);
        tag.putInt("targetZ", targetZ);
        tag.putBoolean("wasPowered", wasPowered);
        tag.putInt("delay", delay);
        tag.putInt("padState", this.state);
        tag.putString("padFacing", getFacing().getSerializedName());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        energy.read(tag);
        if (tag.contains("FuelTank")) {
            fuelTank.readFromNBT(tag.getCompound("FuelTank"));
        }
        if (tag.contains("OxTank")) {
            oxidizerTank.readFromNBT(tag.getCompound("OxTank"));
        }
        hasTarget = tag.getBoolean("hasTarget");
        targetX = tag.getInt("targetX");
        targetY = tag.getInt("targetY");
        targetZ = tag.getInt("targetZ");
        wasPowered = tag.getBoolean("wasPowered");
        delay = tag.contains("delay") ? tag.getInt("delay") : COOLDOWN_TICKS;
        this.state = tag.getInt("padState");
        if (tag.contains("padFacing")) {
            Direction loaded = Direction.byName(tag.getString("padFacing"));
            this.facing = loaded != null && loaded.getAxis().isHorizontal() ? loaded : Direction.NORTH;
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
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        if (side != null && side.getAxis().isVertical()) {
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
        itemOptional.invalidate();
        energyOptional.invalidate();
        fluidOptional.invalidate();
    }
}
