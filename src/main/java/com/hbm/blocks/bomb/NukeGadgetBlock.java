package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.NukeGadgetBlockEntity;
import com.hbm.config.BombConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The Gadget nuclear bomb.
 */
public class NukeGadgetBlock extends AssembledNukeBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NukeGadgetBlockEntity(pos, state);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.gadgetRadius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_gadget";
    }
}
