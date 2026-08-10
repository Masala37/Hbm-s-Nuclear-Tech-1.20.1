package com.hbm.blocks.gas;

import com.hbm.lib.RefStrings;
import com.hbm.registry.ModBlocks;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Radon gas variants (legacy {@code BlockGasRadon} / Dense / Tomb).
 * Full contamination deferred; applies poison/hunger as a stand-in hazard.
 */
public class RadonGasBlock extends GasBlock {
    public enum Kind {
        NORMAL,
        DENSE,
        TOMB
    }

    private final Kind kind;

    public RadonGasBlock(Kind kind) {
        this.kind = kind;
    }

    public static RadonGasBlock normal() {
        return new RadonGasBlock(Kind.NORMAL);
    }

    public static RadonGasBlock dense() {
        return new RadonGasBlock(Kind.DENSE);
    }

    public static RadonGasBlock tomb() {
        return new RadonGasBlock(Kind.TOMB);
    }

    @Override
    protected Direction firstDirection(Level level, BlockPos pos, RandomSource random) {
        int chance = kind == Kind.TOMB ? 3 : 5;
        if (random.nextInt(chance) == 0) {
            return Direction.UP;
        }
        return Direction.DOWN;
    }

    @Override
    protected Direction secondDirection(Level level, BlockPos pos, RandomSource random) {
        return randomHorizontal(random);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        switch (kind) {
            case NORMAL -> {
                if (random.nextInt(50) == 0) {
                    level.removeBlock(pos, false);
                    return;
                }
            }
            case DENSE -> {
                if (random.nextInt(20) == 0) {
                    BlockPos below = pos.below();
                    if (level.getBlockState(below).is(Blocks.GRASS_BLOCK)) {
                        level.setBlock(below, ModBlocks.WASTE_EARTH.get().defaultBlockState(), 3);
                    }
                }
                if (random.nextInt(30) == 0) {
                    level.removeBlock(pos, false);
                    Block fallout = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(RefStrings.MODID, "fallout"));
                    if (fallout != null && fallout != Blocks.AIR
                            && fallout.defaultBlockState().canSurvive(level, pos)) {
                        level.setBlock(pos, fallout.defaultBlockState(), 3);
                    }
                    return;
                }
            }
            case TOMB -> {
                if (random.nextInt(10) == 0) {
                    BlockPos below = pos.below();
                    BlockState under = level.getBlockState(below);
                    if (under.is(Blocks.GRASS_BLOCK)) {
                        if (random.nextInt(5) == 0) {
                            level.setBlock(below, Blocks.COARSE_DIRT.defaultBlockState(), 3);
                        } else {
                            level.setBlock(below, ModBlocks.WASTE_EARTH.get().defaultBlockState(), 3);
                        }
                    } else if (under.is(net.minecraft.tags.BlockTags.LEAVES)
                            || under.is(net.minecraft.tags.BlockTags.FLOWERS)
                            || under.is(net.minecraft.tags.BlockTags.REPLACEABLE)) {
                        if (!under.isSolidRender(level, below)) {
                            level.removeBlock(below, false);
                        }
                    }
                }
                if (random.nextInt(600) == 0) {
                    level.removeBlock(pos, false);
                    return;
                }
            }
        }
        super.tick(state, level, pos, random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }
        float dose = switch (kind) {
            case NORMAL -> 0.5F;
            case DENSE -> 2.0F;
            case TOMB -> 5.0F;
        };
        ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.HAZMAT2, dose);
        switch (kind) {
            case NORMAL -> {
                living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 0, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, true, false));
            }
            case DENSE -> {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 15 * 20, 0, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1, true, false));
            }
            case TOMB -> {
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 2, true, false));
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (kind == Kind.DENSE) {
            level.addParticle(ParticleTypes.MYCELIUM,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.0D, 0.0D);
        } else {
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.02D, 0.0D);
        }
    }
}
