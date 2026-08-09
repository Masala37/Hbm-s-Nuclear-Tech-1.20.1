package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.NukeManBlockEntity;
import com.hbm.config.BombConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Fat Man nuclear bomb.
 */
public class NukeManBlock extends AssembledNukeBlock {
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
}
