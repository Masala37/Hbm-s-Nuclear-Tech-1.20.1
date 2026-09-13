package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** 1.7 {@code BlockPipe}: decorative pipe along an axis. */
public class DecoPipeBlock extends RotatedPillarBlock {
    private static final VoxelShape Y = Block.box(5, 0, 5, 11, 16, 11);
    private static final VoxelShape X = Block.box(0, 5, 5, 16, 11, 11);
    private static final VoxelShape Z = Block.box(5, 5, 0, 11, 11, 16);
    private static final VoxelShape Y_QUAD = Block.box(2, 0, 2, 14, 16, 14);
    private static final VoxelShape X_QUAD = Block.box(0, 2, 2, 16, 14, 14);
    private static final VoxelShape Z_QUAD = Block.box(2, 2, 0, 14, 14, 16);

    private final int pipeType;

    public DecoPipeBlock(int pipeType) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 5.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        this.pipeType = pipeType;
    }

    public int pipeType() {
        return pipeType;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean bulky = pipeType >= 2;
        Direction.Axis axis = state.getValue(AXIS);
        if (axis == Direction.Axis.X) {
            return bulky ? X_QUAD : X;
        }
        if (axis == Direction.Axis.Z) {
            return bulky ? Z_QUAD : Z;
        }
        return bulky ? Y_QUAD : Y;
    }
}
