package com.hbm.blocks.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Air-like drifting gas (legacy {@code BlockGasBase}). Invisible cube; particles only.
 */
public abstract class GasBlock extends Block {
    protected GasBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .replaceable()
                .noCollission()
                .noOcclusion()
                .instabreak()
                .sound(SoundType.EMPTY)
                .pushReaction(PushReaction.DESTROY)
                .isViewBlocking((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isRedstoneConductor((state, level, pos) -> false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Full cube so entityInside still fires while collision stays empty.
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
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
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 10);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 10);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!tryMove(level, pos, firstDirection(level, pos, random))
                && !tryMove(level, pos, secondDirection(level, pos, random))) {
            level.scheduleTick(pos, this, delay());
        }
    }

    protected abstract Direction firstDirection(Level level, BlockPos pos, RandomSource random);

    protected Direction secondDirection(Level level, BlockPos pos, RandomSource random) {
        return firstDirection(level, pos, random);
    }

    protected int delay() {
        return 2;
    }

    protected boolean tryMove(ServerLevel level, BlockPos pos, Direction dir) {
        BlockPos target = pos.relative(dir);
        if (level.getBlockState(target).isAir()) {
            level.removeBlock(pos, false);
            level.setBlock(target, defaultBlockState(), 3);
            level.scheduleTick(target, this, delay());
            return true;
        }
        return false;
    }

    protected static Direction randomHorizontal(RandomSource random) {
        return Direction.Plane.HORIZONTAL.getRandomDirection(random);
    }

    @Override
    public abstract void entityInside(BlockState state, Level level, BlockPos pos, Entity entity);
}
