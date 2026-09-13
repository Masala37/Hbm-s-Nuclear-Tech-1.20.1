package com.hbm.blockentity.machine;

import com.hbm.HbmNuclearTechMod;
import com.hbm.handler.BroadcasterEffect;
import com.hbm.lib.ModDamageSource;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Nausea aura, armor-bypassing broadcast damage, and client loop (legacy {@code TileEntityBroadcaster}).
 */
public class BroadcasterBlockEntity extends BlockEntity {
    public BroadcasterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BROADCASTER_PC.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BroadcasterBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 0.5D;
        double cz = pos.getZ() + 0.5D;
        AABB box = new AABB(
                cx - BroadcasterEffect.NAUSEA_RANGE,
                cy - BroadcasterEffect.NAUSEA_RANGE,
                cz - BroadcasterEffect.NAUSEA_RANGE,
                cx + BroadcasterEffect.NAUSEA_RANGE,
                cy + BroadcasterEffect.NAUSEA_RANGE,
                cz + BroadcasterEffect.NAUSEA_RANGE);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box)) {
            double distance = Math.sqrt(living.distanceToSqr(cx, cy, cz));
            if (BroadcasterEffect.inNauseaRange(distance)) {
                MobEffectInstance current = living.getEffect(MobEffects.CONFUSION);
                int remaining = current == null ? 0 : current.getDuration();
                if (current == null || BroadcasterEffect.shouldRefreshNausea(remaining)) {
                    living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, BroadcasterEffect.NAUSEA_DURATION, 0));
                }
            }
            float damage = BroadcasterEffect.damageAt(distance);
            if (damage > 0.0F) {
                living.hurt(ModDamageSource.broadcast(level), damage);
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BroadcasterBlockEntity be) {
        HbmNuclearTechMod.proxy.tickBroadcaster(level, pos);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = worldPosition;
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 2.0D, pos.getZ() + 1.0D);
    }
}
