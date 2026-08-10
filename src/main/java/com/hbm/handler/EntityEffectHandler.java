package com.hbm.handler;

import com.hbm.capability.HbmLivingProps;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.lib.RefStrings;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Living-entity radiation / digamma intake + sickness.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class EntityEffectHandler {
    private EntityEffectHandler() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide) {
            return;
        }

        boolean contam = RadiationConfig.enableContamination == null || RadiationConfig.enableContamination.get();
        if (contam) {
            handleChunkContamination(entity);
            if (entity.tickCount % 20 == 0) {
                HbmLivingProps.setRadBuf(entity, HbmLivingProps.getRadEnv(entity));
                HbmLivingProps.setRadEnv(entity, 0.0F);
            }
            handleSickness(entity);
        }

        handleDigamma(entity);

        if (entity instanceof ServerPlayer player && entity.tickCount % 20 == 0) {
            HbmLivingProps.get(player).syncIfNeeded(player);
        }
    }

    private static void handleChunkContamination(LivingEntity entity) {
        if (ContaminationUtil.isRadImmune(entity)) {
            return;
        }
        float rad = ChunkRadiationManager.INSTANCE.getRadiation(
                entity.level(),
                (int) Math.floor(entity.getX()),
                (int) Math.floor(entity.getY()),
                (int) Math.floor(entity.getZ()));
        if (rad > 0.0F) {
            ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, rad / 20.0F);
        }
    }

    private static void handleDigamma(LivingEntity entity) {
        if (RadiationConfig.enableDigamma != null && !RadiationConfig.enableDigamma.get()) {
            return;
        }
        float digamma = HbmLivingProps.getDigamma(entity);
        if (digamma < 0.01F) {
            return;
        }
        HbmLivingProps.get(entity).applyDigammaHealth(entity);
        if (entity.level() instanceof ServerLevel server && entity.tickCount % 40 == 0 && digamma >= 0.05F) {
            server.sendParticles(ParticleTypes.SOUL,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    1, 0.2D, 0.2D, 0.2D, 0.01D);
        }
    }

    private static void handleSickness(LivingEntity entity) {
        float eRad = HbmLivingProps.getRadiation(entity);
        if (eRad < 200.0F || ContaminationUtil.isRadImmune(entity)) {
            return;
        }
        if (eRad > 2500.0F) {
            HbmLivingProps.setRadiation(entity, 2500.0F);
            eRad = 2500.0F;
        }

        Level level = entity.level();
        if (eRad >= 1000.0F) {
            entity.hurt(level.damageSources().magic(), 1000.0F);
            HbmLivingProps.setRadiation(entity, 0.0F);
            if (entity.getHealth() > 0.0F) {
                entity.setHealth(0.0F);
            }
            return;
        }

        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return;
        }

        if (eRad >= 800.0F) {
            maybeEffect(level, entity, MobEffects.CONFUSION, 150, 0, 300);
            maybeEffect(level, entity, MobEffects.MOVEMENT_SLOWDOWN, 200, 2, 300);
            maybeEffect(level, entity, MobEffects.WEAKNESS, 200, 2, 300);
            maybeEffect(level, entity, MobEffects.POISON, 60, 2, 500);
            maybeEffect(level, entity, MobEffects.WITHER, 60, 1, 700);
        } else if (eRad >= 600.0F) {
            maybeEffect(level, entity, MobEffects.CONFUSION, 150, 0, 300);
            maybeEffect(level, entity, MobEffects.MOVEMENT_SLOWDOWN, 200, 2, 300);
            maybeEffect(level, entity, MobEffects.WEAKNESS, 200, 2, 300);
            maybeEffect(level, entity, MobEffects.POISON, 60, 1, 500);
        } else if (eRad >= 400.0F) {
            maybeEffect(level, entity, MobEffects.CONFUSION, 150, 0, 300);
            maybeEffect(level, entity, MobEffects.MOVEMENT_SLOWDOWN, 100, 0, 500);
            maybeEffect(level, entity, MobEffects.WEAKNESS, 100, 1, 300);
        } else {
            maybeEffect(level, entity, MobEffects.CONFUSION, 100, 0, 300);
            maybeEffect(level, entity, MobEffects.WEAKNESS, 100, 0, 500);
        }
    }

    private static void maybeEffect(Level level, LivingEntity entity,
                                    net.minecraft.world.effect.MobEffect effect,
                                    int duration, int amplifier, int chance) {
        if (level.random.nextInt(chance) == 0) {
            entity.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }
}
