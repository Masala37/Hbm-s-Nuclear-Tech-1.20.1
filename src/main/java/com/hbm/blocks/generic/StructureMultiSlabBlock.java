package com.hbm.blocks.generic;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7 {@code BlockMultiSlab}: variant in meta bits 0–2, top in bit 3.
 */
public class StructureMultiSlabBlock extends SlabBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 7);

    public StructureMultiSlabBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(15.0F, 100.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
        registerDefaultState(defaultBlockState().setValue(VARIANT, 0));
    }

    public BlockState fromMeta(int meta, boolean doubles) {
        int variant = meta & 7;
        boolean top = (meta & 8) != 0;
        SlabType type = doubles ? SlabType.DOUBLE : (top ? SlabType.TOP : SlabType.BOTTOM);
        return defaultBlockState().setValue(VARIANT, variant).setValue(TYPE, type);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VARIANT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState placed = super.getStateForPlacement(context);
        if (placed == null) {
            return null;
        }
        return placed.setValue(VARIANT, 0);
    }
}
