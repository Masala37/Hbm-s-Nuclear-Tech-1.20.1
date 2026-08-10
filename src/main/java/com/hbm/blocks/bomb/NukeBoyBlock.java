package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.NukeBoyBlockEntity;
import com.hbm.config.BombConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Little Boy nuclear bomb.
 */
public class NukeBoyBlock extends AssembledNukeBlock {
    private static final VoxelShape INTERACT = Block.box(-8, 0, -8, 48, 16, 8);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NukeBoyBlockEntity(pos, state);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.boyRadius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_boy";
    }

    @Override
    protected VoxelShape interactionShape() {
        return INTERACT;
    }
}
