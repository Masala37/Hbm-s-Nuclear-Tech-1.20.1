package com.hbm.blocks.generic;

import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Lite taint spread (legacy decorative taint → live neighbor infection).
 */
public class TaintBlock extends Block {
    public TaintBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.8F)
                .sound(SoundType.STONE)
                .randomTicks());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) != 0) {
            return;
        }
        Direction dir = Direction.getRandom(random);
        BlockPos target = pos.relative(dir);
        BlockState other = level.getBlockState(target);
        if (other.isAir() || other.is(this) || other.getDestroySpeed(level, target) < 0.0F) {
            return;
        }
        // Soft natural blocks only
        if (other.is(Blocks.GRASS_BLOCK) || other.is(Blocks.DIRT) || other.is(Blocks.COARSE_DIRT)
                || other.is(Blocks.PODZOL) || other.is(Blocks.MYCELIUM)
                || other.is(Blocks.STONE) || other.is(Blocks.COBBLESTONE)
                || other.is(Blocks.SAND) || other.is(Blocks.GRAVEL)) {
            level.setBlock(target, defaultBlockState(), 3);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.HAZMAT, 1.0F);
        }
        super.stepOn(level, pos, state, entity);
    }
}
