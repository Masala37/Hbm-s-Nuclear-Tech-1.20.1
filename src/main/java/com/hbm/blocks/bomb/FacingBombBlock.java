package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.config.BombConfig;
import com.hbm.explosion.ExplosionNT;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

/**
 * Placeable bomb with horizontal facing (OBJ casings / multi-bomb).
 * Redstone detonates; nuclear variants use MK5 dig.
 */
public class FacingBombBlock extends Block implements IBomb {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final float power;
    private final boolean nuclear;
    private final IntSupplier nuclearRadius;
    private final VoxelShape shape;

    public FacingBombBlock(float power) {
        this(power, false, null, Shapes.block(), 5.0F, 200.0F);
    }

    public static FacingBombBlock nuclear(IntSupplier radius) {
        return new FacingBombBlock(0.0F, true, radius, Shapes.block(), 5.0F, 200.0F);
    }

    public static FacingBombBlock nuclear(IntSupplier radius, VoxelShape shape) {
        return new FacingBombBlock(0.0F, true, radius, shape, 5.0F, 200.0F);
    }

    private FacingBombBlock(float power, boolean nuclear, IntSupplier nuclearRadius, VoxelShape shape,
                            float hardness, float resistance) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        this.power = power;
        this.nuclear = nuclear;
        this.nuclearRadius = nuclearRadius;
        this.shape = shape;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        level.removeBlock(pos, false);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
        if (nuclear) {
            int radius = nuclearRadius != null ? nuclearRadius.getAsInt() : BombConfig.missileRadius.get();
            AssembledNukeBlock.ignite(level, pos, radius);
        } else {
            new ExplosionNT(level, null, x, y, z, power).explode();
        }
        return BombReturnCode.DETONATED;
    }
}
