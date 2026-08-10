package com.hbm.world.feature;

import com.hbm.HbmNuclearTechMod;
import com.hbm.blocks.bomb.CrashedBombBlock;
import com.hbm.blocks.bomb.DudType;
import com.hbm.config.GeneralConfig;
import com.hbm.config.WorldConfig;
import com.hbm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Surface crashed-bomb placement (legacy {@code Dud} WorldGenerator).
 */
public class DudFeature extends Feature<NoneFeatureConfiguration> {
    public DudFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (GeneralConfig.parseStructureFlag() == 0) {
            return false;
        }

        int chance = WorldConfig.dudStructure.get();
        if (chance <= 0 || context.random().nextInt(chance) != 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos place = origin;

        // Heightmap may land on snow/plants — nudge to air above solid ground.
        if (!level.getBlockState(place).isAir()) {
            place = place.above();
        }
        if (!isValidSpawn(level, place)) {
            return false;
        }

        DudType type = DudType.byOrdinal(context.random().nextInt(DudType.values().length));
        BlockState state = ModBlocks.CRASHED_BOMB.get().defaultBlockState()
                .setValue(CrashedBombBlock.TYPE, type);
        level.setBlock(place, state, 3);

        if (GeneralConfig.enableDebugMode.get()) {
            HbmNuclearTechMod.LOGGER.info("[Debug] Spawned dud ({}) at {} {} {}",
                    type.getSerializedName(), place.getX(), place.getY(), place.getZ());
        }
        return true;
    }

    private static boolean isValidSpawn(WorldGenLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        BlockState ground = level.getBlockState(pos.below());
        if (isSpawnSurface(ground)) {
            return true;
        }
        if (ground.is(Blocks.SNOW) || ground.is(BlockTags.REPLACEABLE) || ground.is(BlockTags.FLOWERS)
                || ground.is(BlockTags.SMALL_FLOWERS)) {
            return isSpawnSurface(level.getBlockState(pos.below(2)));
        }
        return false;
    }

    private static boolean isSpawnSurface(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.STONE)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.RED_SANDSTONE)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }
}
