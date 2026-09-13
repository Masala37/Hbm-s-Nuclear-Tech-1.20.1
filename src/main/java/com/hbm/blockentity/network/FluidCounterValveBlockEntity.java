package com.hbm.blockentity.network;

import com.hbm.blocks.network.FluidDucts;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 {@code TileEntityFluidCounterValve}: adds net transfer to a running total.
 */
public class FluidCounterValveBlockEntity extends FluidValveBlockEntity {
    private long counter;

    public FluidCounterValveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_COUNTER_VALVE.get(), pos, state);
    }

    public long getCounter() {
        return counter;
    }

    @Override
    public void onNetFluidMoved(int millibuckets) {
        long next = FluidDucts.addCounter(counter, millibuckets, pipeFluid() != null);
        if (next == counter) {
            return;
        }
        counter = next;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("counter", counter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        counter = tag.getLong("counter");
    }
}
