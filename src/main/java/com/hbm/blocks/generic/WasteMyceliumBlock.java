package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Contaminated mycelium (legacy {@code WasteEarth} mycelium variant).
 */
public class WasteMyceliumBlock extends Block {
    public WasteMyceliumBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.6F)
                .sound(SoundType.GRASS)
                .lightLevel(s -> 1));
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            if (!(living instanceof Player player && player.getAbilities().instabuild)) {
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2, true, false));
                living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1, true, false));
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
