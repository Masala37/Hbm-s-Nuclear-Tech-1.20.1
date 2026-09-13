package com.hbm.blockentity.machine;

import api.hbm.tile.IHeatSource;
import com.hbm.fluid.IoFluidHandler;
import com.hbm.fluid.SteamCycle;
import com.hbm.heat.HeatTransfer;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code TileEntityHeatBoiler}. Explosion and identifier fluid swap skipped.
 */
public class HeatBoilerBlockEntity extends BlockEntity {
    public static final int WATER_CAP = 16_000;
    public static final int STEAM_CAP = 16_000 * 100;
    public static final int MAX_HEAT = 3_200_000;

    private final FluidTank water = new FluidTank(WATER_CAP) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            if (SteamCycle.isWater(stack.getFluid())) {
                return getFluid().isEmpty() || SteamCycle.isWater(getFluid().getFluid());
            }
            if (SteamCycle.isOil(stack.getFluid())) {
                return getFluid().isEmpty() || SteamCycle.isOil(getFluid().getFluid());
            }
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (SteamCycle.isWater(resource.getFluid())) {
                return super.fill(SteamCycle.canonicalizeWater(resource), action);
            }
            return super.fill(resource, action);
        }
    };
    private final FluidTank steam = new FluidTank(STEAM_CAP) {
        @Override
        protected void onContentsChanged() {
            onChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return SteamCycle.isSteam(stack.getFluid()) || SteamCycle.isHotOil(stack.getFluid());
        }
    };
    private final IFluidHandler fluids = new IoFluidHandler(water, steam,
            stack -> SteamCycle.isWater(stack.getFluid()) || SteamCycle.isOil(stack.getFluid()));
    private final LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(() -> fluids);

    private int heat;
    private boolean isOn;

    public HeatBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_BOILER.get(), pos, state);
    }

    public FluidTank getWater() {
        return water;
    }

    public FluidTank getSteam() {
        return steam;
    }

    public IFluidHandler getFluidHandler() {
        return fluids;
    }

    public int getHeat() {
        return heat;
    }

    public boolean isOn() {
        return isOn;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HeatBoilerBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.tryPullHeat(level, pos);
        be.isOn = false;
        be.tryConvert();
        be.onChanged();
    }

    private void tryPullHeat(Level level, BlockPos pos) {
        BlockEntity below = level.getBlockEntity(pos.below());
        IHeatSource source = below instanceof IHeatSource heatSource ? heatSource : null;
        heat = HeatTransfer.pull(source, heat, MAX_HEAT);
    }

    private void tryConvert() {
        if (SteamCycle.isOil(water.getFluid().getFluid())) {
            if (!steam.getFluid().isEmpty() && !SteamCycle.isHotOil(steam.getFluid().getFluid())) {
                return;
            }
            int heatReq = SteamCycle.oilHeatReq();
            int ops = SteamCycle.heatOil(water.getFluidAmount(), steam.getFluidAmount(), steam.getCapacity(),
                    heat, heatReq);
            if (ops <= 0) {
                return;
            }
            water.drain(ops * SteamCycle.OIL_IN, IFluidHandler.FluidAction.EXECUTE);
            steam.fill(new FluidStack(SteamCycle.hotOil(), ops * SteamCycle.HOTOIL_OUT), IFluidHandler.FluidAction.EXECUTE);
            heat -= heatReq * ops;
            isOn = true;
            return;
        }
        if (!steam.getFluid().isEmpty() && !SteamCycle.isSteam(steam.getFluid().getFluid())) {
            return;
        }
        int heatReq = SteamCycle.boilerHeatReq();
        int ops = SteamCycle.boil(water.getFluidAmount(), steam.getFluidAmount(), steam.getCapacity(), heat, heatReq);
        if (ops <= 0) {
            return;
        }
        water.drain(ops * SteamCycle.WATER_IN, IFluidHandler.FluidAction.EXECUTE);
        steam.fill(new FluidStack(SteamCycle.steam(), ops * SteamCycle.STEAM_OUT), IFluidHandler.FluidAction.EXECUTE);
        heat -= heatReq * ops;
        isOn = true;
    }

    private void onChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 4, 2));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Water", water.writeToNBT(new CompoundTag()));
        tag.put("Steam", steam.writeToNBT(new CompoundTag()));
        tag.putInt("Heat", heat);
        tag.putBoolean("IsOn", isOn);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Water")) {
            water.readFromNBT(tag.getCompound("Water"));
        }
        if (tag.contains("Steam")) {
            steam.readFromNBT(tag.getCompound("Steam"));
        }
        heat = tag.getInt("Heat");
        isOn = tag.getBoolean("IsOn");
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
        fluidOptional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        return super.getCapability(cap, side);
    }
}
