package com.hbm.blocks.generic;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/** 1.7 {@code BlockConcreteColored}: wool-order dye meta. */
public class ColoredConcreteBlock extends Block {
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    public ColoredConcreteBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(state -> state.getValue(COLOR).getMapColor())
                .strength(15.0F, 140.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
        registerDefaultState(stateDefinition.any().setValue(COLOR, DyeColor.WHITE));
    }

    public static DyeColor fromMeta(int meta) {
        DyeColor[] values = DyeColor.values();
        int id = meta & 15;
        return id < values.length ? values[id] : DyeColor.WHITE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }
}
