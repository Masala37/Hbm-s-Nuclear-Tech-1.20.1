package com.hbm.blocks.generic;

import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Volcano fissure ore created by fissure bombs (legacy {@code BlockFissure} / {@code ore_volcano}).
 */
public class OreVolcanoBlock extends Block {
    public static final BooleanProperty CRATER = BooleanProperty.create("crater");

    public OreVolcanoBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3_600_000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 15)
                .randomTicks());
        registerDefaultState(stateDefinition.any().setValue(CRATER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CRATER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (level.getBlockState(above).canBeReplaced()) {
            BlockState lava = state.getValue(CRATER)
                    ? ModBlocks.RAD_LAVA.get().defaultBlockState()
                    : ModBlocks.VOLCANIC_LAVA.get().defaultBlockState();
            level.setBlock(above, lava, 3);
        }
    }
}
