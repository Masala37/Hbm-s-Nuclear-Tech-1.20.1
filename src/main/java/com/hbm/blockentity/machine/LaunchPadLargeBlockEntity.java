package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.LaunchPadLargeBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.energy.ItemEnergyHelper;
import com.hbm.handler.LaunchPadFormFactor;
import com.hbm.handler.LaunchPadLoadingCycle;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class LaunchPadLargeBlockEntity extends LaunchPadBlockEntity {
    public int formFactor = -1;
    public boolean erected;
    public boolean readyToLoad;
    public boolean scheduleErect;
    public float lift = 1.0F;
    public float erector = 90.0F;
    public float prevLift = 1.0F;
    public float prevErector = 90.0F;
    public float syncLift = 1.0F;
    public float syncErector = 90.0F;
    private int sync;
    public boolean liftMoving;
    public boolean erectorMoving;

    public LaunchPadLargeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_PAD_LARGE.get(), pos, state);
        this.delay = 20;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.launch_pad_large");
    }

    @Override
    public double getLaunchOffset() {
        return 2.0D;
    }

    @Override
    public boolean isReadyForLaunch() {
        return erected && readyToLoad;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 10.0D, worldPosition.getY(), worldPosition.getZ() - 10.0D,
                worldPosition.getX() + 11.0D, worldPosition.getY() + 15.0D, worldPosition.getZ() + 11.0D);
    }

    @Override
    protected void onLaunched() {
        erected = false;
        readyToLoad = false;
        scheduleErect = false;
        delay = 20;
    }

    @Override
    protected void onMissileChanged() {
        super.onMissileChanged();
        applyLoadingState(loadingState().missileChanged());
    }

    private LaunchPadLoadingCycle.State loadingState() {
        return new LaunchPadLoadingCycle.State(lift, erector, delay, erected, readyToLoad, scheduleErect);
    }

    private void applyLoadingState(LaunchPadLoadingCycle.State next) {
        lift = next.lift();
        erector = next.erector();
        delay = next.delay();
        erected = next.erected();
        readyToLoad = next.readyToLoad();
        scheduleErect = next.scheduleErect();
    }

    @Override
    protected void pullEnergyPorts(Level level) {
        BlockPos pos = worldPosition;
        EnergyNetworkHelper.pullFrom(level, pos.offset(5, 0, -2), Direction.WEST, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(5, 0, 2), Direction.WEST, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-5, 0, -2), Direction.EAST, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-5, 0, 2), Direction.EAST, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-2, 0, 5), Direction.NORTH, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(2, 0, 5), Direction.NORTH, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-2, 0, -5), Direction.SOUTH, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(2, 0, -5), Direction.SOUTH, getEnergy(), ENERGY_TRANSFER);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LaunchPadLargeBlockEntity be) {
        LaunchPadLargeBlock.tryCompleteStructure(level, pos);
        int[][] extras = {{4, 2}, {4, -2}, {-4, 2}, {-4, -2}, {2, 4}, {2, -4}, {-2, 4}, {-2, -4}};
        for (int[] extra : extras) {
            EnergyNetworkHelper.pullFromNeighbors(level, pos.offset(extra[0], 0, extra[1]),
                    be.getEnergy(), ENERGY_TRANSFER);
        }
        be.pullEnergyPorts(level);
        ItemEnergyHelper.chargeFromItem(be.getItems().getStackInSlot(SLOT_BATTERY), be.getEnergy(), ENERGY_TRANSFER);
        be.processFluidSlots();

        be.prevLift = be.lift;
        be.prevErector = be.erector;

        ItemStack missile = be.getItems().getStackInSlot(SLOT_MISSILE);
        boolean missileValid = be.isMissileValid();
        boolean slow = false;
        if (missileValid) {
            LaunchPadFormFactor form = LaunchPadFormFactor.of(missile);
            be.formFactor = form.ordinal();
            slow = form.slowErector();
        }
        // Retain the previous form factor while the empty mechanism returns home.
        be.applyLoadingState(LaunchPadLoadingCycle.tick(be.loadingState(), missileValid,
                be.getEnergy().getEnergyStored() >= LAUNCH_COST, slow));
        be.state = !missileValid || !be.hasFuel() ? STATE_MISSING
                : be.isReadyForLaunch() ? STATE_READY : STATE_LOADING;

        boolean prevLiftMoving = be.liftMoving;
        boolean prevErectorMoving = be.erectorMoving;
        be.liftMoving = be.prevLift != be.lift;
        be.erectorMoving = be.prevErector != be.erector;
        if (prevLiftMoving && !be.liftMoving) {
            level.playSound(null, pos, ModSounds.DOOR_WGH_STOP.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        }
        if (prevErectorMoving && !be.erectorMoving) {
            level.playSound(null, pos, ModSounds.DOOR_GARAGE_STOP.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        }
        be.setChanged();
        be.syncToClient();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LaunchPadLargeBlockEntity be) {
        be.prevLift = be.lift;
        be.prevErector = be.erector;
        if (be.sync > 0) {
            be.lift = be.lift + ((be.syncLift - be.lift) / (float) be.sync);
            be.erector = be.erector + ((be.syncErector - be.erector) / (float) be.sync);
            be.sync--;
        } else {
            be.lift = be.syncLift;
            be.erector = be.syncErector;
        }
        com.hbm.HbmNuclearTechMod.proxy.tickLaunchPadLarge(level, pos, be);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("erected", erected);
        tag.putBoolean("readyToLoad", readyToLoad);
        tag.putBoolean("scheduleErect", scheduleErect);
        tag.putBoolean("liftMoving", liftMoving);
        tag.putBoolean("erectorMoving", erectorMoving);
        tag.putFloat("lift", lift);
        tag.putFloat("erector", erector);
        tag.putInt("formFactor", formFactor);
    }

    @Override
    public void load(CompoundTag tag) {
        float oldLift = lift;
        float oldErector = erector;
        super.load(tag);
        erected = tag.getBoolean("erected");
        readyToLoad = tag.getBoolean("readyToLoad");
        liftMoving = tag.getBoolean("liftMoving");
        erectorMoving = tag.getBoolean("erectorMoving");
        syncLift = tag.contains("lift") ? tag.getFloat("lift") : lift;
        syncErector = tag.contains("erector") ? tag.getFloat("erector") : erector;
        formFactor = tag.contains("formFactor") ? tag.getInt("formFactor") : -1;
        scheduleErect = tag.contains("scheduleErect") ? tag.getBoolean("scheduleErect")
                : readyToLoad && !erected && syncLift == 0.0F && syncErector == 0.0F;
        if (scheduleErect && delay <= 0) {
            delay = 10;
        }
        if (level != null && level.isClientSide) {
            if (oldLift != syncLift || oldErector != syncErector) {
                sync = 3;
            }
        } else {
            lift = syncLift;
            erector = syncErector;
        }
    }
}
