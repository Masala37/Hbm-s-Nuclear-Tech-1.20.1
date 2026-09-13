package com.hbm.blockentity.network;

import com.hbm.blocks.network.FluidDucts;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 {@code TileEntityFluidValve}: pipe node only while metadata is open.
 */
public class FluidValveBlockEntity extends FluidPipeBlockEntity {
    public FluidValveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_VALVE.get(), pos, state);
    }

    protected FluidValveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean isFluidPipe() {
        return FluidDucts.isOpen(getBlockState());
    }
}
