package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.FelBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
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
 * 1.7.10 {@code MachineFEL}: Dummyable {@code {2,0,4,2,1,1}}, offset 2.
 * One energy extra at core − 4×facing, y+1.
 */
public class MachineFelBlock extends BlockDummyable {
    public static final int[] DIM = {2, 0, 4, 2, 1, 1};

    public MachineFelBlock() {
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
        return 2;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new FelBlockEntity(pos, state);
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
        BlockPos extra = extraPos(core, facing);
        makeExtra(level, extra);
        DummyableProxyBlockEntity.spawn(level, extra);
    }

    public static BlockPos extraPos(BlockPos core, int facing) {
        return core.offset(-4 * DummyableMeta.offsetX(facing), 1, -4 * DummyableMeta.offsetZ(facing));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_FEL.get(), FelBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FelBlockEntity fel) {
                MachineDrops.drop(level, pos, fel.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
