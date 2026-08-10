package com.hbm.blocks.bomb;

import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Flowing volcanic / radioactive lava (legacy {@code VolcanicBlock} / {@code RadBlock}).
 */
public class VolcanicLavaBlock extends LiquidBlock {
    private final boolean radioactive;

    public VolcanicLavaBlock(Supplier<? extends FlowingFluid> fluid, boolean radioactive) {
        super(fluid, BlockBehaviour.Properties.of()
                .mapColor(radioactive ? MapColor.COLOR_LIGHT_GREEN : MapColor.FIRE)
                .replaceable()
                .noCollission()
                .randomTicks()
                .strength(100.0F)
                .lightLevel(state -> 15)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY));
        this.radioactive = radioactive;
    }

    public boolean isRadioactive() {
        return radioactive;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) {
            return;
        }
        for (Direction dir : Direction.values()) {
            BlockPos target = pos.relative(dir);
            Block replacement = reactionFor(level, target);
            if (replacement != null) {
                level.setBlock(target, replacement.defaultBlockState(), 3);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);

        int lavaCount = 0;
        int basaltCount = 0;
        Block basalt = basaltForCheck();
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (neighbor.is(this)) {
                lavaCount++;
            }
            if (neighbor.is(basalt)) {
                basaltCount++;
            }
        }

        boolean source = state.getValue(LEVEL) == 0;
        BlockState below = level.getBlockState(pos.below());
        if (((!source && lavaCount < 2) || (random.nextInt(5) == 0 && lavaCount < 5)) && !below.is(this)) {
            solidify(level, pos, lavaCount, basaltCount, random);
        }
    }

    protected Block basaltForCheck() {
        return radioactive ? ModBlocks.SELLAFIELD_SLAKED.get() : ModBlocks.BASALT.get();
    }

    protected void solidify(ServerLevel level, BlockPos pos, int lavaCount, int basaltCount, RandomSource random) {
        if (radioactive) {
            int r = random.nextInt(400);
            BlockState above = level.getBlockState(pos.above(10));
            boolean canMakeGem = lavaCount + basaltCount == 6 && lavaCount < 3
                    && (above.is(ModBlocks.SELLAFIELD_SLAKED.get()) || above.is(this));
            if (r < 2) {
                level.setBlock(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), 3);
            } else if (r == 2) {
                level.setBlock(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), 3);
            } else if (r < 20 && canMakeGem) {
                level.setBlock(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), 3);
            } else {
                level.setBlock(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), 3);
            }
            return;
        }

        int r = random.nextInt(200);
        BlockState above = level.getBlockState(pos.above(10));
        boolean canMakeGem = lavaCount + basaltCount == 6 && lavaCount < 3
                && (above.is(ModBlocks.BASALT.get()) || above.is(this));
        if (r < 2) {
            level.setBlock(pos, ModBlocks.ORE_BASALT_SULFUR.get().defaultBlockState(), 3);
        } else if (r == 2) {
            level.setBlock(pos, ModBlocks.ORE_BASALT_ASBESTOS.get().defaultBlockState(), 3);
        } else if (r == 3) {
            level.setBlock(pos, ModBlocks.ORE_BASALT_FLUORITE.get().defaultBlockState(), 3);
        } else if (r == 4) {
            level.setBlock(pos, ModBlocks.ORE_BASALT_MOLYSITE.get().defaultBlockState(), 3);
        } else if (r < 15 && canMakeGem) {
            level.setBlock(pos, ModBlocks.ORE_BASALT_GEM.get().defaultBlockState(), 3);
        } else {
            level.setBlock(pos, ModBlocks.BASALT.get().defaultBlockState(), 3);
        }
    }

    protected Block reactionFor(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getFluidState().isSource() && state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)
                || state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON)) {
            return Blocks.STONE;
        }
        if (state.is(BlockTags.LOGS)) {
            Block waste = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("hbm", "waste_log"));
            return waste != null && waste != Blocks.AIR ? waste : Blocks.COAL_BLOCK;
        }
        if (state.is(BlockTags.PLANKS)) {
            return ModBlocks.WASTE_PLANKS.get();
        }
        if (state.is(BlockTags.LEAVES)) {
            return Blocks.FIRE;
        }
        if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
            return radioactive ? ModBlocks.SELLAFIELD_SLAKED.get() : ModBlocks.ORE_BASALT_GEM.get();
        }
        return null;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.setSecondsOnFire(15);
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.hurt(level.damageSources().lava(), radioactive ? 2.0F : 1.0F);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (level.getBlockState(above).isAir()) {
            if (random.nextInt(100) == 0) {
                double dx = pos.getX() + random.nextDouble();
                double dy = pos.getY() + 1.0D;
                double dz = pos.getZ() + random.nextDouble();
                level.addParticle(ParticleTypes.LAVA, dx, dy, dz, 0.0D, 0.0D, 0.0D);
                level.playLocalSound(dx, dy, dz, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                        0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
            if (random.nextInt(200) == 0) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS,
                        0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }
        if (random.nextInt(10) == 0 && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            level.addParticle(ParticleTypes.DRIPPING_LAVA,
                    pos.getX() + random.nextDouble(),
                    pos.getY() - 0.05D,
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }
}
