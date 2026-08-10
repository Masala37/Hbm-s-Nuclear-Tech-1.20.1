package com.hbm.blocks.bomb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.joml.Vector3f;

/**
 * Eternal green fire residue (legacy {@code Balefire} extends {@code BlockFire}).
 * No random ticks — does not extinguish like vanilla fire.
 * Contact applies fire + a radiation stand-in (legacy {@code HbmPotion.radiation}).
 */
public class BalefireBlock extends BaseFireBlock {
    private static final DustParticleOptions GREEN_DUST =
            new DustParticleOptions(new Vector3f(0.35F, 1.0F, 0.25F), 1.0F);

    public BalefireBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GREEN)
                .replaceable()
                .noCollission()
                .instabreak()
                .lightLevel(state -> 15)
                .sound(SoundType.WOOL)
                .pushReaction(PushReaction.DESTROY), 1.0F);
        // BaseFireBlock has no AGE (unlike FireBlock); keep a single default state.
        registerDefaultState(stateDefinition.any());
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return !state.isAir();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState floor = level.getBlockState(below);
        return floor.isFaceSturdy(level, below, Direction.UP) || canBurn(floor);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        // Persist even if neighbors change; only drop if the floor vanishes.
        return facing == Direction.DOWN && !canSurvive(state, level, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : state;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                    1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
        }

        for (int i = 0; i < 2; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.5D + 0.5D;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(GREEN_DUST, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.05D,
                    random.nextDouble() * 0.08D,
                    (random.nextDouble() - 0.5D) * 0.05D);
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.setSecondsOnFire(10);
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.hurt(level.damageSources().inFire(), 1.0F);
            // Legacy Balefire: radiation 5s @ amplifier 9 — stand-in until ContamUtil lands.
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 5 * 20, 2, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 5 * 20, 1, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 5 * 20, 1, true, false));
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
