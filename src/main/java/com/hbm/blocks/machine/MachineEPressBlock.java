package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.EPressBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blocks.BlockDummyable;
import com.hbm.registry.ModBlockEntities;
import api.hbm.block.IToolable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
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
 * 1.7.10 {@code MachineEPress}: 1×1×3 Dummyable electric press.
 */
public class MachineEPressBlock extends BlockDummyable implements IToolable {
    public MachineEPressBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    public int[] getDimensions() {
        return new int[]{2, 0, 0, 0, 0, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new EPressBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.EPRESS.get(), EPressBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EPressBlockEntity press) {
                MachineDrops.drop(level, pos, press.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction side, float hitX, float hitY, float hitZ,
                           ToolType tool) {
        return drillOutDummy(level, pos, level.getBlockState(pos), tool);
    }
}
