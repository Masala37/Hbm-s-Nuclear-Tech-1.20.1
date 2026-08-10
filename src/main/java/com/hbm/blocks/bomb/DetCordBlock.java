package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.api.bomb.IDetConnectible;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;

/**
 * Detonation cord (legacy {@code DetCord}): thin connectible fuse that chains via blast.
 */
public class DetCordBlock extends Block implements IBomb, IDetConnectible {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    private static final VoxelShape CORE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    private static final VoxelShape ARM_N = Block.box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 5.0D);
    private static final VoxelShape ARM_S = Block.box(5.0D, 5.0D, 11.0D, 11.0D, 11.0D, 16.0D);
    private static final VoxelShape ARM_W = Block.box(0.0D, 5.0D, 5.0D, 5.0D, 11.0D, 11.0D);
    private static final VoxelShape ARM_E = Block.box(11.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
    private static final VoxelShape ARM_D = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D);
    private static final VoxelShape ARM_U = Block.box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D);

    public DetCordBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(0.1F, 0.0F)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .ignitedByLava());
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
                .setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return withConnections(state, level, pos);
    }

    private static BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        return state
                .setValue(NORTH, IDetConnectible.isConnectible(level, pos.north(), Direction.SOUTH))
                .setValue(SOUTH, IDetConnectible.isConnectible(level, pos.south(), Direction.NORTH))
                .setValue(WEST, IDetConnectible.isConnectible(level, pos.west(), Direction.EAST))
                .setValue(EAST, IDetConnectible.isConnectible(level, pos.east(), Direction.WEST))
                .setValue(UP, IDetConnectible.isConnectible(level, pos.above(), Direction.DOWN))
                .setValue(DOWN, IDetConnectible.isConnectible(level, pos.below(), Direction.UP));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_N);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_S);
        if (state.getValue(WEST)) shape = Shapes.or(shape, ARM_W);
        if (state.getValue(EAST)) shape = Shapes.or(shape, ARM_E);
        if (state.getValue(UP)) shape = Shapes.or(shape, ARM_U);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, ARM_D);
        return shape;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    1.5F, Level.ExplosionInteraction.TNT);
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity be, ItemStack tool) {
        if (!player.isCreative()) {
            popResource(level, pos, new ItemStack(this));
        }
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                1.5F, Level.ExplosionInteraction.TNT);
        return BombReturnCode.DETONATED;
    }
}
