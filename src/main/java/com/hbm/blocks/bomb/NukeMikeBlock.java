package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.NukeMikeBlockEntity;
import com.hbm.config.BombConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Ivy Mike. Primary-only yield uses Fat Man radius; full secondary uses mikeRadius.
 */
public class NukeMikeBlock extends AssembledNukeBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NukeMikeBlockEntity(pos, state);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.mikeRadius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_mike";
    }
}
