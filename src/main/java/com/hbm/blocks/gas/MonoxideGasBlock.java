package com.hbm.blocks.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Carbon monoxide cloud from volcano ejecta (legacy {@code BlockGasMonoxide}).
 */
public class MonoxideGasBlock extends GasBlock {
    @Override
    protected Direction firstDirection(Level level, BlockPos pos, RandomSource random) {
        return Direction.DOWN;
    }

    @Override
    protected Direction secondDirection(Level level, BlockPos pos, RandomSource random) {
        return randomHorizontal(random);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.removeBlock(pos, false);
            return;
        }
        super.tick(state, level, pos, random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }
        // Gas-mask filter hook deferred; apply poison as stand-in for monoxide damage.
        living.hurt(level.damageSources().magic(), 1.0F);
        living.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, true, false));
        living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 0, true, false));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.02D, 0.0D);
        }
    }
}
