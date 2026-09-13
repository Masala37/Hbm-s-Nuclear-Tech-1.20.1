package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.CableDiodeBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.registry.ModBlockEntities;
import api.hbm.block.IToolable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One-way FE relay: receives from all sides except output, pushes only toward FACING.
 */
public class CableDiodeBlock extends BaseEntityBlock implements IToolable, ILookOverlay {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(5, 5, 0, 11, 11, 16),
            Block.box(0, 5, 5, 16, 11, 11),
            Block.box(5, 0, 5, 11, 16, 11));

    public CableDiodeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(0.5F, 2.0F)
                .sound(SoundType.METAL)
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableDiodeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.CABLE_DIODE.get(), CableDiodeBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        InteractionResult tooled = IToolable.tryUse(level, player, hand, hit, this);
        if (tooled.consumesAction() || tooled == InteractionResult.SUCCESS) {
            return tooled;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CableDiodeBlockEntity diode) {
            player.displayClientMessage(Component.translatable("block.hbm.cable_diode.throughput",
                    diode.transferRate(), diode.getThroughputLevel()).withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onScrew(Level level, Player player, BlockPos pos, Direction side, float hitX, float hitY, float hitZ,
                           ToolType tool) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CableDiodeBlockEntity diode)) {
            return false;
        }
        if (tool == ToolType.SCREWDRIVER) {
            return diode.raiseThroughput();
        }
        if (tool == ToolType.HAND_DRILL) {
            return diode.lowerThroughput();
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.hbm.cable_diode.desc").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("block.hbm.cable_diode.desc.screw").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("block.hbm.cable_diode.desc.drill").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void printHook(Level level, BlockPos pos, List<Component> lines) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CableDiodeBlockEntity diode) {
            lines.add(Component.translatable("block.hbm.cable_diode.throughput",
                    diode.transferRate(), diode.getThroughputLevel()));
        }
    }
}
