package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** 1.7 {@code BlockWoodStructure}: roof, scaffold, ceiling. */
public class WoodStructureBlock extends Block {
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    private static final VoxelShape ROOF = Block.box(0, 0, 0, 16, 3, 16);
    private static final VoxelShape SCAFFOLD = Block.box(1, 0, 1, 15, 16, 15);
    private static final VoxelShape CEILING = Block.box(0, 14, 0, 16, 16, 16);

    public WoodStructureBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(5.0F, 15.0F)
                .sound(SoundType.WOOD)
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(VARIANT, Variant.ROOF));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(VARIANT)) {
            case SCAFFOLD -> SCAFFOLD;
            case CEILING -> CEILING;
            default -> ROOF;
        };
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return state.getValue(VARIANT) == Variant.SCAFFOLD;
    }

    public enum Variant implements StringRepresentable {
        ROOF,
        SCAFFOLD,
        CEILING;

        public static Variant byMeta(int meta) {
            Variant[] values = values();
            return values[Math.max(0, Math.min(meta, values.length - 1))];
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
