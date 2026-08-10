package com.hbm.blocks.fluid;

import com.hbm.registry.ModBlocks;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * Hot toxic sludge (legacy {@code ToxicBlock} / {@code toxic_block}).
 */
public class ToxicBlock extends LiquidBlock {
    public ToxicBlock(Supplier<? extends FlowingFluid> fluid) {
        super(fluid, BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GREEN)
                .replaceable()
                .noCollission()
                .strength(100.0F)
                .lightLevel(state -> 15)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (living instanceof Player player && player.getAbilities().instabuild) {
            return;
        }
        ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.HAZMAT2, 2.5F);
        living.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 1, true, false));
        living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, true, false));
        living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1, true, false));
        living.hurt(level.damageSources().magic(), 1.0F);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) {
            return;
        }
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState other = level.getBlockState(neighbor);
            if (other.getFluidState().isEmpty()) {
                continue;
            }
            if (other.getFluidState().getType().isSame(getFluid())) {
                continue;
            }
            level.setBlock(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), 3);
            return;
        }
    }
}
