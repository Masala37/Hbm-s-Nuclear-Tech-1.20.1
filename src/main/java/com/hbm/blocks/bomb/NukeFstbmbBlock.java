package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.AssembledNuke;
import com.hbm.blockentity.bomb.NukeFstbmbBlockEntity;
import com.hbm.config.BombConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Balefire Bomb ({@code nuke_fstbmb}): egg + spark/trixite battery, optional countdown.
 */
public class NukeFstbmbBlock extends AssembledNukeBlock {
    private static final VoxelShape INTERACT = Block.box(-8, 0, -8, 48, 24, 24);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NukeFstbmbBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                com.hbm.registry.ModBlockEntities.NUKE_FSTBMB.get(), NukeFstbmbBlockEntity::serverTick);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.balefireRadius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_fstbmb";
    }

    @Override
    protected VoxelShape interactionShape() {
        return INTERACT;
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        AssembledNuke nuke = asAssembly(level.getBlockEntity(pos));
        if (!(nuke instanceof NukeFstbmbBlockEntity balefire) || !balefire.isReady()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        balefire.detonate();
        return BombReturnCode.DETONATED;
    }
}
