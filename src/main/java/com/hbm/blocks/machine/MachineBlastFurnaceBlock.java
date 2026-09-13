package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.BlastFurnaceBlockEntity;
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
 * 1.7.10 {@code MachineBlastFurnace}: 7×3×3 Dummyable NT blast furnace.
 */
public class MachineBlastFurnaceBlock extends BlockDummyable {
    public MachineBlastFurnaceBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    @Override
    public int[] getDimensions() {
        return new int[]{6, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new BlastFurnaceBlockEntity(pos, state);
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        makeExtra(level, core.east());
        makeExtra(level, core.west());
        makeExtra(level, core.north());
        makeExtra(level, core.south());
        BlockPos along = core.offset(DummyableMeta.offsetX(facing), 0, DummyableMeta.offsetZ(facing));
        makeExtra(level, along.above(3));
        makeExtra(level, along.above(5));
        makeExtra(level, core.above(6));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.BLAST_FURNACE.get(), BlastFurnaceBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlastFurnaceBlockEntity furnace) {
                MachineDrops.drop(level, pos, furnace.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
