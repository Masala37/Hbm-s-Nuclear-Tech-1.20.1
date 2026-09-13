package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.ArcWelderBlockEntity;
import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code MachineArcWelder}: Dummyable {@code {1,0,1,0,1,1}} (3×2×2).
 * Dummy cells are inventory/power/fluid proxies.
 */
public class MachineArcWelderBlock extends BlockDummyable {
    public static final int[] DIM = {1, 0, 1, 0, 1, 1};

    public MachineArcWelderBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    public int[] getDimensions() {
        return DIM;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new ArcWelderBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (DummyableMeta.isCore(state.getValue(META))) {
            return newCoreEntity(pos, state);
        }
        return new DummyableProxyBlockEntity(pos, state);
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        for (MultiblockHandlerXR.Cell cell : MultiblockHandlerXR.dummyCells(
                core.getX(), core.getY(), core.getZ(), getDimensions(), facing)) {
            BlockPos at = new BlockPos(cell.x(), cell.y(), cell.z());
            if (at.equals(core)) {
                continue;
            }
            makeExtra(level, at);
            DummyableProxyBlockEntity.spawn(level, at);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_ARC_WELDER.get(), ArcWelderBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ArcWelderBlockEntity welder) {
                MachineDrops.drop(level, pos, welder.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
