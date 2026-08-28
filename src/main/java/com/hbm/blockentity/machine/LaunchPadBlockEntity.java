package com.hbm.blockentity.machine;

import com.hbm.api.bomb.IBomb;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.MissileLaunchRegistry;
import com.hbm.inventory.menu.LaunchPadMenu;
import com.hbm.items.tool.DesignatorItem;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
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
public class LaunchPadBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_MISSILE = 0;
    public static final int SLOT_DESIGNATOR = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_FUEL_IN = 3;
    public static final int SLOT_FUEL_OUT = 4;
    public static final int SLOT_OX_IN = 5;
    public static final int SLOT_OX_OUT = 6;

    public static final int ENERGY_CAPACITY = 100_000;
    public static final int ENERGY_TRANSFER = 5_000;
    public static final int LAUNCH_COST = 75_000;
    public static final int TANK_CAPACITY = 24_000;
    public static final int TIER1_FUEL_COST = 4_000;
    public static final int COOLDOWN_TICKS = 100;

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
                case SLOT_DESIGNATOR -> stack.getItem() instanceof DesignatorItem;
                case SLOT_BATTERY -> ItemEnergyHelper.isEnergyItem(stack);
                case SLOT_FUEL_IN, SLOT_OX_IN -> FluidUtil.getFluidHandler(stack).isPresent()
                        || stack.getItem() instanceof com.hbm.items.machine.InfiniteFluidBarrelItem;
                default -> false;
            };
        }
    };

    private final ModEnergyStorage energy = new ModEnergyStorage(
            ENERGY_CAPACITY, ENERGY_TRANSFER, ENERGY_TRANSFER, this::onChanged);

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
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            if (isFuelFluid(resource.getFluid())) {
                return fuelTank.drain(resource, action);
            }
            if (isOxidizerFluid(resource.getFluid())) {
                return oxidizerTank.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (!fuelTank.getFluid().isEmpty()) {
                return fuelTank.drain(maxDrain, action);
            }
            return oxidizerTank.drain(maxDrain, action);
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> combinedFluids);

    private boolean hasTarget;
    private int targetX;
    private int targetY;
    private int targetZ;
    private boolean wasPowered;
    private int delay = COOLDOWN_TICKS;
    private int state = STATE_MISSING;

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_PAD.get(), pos, state);
    }

    /**
     * Silo mesh is 3×3 (OBJ ±1.5) and standing missiles are tall; default 1×1×1 AABB
     * frustum-culls the BER when looking at the rocket tip or silo edges.
     */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(2.0D, 8.0D, 2.0D);
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

    private void syncTargetFromDesignator() {
        ItemStack stack = items.getStackInSlot(SLOT_DESIGNATOR);
        if (stack.getItem() instanceof DesignatorItem && DesignatorItem.hasTarget(stack)) {
            BlockPos t = DesignatorItem.getTarget(stack);
            hasTarget = true;
            targetX = t.getX();
            targetY = t.getY();
            targetZ = t.getZ();
        }
    }

    public static boolean isFuelFluid(Fluid fluid) {
        return fluid == ModFluids.ETHANOL.source.get();
    }

    public static boolean isOxidizerFluid(Fluid fluid) {
        return fluid == ModFluids.PEROXIDE.source.get();
    }

    public boolean isMissileValid() {
        return MissileLaunchRegistry.isLaunchable(items.getStackInSlot(SLOT_MISSILE));
    }

    /**
     * Legacy {@code ItemMissile.fuelCap}: MICRO/Tier0 solid = 0 (power only);
     * V2/Strong on this pad = ethanol+peroxide drain amount.
     */
    public int getRequiredFuelAmount() {
        ItemStack missile = items.getStackInSlot(SLOT_MISSILE);
        if (missile.getItem() instanceof com.hbm.items.weapon.MissileItem mi) {
            return mi.getFuelCap();
        }
        // Non-MissileItem launchables default to Tier1 fluid cost
        return isMissileValid() ? TIER1_FUEL_COST : 0;
    }

    public boolean hasFuel() {
        int cost = getRequiredFuelAmount();
        if (cost <= 0) {
            // Solid / pre-fueled (legacy MICRO / Tier0 — taint, micro, bhole, schrab) — no tank drain
            return true;
        }
        return fuelTank.getFluidAmount() >= cost
                && oxidizerTank.getFluidAmount() >= cost
                && isFuelFluid(fuelTank.getFluid().getFluid())
                && isOxidizerFluid(oxidizerTank.getFluid().getFluid());
    }

    public boolean tryInsertMissile(ItemStack stack) {
        if (!MissileLaunchRegistry.isLaunchable(stack) || !items.getStackInSlot(SLOT_MISSILE).isEmpty()) {
            return false;
        }
        items.setStackInSlot(SLOT_MISSILE, stack.split(1));
        return true;
    }

    public boolean trySetTargetFromDesignator(ItemStack stack) {
        if (!(stack.getItem() instanceof DesignatorItem) || !DesignatorItem.hasTarget(stack)) {
            return false;
        }
        setTarget(DesignatorItem.getTarget(stack));
        return true;
    }

    public boolean canLaunch() {
        syncTargetFromDesignator();
        return hasTarget
                && isMissileValid()
                && hasFuel()
                && energy.getEnergyStored() >= LAUNCH_COST
                && delay <= 0;
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
        return launch() ? IBomb.BombReturnCode.LAUNCHED : IBomb.BombReturnCode.ERROR_MISSING_COMPONENT;
    }

    public boolean launch() {
        if (level == null || level.isClientSide || !canLaunch()) {
            return false;
        }
        ItemStack missileStack = items.getStackInSlot(SLOT_MISSILE);
        var spawner = MissileLaunchRegistry.getSpawner(missileStack.getItem());
        if (spawner == null) {
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
        EntityMissileBaseNT missile = spawner.spawn(
                level,
                pos.getX() + 0.5D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D,
                targetX, targetY, targetZ);
        level.addFreshEntity(missile);
        playLaunchSounds(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        setChanged();
        syncToClient();
        return true;
    }

    private static void playLaunchSounds(Level level, double x, double y, double z) {
        level.playSound(null, x, y, z, ModSounds.MISSILE_TAKEOFF.get(), SoundSource.PLAYERS, 4.0F, 1.0F);
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            for (net.minecraft.server.level.ServerPlayer player : server.players()) {
                if (player.distanceToSqr(x, y, z) < 128.0D * 128.0D) {
                    player.playNotifySound(ModSounds.MISSILE_TAKEOFF.get(), SoundSource.PLAYERS, 4.0F, 1.0F);
                }
            }
        }
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
        if (!hasFuel()) {
            int cost = getRequiredFuelAmount();
            return Component.literal("Not ready — need " + cost + " mB ethanol + peroxide");
        }
        if (!hasTarget) {
            return Component.literal("No target — designator required");
        }
        if (energy.getEnergyStored() < LAUNCH_COST) {
            return Component.literal("Need " + LAUNCH_COST + " FE (have " + energy.getEnergyStored() + ")");
        }
        if (delay > 0) {
            return Component.literal("Loading... (" + delay + ")");
        }
        return Component.literal("Ready → " + targetX + ", " + targetY + ", " + targetZ);
    }

    private void onChanged() {
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void updateState() {
        if (!isMissileValid() || !hasFuel()) {
            state = STATE_MISSING;
            delay = COOLDOWN_TICKS;
        } else if (delay > 0) {
            state = STATE_LOADING;
        } else {
            state = STATE_READY;
        }
    }

    private void processFluidSlots() {
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, LaunchPadBlockEntity be) {
        EnergyNetworkHelper.pullFromNeighbors(level, pos, be.energy, ENERGY_TRANSFER);
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
