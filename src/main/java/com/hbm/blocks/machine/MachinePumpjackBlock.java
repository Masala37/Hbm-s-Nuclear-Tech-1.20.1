package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blockentity.machine.PumpjackBlockEntity;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code MachinePumpjack}: Dummyable {@code {3,0,0,0,0,6}} plus walkway boxes.
 */
public class MachinePumpjackBlock extends BlockDummyable {
    public static final int[] DIM = {3, 0, 0, 0, 0, 6};
    public static final int[] WALK_A = {0, 0, -1, 1, 1, 1};
    public static final int[] WALK_B = {0, 0, 1, -1, 2, 2};

    public MachinePumpjackBlock() {
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
        return 0;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new PumpjackBlockEntity(pos, state);
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
    protected List<MultiblockHandlerXR.Cell> extraDummyCells(BlockPos core, int facing) {
        int rot = DummyableMeta.rotateYCounterClockwise(facing);
        int ox = core.getX() + DummyableMeta.offsetX(rot) * 3;
        int oz = core.getZ() + DummyableMeta.offsetZ(rot) * 3;
        List<MultiblockHandlerXR.Cell> extra = new ArrayList<>();
        extra.addAll(MultiblockHandlerXR.dummyCells(ox, core.getY(), oz, WALK_A, facing));
        extra.addAll(MultiblockHandlerXR.dummyCells(ox, core.getY(), oz, WALK_B, facing));
        return extra;
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        int rot = DummyableMeta.rotateYCounterClockwise(facing);
        BlockPos origin = core.offset(DummyableMeta.offsetX(rot) * 3, 0, DummyableMeta.offsetZ(rot) * 3);
        makeExtra(level, origin.offset(1, 0, 1));
        DummyableProxyBlockEntity.spawn(level, origin.offset(1, 0, 1));
        makeExtra(level, origin.offset(1, 0, -1));
        DummyableProxyBlockEntity.spawn(level, origin.offset(1, 0, -1));
        makeExtra(level, origin.offset(-1, 0, 1));
        DummyableProxyBlockEntity.spawn(level, origin.offset(-1, 0, 1));
        makeExtra(level, origin.offset(-1, 0, -1));
        DummyableProxyBlockEntity.spawn(level, origin.offset(-1, 0, -1));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_PUMPJACK.get(), PumpjackBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PumpjackBlockEntity jack) {
                MachineDrops.drop(level, pos, jack.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
