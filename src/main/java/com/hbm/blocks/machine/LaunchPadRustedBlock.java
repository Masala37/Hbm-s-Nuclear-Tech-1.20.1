package com.hbm.blocks.machine;

import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.machine.LaunchPadRustedBlockEntity;
import com.hbm.inventory.menu.HbmMenuHelper;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 3×3 rusted silo (1.7.10 {@code LaunchPadRusted}) — loaded rusted doomsday, codes + key.
 */
public class LaunchPadRustedBlock extends LaunchPadBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCore(state) ? new LaunchPadRustedBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.LAUNCH_PAD_RUSTED.get(),
                level.isClientSide ? LaunchPadRustedBlockEntity::clientTick : LaunchPadRustedBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide || !(state.getBlock() instanceof LaunchPadRustedBlock)) {
            return;
        }
        fillStructure(level, corePos(pos, state), state.getBlock());
        BlockEntity be = level.getBlockEntity(corePos(pos, state));
        if (be instanceof LaunchPadRustedBlockEntity pad && placer != null) {
            pad.setFacing(placer.getDirection());
        }
        checkRustedPower(level, corePos(pos, state));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (DummyablePlacement.placing() || DummyablePlacement.dismantling()) {
            return;
        }
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, false);
            return;
        }
        checkRustedPower(level, pos);
    }

    private static void checkRustedPower(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof LaunchPadRustedBlock)) {
            return;
        }
        BlockPos core = corePos(pos, state);
        BlockEntity be = level.getBlockEntity(core);
        if (!(be instanceof LaunchPadRustedBlockEntity pad)) {
            return;
        }
        boolean powered = false;
        for (int dx = -1; dx <= 1 && !powered; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.hasNeighborSignal(core.offset(dx, 0, dz))) {
                    powered = true;
                    break;
                }
            }
        }
        pad.checkRedstone(powered);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                    InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(corePos(pos, state));
        if (be instanceof LaunchPadRustedBlockEntity pad) {
            HbmMenuHelper.open(sp, pad, corePos(pos, state));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        BlockEntity be = level.getBlockEntity(corePos(pos, level.getBlockState(pos)));
        if (be instanceof LaunchPadRustedBlockEntity pad) {
            return pad.launch();
        }
        return BombReturnCode.UNDEFINED;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            return;
        }
        if (DummyablePlacement.dismantling()) {
            if (state.hasBlockEntity() && !newState.is(this)) {
                level.removeBlockEntity(pos);
            }
            return;
        }
        BlockPos core = corePos(pos, state);
        BlockEntity be = level.getBlockEntity(core);
        if (be instanceof LaunchPadRustedBlockEntity pad) {
            pad.dropContents();
        }
        DummyablePlacement.beginDismantle();
        try {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos cell = core.offset(dx, 0, dz);
                    if (cell.equals(pos)) {
                        continue;
                    }
                    if (level.getBlockState(cell).is(this)) {
                        level.removeBlock(cell, false);
                    }
                }
            }
        } finally {
            DummyablePlacement.endDismantle();
        }
        if (state.hasBlockEntity() && !newState.is(this)) {
            level.removeBlockEntity(pos);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }
}
