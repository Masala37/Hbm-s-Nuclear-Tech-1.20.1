package com.hbm.blocks.generic;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/** 1.7 {@code BlockLightstone} / {@code LightstoneType}. */
public class LightstoneBlock extends Block {
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    public LightstoneBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(2.0F, 15.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
        registerDefaultState(stateDefinition.any().setValue(VARIANT, Variant.UNREFINED));
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

    public enum Variant implements StringRepresentable {
        UNREFINED,
        TILE,
        BRICKS,
        BRICKS_CHISELED,
        CHISELED;

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
