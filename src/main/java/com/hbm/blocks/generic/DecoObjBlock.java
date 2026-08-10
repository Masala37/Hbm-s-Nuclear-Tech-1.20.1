package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Decorative block rendered via a Forge OBJ model, with horizontal facing.
 */
public class DecoObjBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final VoxelShape shape;

    public DecoObjBlock(VoxelShape shape) {
        this(shape, MapColor.METAL, 2.0F, 5.0F, SoundType.METAL, 0);
    }

    public DecoObjBlock(VoxelShape shape, MapColor color, float hardness, float resistance, SoundType sound, int light) {
        super(BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
                .sound(sound)
                .noOcclusion()
                .lightLevel(state -> light));
        this.shape = shape;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public static DecoObjBlock lamp(VoxelShape shape, int light) {
        return new DecoObjBlock(shape, MapColor.METAL, 1.0F, 2.0F, SoundType.METAL, light);
    }

    public static DecoObjBlock anvil() {
        return new DecoObjBlock(Block.box(2, 0, 2, 14, 11, 14), MapColor.METAL, 5.0F, 1200.0F, SoundType.ANVIL, 0);
    }

    public static DecoObjBlock smallAppliance() {
        return new DecoObjBlock(Block.box(2, 0, 2, 14, 12, 14));
    }

    public static DecoObjBlock floorFixture(VoxelShape shape) {
        return new DecoObjBlock(shape, MapColor.METAL, 2.0F, 5.0F, SoundType.METAL, 0);
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
