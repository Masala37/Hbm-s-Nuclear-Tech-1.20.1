package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.NukeManBlockEntity;
import com.hbm.config.BombConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Fat Man nuclear bomb.
 */
public class NukeManBlock extends AssembledNukeBlock {
    private static final VoxelShape INTERACT = Block.box(-24, 0, -16, 40, 32, 16);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NukeManBlockEntity(pos, state);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.manRadius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_man";
    }

    @Override
    protected VoxelShape interactionShape() {
        return INTERACT;
    }
}
