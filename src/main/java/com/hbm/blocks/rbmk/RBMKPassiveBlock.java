package com.hbm.blocks.rbmk;

import com.hbm.blockentity.rbmk.RBMKPassiveBlockEntity;
import com.hbm.rbmk.RBMKColumnType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RBMKPassiveBlock extends RBMKBaseBlock {
    private final RBMKColumnType columnType;

    public RBMKPassiveBlock(RBMKColumnType type) {
        this.columnType = type;
    }

    public RBMKColumnType getColumnType() {
        return columnType;
    }

    @Override
    protected BlockEntity createCoreBlockEntity(BlockPos pos, BlockState state) {
        return new RBMKPassiveBlockEntity(pos, state);
    }
}
