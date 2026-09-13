package com.hbm.blocks.network;

import com.hbm.blockentity.network.FluidCounterValveBlockEntity;
import com.hbm.blockentity.network.FluidPipeBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
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
 * 1.7.10 {@code FluidCounterValve}: valve that totals millibuckets moved on its net.
 */
public class FluidCounterValveBlock extends BaseEntityBlock implements ILookOverlay {
    public FluidCounterValveBlock() {
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
        return new FluidCounterValveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.FLUID_COUNTER_VALVE.get(), FluidPipeBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FluidPipeBlockEntity pipe)) {
            return InteractionResult.PASS;
        }
        InteractionResult typed = FluidDucts.trySetType(level, player.getItemInHand(hand), pipe);
        if (typed.consumesAction() || typed == InteractionResult.SUCCESS) {
            return typed;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        // 1.7 only toggled this via OC/ROR; without OC the valve never opened.
        FluidDucts.setOpen(level, pos, state, !FluidDucts.isOpen(state));
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Beware of loops in your fluid network...").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void printHook(Level level, BlockPos pos, List<Component> lines) {
        BlockEntity be = level.getBlockEntity(pos);
        FluidDucts.overlayType(be, lines);
        if (be instanceof FluidCounterValveBlockEntity counter) {
            lines.add(Component.literal("Counter: " + counter.getCounter()));
        }
    }
}
