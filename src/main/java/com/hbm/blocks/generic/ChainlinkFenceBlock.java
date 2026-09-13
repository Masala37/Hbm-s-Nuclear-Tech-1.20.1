package com.hbm.blocks.generic;

import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Chainlink fence / pole (legacy {@code BlockMetalFence} meta 0 and 1).
 */
public class ChainlinkFenceBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty POST = BooleanProperty.create("post");
    public static final BooleanProperty POLE = BooleanProperty.create("pole");

    private static final VoxelShape[] COLLISION = buildCollision();

    public ChainlinkFenceBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(15.0F, 0.25F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(POST, false)
                .setValue(POLE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, POST, POLE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connections(context.getLevel(), context.getClickedPos(), false);
    }

    public BlockState withForcedPost(BlockState state, boolean post) {
        return applyPole(state.setValue(POST, post));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal()) {
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), canConnectFenceTo(level, neighborPos));
            return applyPole(state);
        }
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION[index(state)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION[index(state)];
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(state.getValue(POST) ? ModItems.FENCE_METAL_POST.get() : ModItems.FENCE_METAL.get());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return applyPole(switch (rotation) {
            case CLOCKWISE_180 -> state
                    .setValue(NORTH, state.getValue(SOUTH))
                    .setValue(EAST, state.getValue(WEST))
                    .setValue(SOUTH, state.getValue(NORTH))
                    .setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> state
                    .setValue(NORTH, state.getValue(EAST))
                    .setValue(EAST, state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(WEST))
                    .setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90 -> state
                    .setValue(NORTH, state.getValue(WEST))
                    .setValue(EAST, state.getValue(NORTH))
                    .setValue(SOUTH, state.getValue(EAST))
                    .setValue(WEST, state.getValue(SOUTH));
            default -> state;
        });
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return applyPole(switch (mirror) {
            case LEFT_RIGHT -> state
                    .setValue(NORTH, state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state
                    .setValue(EAST, state.getValue(WEST))
                    .setValue(WEST, state.getValue(EAST));
            default -> state;
        });
    }

    public boolean canConnectFenceTo(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof ChainlinkFenceBlock) {
            return true;
        }
        if (block instanceof FenceGateBlock) {
            return true;
        }
        if (block instanceof StemGrownBlock || block instanceof CarvedPumpkinBlock) {
            return false;
        }
        return state.isSolidRender(level, pos);
    }

    private BlockState connections(BlockGetter level, BlockPos pos, boolean post) {
        boolean north = canConnectFenceTo(level, pos.north());
        boolean east = canConnectFenceTo(level, pos.east());
        boolean south = canConnectFenceTo(level, pos.south());
        boolean west = canConnectFenceTo(level, pos.west());
        return applyPole(defaultBlockState()
                .setValue(NORTH, north)
                .setValue(EAST, east)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                .setValue(POST, post));
    }

    private static BlockState applyPole(BlockState state) {
        return state.setValue(POLE, ChainlinkFence.showPole(
                state.getValue(POST),
                state.getValue(NORTH),
                state.getValue(EAST),
                state.getValue(SOUTH),
                state.getValue(WEST)));
    }

    private static int index(BlockState state) {
        int i = 0;
        if (state.getValue(NORTH)) {
            i |= 1;
        }
        if (state.getValue(EAST)) {
            i |= 2;
        }
        if (state.getValue(SOUTH)) {
            i |= 4;
        }
        if (state.getValue(WEST)) {
            i |= 8;
        }
        return i;
    }

    private static VoxelShape[] buildCollision() {
        VoxelShape[] shapes = new VoxelShape[16];
        for (int i = 0; i < 16; i++) {
            boolean north = (i & 1) != 0;
            boolean east = (i & 2) != 0;
            boolean south = (i & 4) != 0;
            boolean west = (i & 8) != 0;
            VoxelShape shape = Shapes.empty();
            if (ChainlinkFence.usesNorthSouthBox(north, south)) {
                shape = Shapes.or(shape, Block.box(6.0D, 0.0D, north ? 0.0D : 6.0D, 10.0D, 16.0D, south ? 16.0D : 10.0D));
            }
            if (ChainlinkFence.usesEastWestBox(north, east, south, west)) {
                shape = Shapes.or(shape, Block.box(west ? 0.0D : 6.0D, 0.0D, 6.0D, east ? 16.0D : 10.0D, 16.0D, 10.0D));
            }
            shapes[i] = shape;
        }
        return shapes;
    }
}
