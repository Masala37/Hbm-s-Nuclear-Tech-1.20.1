package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blockentity.network.CraneInserterBlockEntity;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code CraneInserter}.
 */
public class CraneInserterBlock extends CraneBaseBlock implements IEnterableBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneInserterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.CRANE_INSERTER.get(), CraneInserterBlockEntity::serverTick);
    }

    @Override
    public boolean canItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {
        return getInputSide(level, pos) == dir;
    }

    @Override
    public void onItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CraneInserterBlockEntity inserter) {
            inserter.acceptFromBelt(entity);
        }
    }

    @Override
    public boolean canPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) {
        return true;
    }

    @Override
    public void onPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity) {
        // Packages are not spawned in this pass.
    }

    private Direction getInputSide(Level level, BlockPos pos) {
        return level.getBlockState(pos).getValue(FACING);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CraneInserterBlockEntity inserter) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer(
                    com.hbm.conveyor.CraneInventories.asContainer(inserter.getItems()));
        }
        return 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CraneInserterBlockEntity inserter) {
                MachineDrops.drop(level, pos, inserter.getItems());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
