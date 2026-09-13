package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blockentity.machine.OilWellBlockEntity;
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
 * 1.7.10 {@code MachineOilWell}: center column plus 3×3 tower from y+1 and four ground legs.
 */
public class MachineOilWellBlock extends BlockDummyable {
    public static final int[] COLUMN = {9, 0, 0, 0, 0, 0};
    public static final int[] TOWER = {8, 0, 1, 1, 1, 1};
    public static final int[] LEG = {-1, 1, 0, 0, 0, 0};

    public MachineOilWellBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 20.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    public int[] getDimensions() {
        return COLUMN;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new OilWellBlockEntity(pos, state);
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
        List<MultiblockHandlerXR.Cell> extra = new ArrayList<>();
        extra.addAll(MultiblockHandlerXR.dummyCells(
                core.getX(), core.getY() + 1, core.getZ(), TOWER, facing));
        extra.addAll(MultiblockHandlerXR.dummyCells(
                core.getX() + 1, core.getY() + 1, core.getZ() + 1, LEG, facing));
        extra.addAll(MultiblockHandlerXR.dummyCells(
                core.getX() + 1, core.getY() + 1, core.getZ() - 1, LEG, facing));
        extra.addAll(MultiblockHandlerXR.dummyCells(
                core.getX() - 1, core.getY() + 1, core.getZ() + 1, LEG, facing));
        extra.addAll(MultiblockHandlerXR.dummyCells(
                core.getX() - 1, core.getY() + 1, core.getZ() - 1, LEG, facing));
        return extra;
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        makeExtra(level, core.offset(1, 0, 1));
        DummyableProxyBlockEntity.spawn(level, core.offset(1, 0, 1));
        makeExtra(level, core.offset(1, 0, -1));
        DummyableProxyBlockEntity.spawn(level, core.offset(1, 0, -1));
        makeExtra(level, core.offset(-1, 0, 1));
        DummyableProxyBlockEntity.spawn(level, core.offset(-1, 0, 1));
        makeExtra(level, core.offset(-1, 0, -1));
        DummyableProxyBlockEntity.spawn(level, core.offset(-1, 0, -1));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state) || level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_WELL.get(), OilWellBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof OilWellBlockEntity well) {
                MachineDrops.drop(level, pos, well.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
