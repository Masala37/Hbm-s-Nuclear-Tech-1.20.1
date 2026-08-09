package com.hbm.blockentity.rbmk;

import com.hbm.rbmk.RBMKColumnType;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RBMKPassiveBlockEntity extends RBMKBaseBlockEntity {
    private final RBMKColumnType columnType;

    public RBMKPassiveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RBMK_PASSIVE.get(), pos, state);
        this.columnType = resolveType(state);
    }

    private static RBMKColumnType resolveType(BlockState state) {
        Block block = state.getBlock();
        if (block == ModBlocks.RBMK_REFLECTOR.get()) {
            return RBMKColumnType.REFLECTOR;
        }
        if (block == ModBlocks.RBMK_ABSORBER.get()) {
            return RBMKColumnType.ABSORBER;
        }
        if (block == ModBlocks.RBMK_MODERATOR.get()) {
            return RBMKColumnType.MODERATOR;
        }
        return RBMKColumnType.BLANK;
    }

    @Override
    public RBMKColumnType getColumnType() {
        return columnType;
    }
}
