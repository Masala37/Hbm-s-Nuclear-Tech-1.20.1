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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Decorative block rendered via a Forge OBJ model, with horizontal facing.
 * Collision is specified for {@link Direction#NORTH} and rotated with facing.
 */
public class DecoObjBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final VoxelShape[] facingShapes = new VoxelShape[4];
    private final boolean decoModelMeta;

    public DecoObjBlock(VoxelShape northShape) {
        this(northShape, MapColor.METAL, 2.0F, 5.0F, SoundType.METAL, 0, false);
    }

    public DecoObjBlock(VoxelShape northShape, MapColor color, float hardness, float resistance, SoundType sound, int light) {
        this(northShape, color, hardness, resistance, sound, light, false);
    }

    public DecoObjBlock(VoxelShape northShape, MapColor color, float hardness, float resistance, SoundType sound, int light,
                        boolean decoModelMeta) {
        super(BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
                .sound(sound)
                .noOcclusion()
                .lightLevel(state -> light));
        this.decoModelMeta = decoModelMeta;
        Direction facing = Direction.NORTH;
        VoxelShape shape = northShape;
        for (int i = 0; i < 4; i++) {
            facingShapes[facing.get2DDataValue()] = shape;
            shape = rotateY90Cw(shape);
            facing = facing.getClockWise();
        }
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public boolean usesDecoModelMeta() {
        return decoModelMeta;
    }

    public static DecoObjBlock lamp(VoxelShape northShape, int light) {
        return new DecoObjBlock(northShape, MapColor.METAL, 1.0F, 2.0F, SoundType.METAL, light);
    }

    public static DecoObjBlock anvil() {
        return new DecoObjBlock(Block.box(2, 0, 2, 14, 11, 14), MapColor.METAL, 5.0F, 1200.0F, SoundType.ANVIL, 0);
    }

    public static DecoObjBlock smallAppliance() {
        return new DecoObjBlock(Block.box(2, 0, 2, 14, 12, 14));
    }

    /**
     * 1.7 {@code BlockDecoModel} facing packed in meta bits 2–3.
     * North AABB matches 1.7 rot 0 after the 180° world render.
     */
    public static DecoObjBlock decoModel() {
        return new DecoObjBlock(Block.box(2, 0, 6, 14, 14, 16), MapColor.METAL, 5.0F, 10.0F, SoundType.METAL, 0, true);
    }

    public static DecoObjBlock floorFixture(VoxelShape northShape) {
        return new DecoObjBlock(northShape, MapColor.METAL, 2.0F, 5.0F, SoundType.METAL, 0);
    }

    /**
     * Clockwise 90° around Y looking down: north face → east face.
     */
    static VoxelShape rotateY90Cw(VoxelShape shape) {
        AABB a = shape.bounds();
        return Shapes.box(1.0D - a.maxZ, a.minY, a.minX, 1.0D - a.minZ, a.maxY, a.maxX);
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
        return facingShapes[state.getValue(FACING).get2DDataValue()];
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
