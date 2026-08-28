package com.hbm.effect;

import com.hbm.blocks.generic.TaintBlock;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Legacy {@code HbmPotion.taint}: periodic absolute-ish damage + optional trail infection.
 */
public class TaintMobEffect extends MobEffect {
    public TaintMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x800080);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (level.isClientSide) {
            return;
        }
        // Legacy: 1/40 chance per tick when ready (every 2 ticks) → ~every 80 ticks average.
        if (level.random.nextInt(40) == 0) {
            entity.hurt(level.damageSources().magic(), amplifier + 1.0F);
        }
        // Trail infection (legacy ServerConfig.TAINT_TRAILS default false — keep opt-in via always-on light trail
        // disabled; missile/block spread covers the main gameplay). Leave trail off by default.
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Legacy isReady: every 2 ticks
        return duration % 2 == 0;
    }

    /** Place aged taint under an entity (for trail / syringe helpers). */
    public static void infectGround(LivingEntity entity, int age) {
        Level level = entity.level();
        if (level.isClientSide) {
            return;
        }
        BlockPos below = entity.blockPosition().below();
        BlockState state = level.getBlockState(below);
        if (state.isAir() || state.getDestroySpeed(level, below) < 0.0F) {
            return;
        }
        if (!state.isSolidRender(level, below)) {
            return;
        }
        int clamped = Math.min(15, Math.max(0, age));
        level.setBlock(below, ModBlocks.TAINT.get().defaultBlockState()
                .setValue(TaintBlock.AGE, clamped), 2);
    }
}
