package com.hbm.blocks.network;

import com.hbm.blockentity.network.FluidPipeBlockEntity;
import com.hbm.blockentity.network.FluidValveBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 {@code FluidSwitch}: redstone opens the duct graph.
 */
public class FluidSwitchBlock extends BaseEntityBlock implements ILookOverlay {
    public FluidSwitchBlock() {
        super(FluidDucts.properties());
        registerDefaultState(stateDefinition.any().setValue(FluidDucts.OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FluidDucts.OPEN);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidValveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.FLUID_VALVE.get(), FluidPipeBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FluidPipeBlockEntity pipe)) {
            return InteractionResult.PASS;
        }
        return FluidDucts.trySetType(level, player.getItemInHand(hand), pipe);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            neighborChanged(state, level, pos, this, pos, false);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        FluidDucts.setOpen(level, pos, state, level.hasNeighborSignal(pos));
    }

    @Override
    public void printHook(Level level, BlockPos pos, List<Component> lines) {
        FluidDucts.overlayType(level.getBlockEntity(pos), lines);
    }
}
