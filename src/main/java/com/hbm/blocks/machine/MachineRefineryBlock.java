package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blockentity.machine.RefineryBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
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
 * 1.7.10 {@code MachineRefinery}: 3×3×9 Dummyable, extras on the four back-ground corners.
 */
public class MachineRefineryBlock extends BlockDummyable {
    public static final int[] DIM = {8, 0, 1, 1, 1, 1};

    public MachineRefineryBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
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
        return 1;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new RefineryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (DummyableMeta.isCore(state.getValue(META))) {
            return newCoreEntity(pos, state);
        }
        if (DummyableMeta.isExtra(state.getValue(META))) {
            return new DummyableProxyBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        BlockPos back = core.offset(-DummyableMeta.offsetX(facing), 0, -DummyableMeta.offsetZ(facing));
        makeExtra(level, back.offset(1, 0, 1));
        DummyableProxyBlockEntity.spawn(level, back.offset(1, 0, 1));
        makeExtra(level, back.offset(1, 0, -1));
        DummyableProxyBlockEntity.spawn(level, back.offset(1, 0, -1));
        makeExtra(level, back.offset(-1, 0, 1));
        DummyableProxyBlockEntity.spawn(level, back.offset(-1, 0, 1));
        makeExtra(level, back.offset(-1, 0, -1));
        DummyableProxyBlockEntity.spawn(level, back.offset(-1, 0, -1));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state) || level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_REFINERY.get(), RefineryBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RefineryBlockEntity refinery) {
                MachineDrops.drop(level, pos, refinery.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
