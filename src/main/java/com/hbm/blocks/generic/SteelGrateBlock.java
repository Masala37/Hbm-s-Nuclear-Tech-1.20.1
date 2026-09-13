package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7 {@code BlockGrate}: 2px-thick walkable grate whose Y offset is metadata 0–9.
 */
public class SteelGrateBlock extends Block {
    public static final IntegerProperty OFFSET = IntegerProperty.create("offset", 0, 9);

    private final VoxelShape[] shapes = new VoxelShape[10];
    private final boolean wide;

    public SteelGrateBlock(boolean wide) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 5.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        this.wide = wide;
        float thickness = wide ? 1.999F : 2.0F;
        for (int meta = 0; meta <= 9; meta++) {
            float y = meta == 9 ? -2.0F : meta * 2.0F;
            shapes[meta] = Block.box(0, y, 0, 16, y + thickness, 16);
        }
        registerDefaultState(stateDefinition.any().setValue(OFFSET, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face == Direction.DOWN) {
            return defaultBlockState().setValue(OFFSET, 7);
        }
        if (face == Direction.UP) {
            return defaultBlockState().setValue(OFFSET, 0);
        }
        double hit = context.getClickLocation().y - context.getClickedPos().getY();
        int offset = Mth.clamp((int) Math.floor(hit * 8.0D), 0, 7);
        return defaultBlockState().setValue(OFFSET, offset);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[state.getValue(OFFSET)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (wide && context instanceof EntityCollisionContext ecc) {
            Entity entity = ecc.getEntity();
            if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
                return Shapes.empty();
            }
        }
        return shapes[state.getValue(OFFSET)];
    }
}
