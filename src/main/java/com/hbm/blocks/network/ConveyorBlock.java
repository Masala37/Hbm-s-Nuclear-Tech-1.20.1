package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import com.hbm.conveyor.ConveyorTravel;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.items.tool.ConveyorWandItem;
import com.hbm.items.tool.ScrewdriverItem;
import com.hbm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 {@code BlockConveyor}: 0.25-high belt. {@link #FACING} is the 1.7 input metadata.
 */
public class ConveyorBlock extends Block implements IConveyorBelt {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty BEND = IntegerProperty.create("bend", 0, 2);
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);

    public ConveyorBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 2.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(BEND, ConveyorTravel.BEND_STRAIGHT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BEND);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public static Direction input(BlockState state) {
        return ConveyorTravel.input(state.getValue(FACING), state.getValue(BEND));
    }

    public static Direction output(BlockState state) {
        return ConveyorTravel.output(state.getValue(FACING), state.getValue(BEND));
    }

    @Override
    public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) {
        return true;
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) {
            return itemPos;
        }
        return ConveyorTravel.travelLocation(pos, state.getValue(FACING), state.getValue(BEND), itemPos, speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        BlockState state = level.getBlockState(pos);
        Direction travel = state.is(this)
                ? ConveyorTravel.travel(state.getValue(FACING), state.getValue(BEND), itemPos, pos)
                : Direction.NORTH;
        return ConveyorTravel.snap(pos, travel, itemPos);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        pickup(level, pos, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        pickup(level, pos, entity);
        super.stepOn(level, pos, state, entity);
    }

    private void pickup(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ItemEntity item) || item.isRemoved() || item.getAge() <= 10) {
            return;
        }
        EntityMovingItem moving = new EntityMovingItem(level);
        moving.setItemStack(item.getItem().copy());
        Vec3 snap = getClosestSnappingPosition(level, pos, entity.position());
        moving.setPos(snap.x, snap.y, snap.z);
        level.addFreshEntity(moving);
        item.discard();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof ScrewdriverItem) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            BlockState next;
            if (player.isShiftKeyDown()) {
                int bend = state.getValue(BEND);
                next = state.setValue(BEND, bend < 2 ? bend + 1 : 0);
            } else {
                next = state.setValue(FACING, state.getValue(FACING).getClockWise());
            }
            level.setBlock(pos, next, Block.UPDATE_ALL);
            return InteractionResult.CONSUME;
        }
        if (held.getItem() instanceof ConveyorWandItem) {
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.CONVEYOR_WAND.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.hbm.conveyor.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("block.hbm.conveyor.desc.screw").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("block.hbm.conveyor.desc.bend").withStyle(ChatFormatting.GRAY));
    }
}
