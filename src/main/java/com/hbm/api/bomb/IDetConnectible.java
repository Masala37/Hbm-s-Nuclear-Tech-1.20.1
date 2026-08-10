package com.hbm.api.bomb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

/**
 * Blocks that visually/connectively link to det cord (legacy {@code IDetConnectible}).
 */
public interface IDetConnectible {
    default boolean canConnectToDetCord(BlockGetter level, BlockPos pos, Direction fromNeighbor) {
        return true;
    }

    static boolean isConnectible(BlockGetter level, BlockPos pos, Direction fromNeighbor) {
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof IDetConnectible connectible) {
            return connectible.canConnectToDetCord(level, pos, fromNeighbor);
        }
        return false;
    }
}
