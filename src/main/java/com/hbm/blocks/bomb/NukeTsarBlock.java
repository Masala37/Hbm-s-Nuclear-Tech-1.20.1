package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.NukeTsarBlockEntity;
import com.hbm.config.BombConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Tsar Bomba. Primary uses Fat Man radius; full secondary uses tsarRadius.
 */
public class NukeTsarBlock extends AssembledNukeBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NukeTsarBlockEntity(pos, state);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.tsarRadius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_tsar";
    }
}
