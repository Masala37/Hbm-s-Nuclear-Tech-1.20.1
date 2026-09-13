package com.hbm.blocks.network;

import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blockentity.network.CraneExtractorBlockEntity;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code CraneExtractor}.
 */
public class CraneExtractorBlock extends CraneBaseBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.CRANE_EXTRACTOR.get(), CraneExtractorBlockEntity::serverTick);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CraneExtractorBlockEntity extractor) {
                ItemStackHandler items = extractor.getItems();
                ItemStackHandler drop = new ItemStackHandler(CraneExtractorBlockEntity.SLOTS
                        - CraneExtractorBlockEntity.BUFFER_START);
                int o = 0;
                for (int i = CraneExtractorBlockEntity.BUFFER_START; i < CraneExtractorBlockEntity.SLOTS; i++) {
                    drop.setStackInSlot(o++, items.getStackInSlot(i));
                    items.setStackInSlot(i, ItemStack.EMPTY);
                }
                MachineDrops.drop(level, pos, drop);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
