package com.hbm.blocks.generic;

import com.hbm.config.RadiationConfig;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.registry.ModBlocks;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.Tags;

/**
 * Live Sellafield waste rank 0–5: emits chunk rad, slowly decays toward gravel/sand by biome.
 */
public class SellafieldWasteBlock extends Block {
    private final int rank;

    public SellafieldWasteBlock(int rank) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GREEN)
                .strength(0.5F + rank * 0.15F)
                .sound(SoundType.GRAVEL)
                .randomTicks());
        this.rank = Math.max(0, Math.min(5, rank));
    }

    public int getRank() {
        return rank;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            scheduleNext((ServerLevel) level, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        float emit = 0.5F * (rank + 1);
        ChunkRadiationManager.INSTANCE.incrementRad(level, pos.getX(), pos.getY(), pos.getZ(), emit);

        int chance = rank > 0
                ? RadiationConfig.sellafieldDecayChanceHigh.get()
                : RadiationConfig.sellafieldDecayChanceZero.get();
        if (random.nextInt(Math.max(2, chance)) == 0) {
            level.setBlock(pos, nextState(level, pos), 3);
        } else {
            scheduleNext(level, pos);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Controlled pacing via scheduled ticks; randomTick is a light backup emit + reschedule.
        ChunkRadiationManager.INSTANCE.incrementRad(level, pos.getX(), pos.getY(), pos.getZ(), 0.1F * (rank + 1));
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            scheduleNext(level, pos);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.HAZMAT2,
                    0.5F * (rank + 1));
        }
        super.stepOn(level, pos, state, entity);
    }

    private void scheduleNext(ServerLevel level, BlockPos pos) {
        int interval = RadiationConfig.sellafieldTickInterval != null
                ? RadiationConfig.sellafieldTickInterval.get()
                : 6000;
        level.scheduleTick(pos, this, Math.max(20, interval));
    }

    private BlockState nextState(Level level, BlockPos pos) {
        if (rank > 0) {
            return blockForRank(rank - 1).defaultBlockState();
        }
        return terminalBlock(level, pos).defaultBlockState();
    }

    private static Block terminalBlock(Level level, BlockPos pos) {
        var holder = level.getBiome(pos);
        if (holder.is(BiomeTags.IS_BADLANDS)) {
            return Blocks.RED_SAND;
        }
        if (holder.is(Tags.Biomes.IS_DESERT)
                || (holder.is(Tags.Biomes.IS_DRY) && holder.is(Tags.Biomes.IS_HOT))) {
            return Blocks.SAND;
        }
        Biome biome = holder.value();
        if (biome.getBaseTemperature() >= 1.0F) {
            return Blocks.SAND;
        }
        return Blocks.GRAVEL;
    }

    public static Block blockForRank(int rank) {
        return switch (Math.max(0, Math.min(5, rank))) {
            case 1 -> ModBlocks.SELLAFIELD_1.get();
            case 2 -> ModBlocks.SELLAFIELD_2.get();
            case 3 -> ModBlocks.SELLAFIELD_3.get();
            case 4 -> ModBlocks.SELLAFIELD_4.get();
            case 5 -> ModBlocks.SELLAFIELD_5.get();
            default -> ModBlocks.SELLAFIELD_0.get();
        };
    }
}
