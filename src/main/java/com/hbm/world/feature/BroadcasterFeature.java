package com.hbm.world.feature;

import com.hbm.HbmNuclearTechMod;
import com.hbm.blocks.machine.PinkCloudBroadcasterBlock;
import com.hbm.config.GeneralConfig;
import com.hbm.config.WorldConfig;
import com.hbm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Surface corrupted-broadcaster placement (legacy {@code HbmWorldGen} broadcaster spawn).
 */
public class BroadcasterFeature extends Feature<NoneFeatureConfiguration> {
    public BroadcasterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int chance = WorldConfig.broadcaster.get();
        if (chance <= 0 || context.random().nextInt(chance) != 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos place = origin;
        if (!level.getBlockState(place).isAir()) {
            place = place.above();
        }
        if (!level.getBlockState(place).isAir()) {
            return false;
        }
        BlockPos groundPos = place.below();
        BlockState ground = level.getBlockState(groundPos);
        if (!ground.isFaceSturdy(level, groundPos, Direction.UP)) {
            return false;
        }

        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(context.random());
        BlockState state = ModBlocks.BROADCASTER_PC.get().defaultBlockState()
                .setValue(PinkCloudBroadcasterBlock.FACING, facing);
        level.setBlock(place, state, 3);

        if (GeneralConfig.enableDebugMode.get()) {
            HbmNuclearTechMod.LOGGER.info("[Debug] Successfully spawned corrupted broadcaster at {} {} {}",
                    place.getX(), place.getY(), place.getZ());
        }
        return true;
    }
}
