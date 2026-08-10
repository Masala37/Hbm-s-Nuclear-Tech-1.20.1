package com.hbm.capability;

import com.hbm.config.RadiationConfig;
import com.hbm.network.LivingPropsSyncPacket;
import com.hbm.network.ModMessages;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Living-entity radiation / digamma dose (legacy {@code HbmLivingProps} subset).
 */
public class HbmLivingProps implements INBTSerializable<CompoundTag> {
    public static final Capability<HbmLivingProps> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final String KEY = "hbm_living_props";
    public static final UUID DIGAMMA_HEALTH_UUID = UUID.fromString("2a3d8aec-5ab9-4218-9b8b-ca812bdf378b");
    public static final ResourceLocation DIGAMMA_HEALTH_ID = new ResourceLocation("hbm", "digamma_health");

    private float radiation;
    private float radEnv;
    private float radBuf;
    private float digamma;
    private float lastSyncedRadiation = Float.NaN;
    private float lastSyncedDigamma = Float.NaN;

    public static HbmLivingProps get(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElseGet(HbmLivingProps::new);
    }

    public static float getRadiation(LivingEntity entity) {
        if (!contaminationEnabled()) {
            return 0.0F;
        }
        return get(entity).radiation;
    }

    public static void setRadiation(LivingEntity entity, float rad) {
        if (!contaminationEnabled()) {
            return;
        }
        HbmLivingProps props = get(entity);
        props.radiation = clampRad(rad);
        props.markDirty(entity);
    }

    public static void incrementRadiation(LivingEntity entity, float rad) {
        if (!contaminationEnabled()) {
            return;
        }
        setRadiation(entity, getRadiation(entity) + rad);
    }

    public static float getRadEnv(LivingEntity entity) {
        return get(entity).radEnv;
    }

    public static void setRadEnv(LivingEntity entity, float rad) {
        get(entity).radEnv = rad;
    }

    public static float getRadBuf(LivingEntity entity) {
        return get(entity).radBuf;
    }

    public static void setRadBuf(LivingEntity entity, float rad) {
        get(entity).radBuf = rad;
    }

    public static float getDigamma(LivingEntity entity) {
        if (!digammaEnabled()) {
            return 0.0F;
        }
        return get(entity).digamma;
    }

    public static void setDigamma(LivingEntity entity, float digamma) {
        if (!digammaEnabled()) {
            return;
        }
        HbmLivingProps props = get(entity);
        props.digamma = clampDigamma(digamma);
        props.applyDigammaHealth(entity);
        props.markDirty(entity);

        if (props.digamma >= 10.0F || entity.getMaxHealth() <= 0.0F) {
            entity.hurt(entity.level().damageSources().magic(), 1000.0F);
            if (entity.getHealth() > 0.0F) {
                entity.setHealth(0.0F);
            }
        }
    }

    public static void incrementDigamma(LivingEntity entity, float amount) {
        if (!digammaEnabled() || amount == 0.0F) {
            return;
        }
        setDigamma(entity, getDigamma(entity) + amount);
    }

    public void applyDigammaHealth(LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        maxHealth.removeModifier(DIGAMMA_HEALTH_UUID);
        if (digamma <= 0.0F) {
            return;
        }
        double healthMod = Math.pow(0.5D, digamma) - 1.0D;
        maxHealth.addTransientModifier(new AttributeModifier(
                DIGAMMA_HEALTH_UUID, "hbm Digamma", healthMod, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    public void syncIfNeeded(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (Float.isNaN(lastSyncedRadiation)
                || Math.abs(lastSyncedRadiation - radiation) > 0.05F
                || Math.abs(lastSyncedDigamma - digamma) > 0.001F) {
            sendSync(player);
        }
    }

    public void sendSync(ServerPlayer player) {
        lastSyncedRadiation = radiation;
        lastSyncedDigamma = digamma;
        ModMessages.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new LivingPropsSyncPacket(player.getId(), radiation, digamma, radBuf));
    }

    private void markDirty(LivingEntity entity) {
        if (entity instanceof ServerPlayer player && player.tickCount % 5 == 0) {
            sendSync(player);
        }
    }

    private static boolean contaminationEnabled() {
        return RadiationConfig.enableContamination == null || RadiationConfig.enableContamination.get();
    }

    private static boolean digammaEnabled() {
        return RadiationConfig.enableDigamma == null || RadiationConfig.enableDigamma.get();
    }

    private static float clampRad(float rad) {
        if (rad > 2500.0F) {
            return 2500.0F;
        }
        return Math.max(0.0F, rad);
    }

    private static float clampDigamma(float digamma) {
        if (digamma > 10.0F) {
            return 10.0F;
        }
        return Math.max(0.0F, digamma);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("radiation", radiation);
        tag.putFloat("radEnv", radEnv);
        tag.putFloat("radBuf", radBuf);
        tag.putFloat("digamma", digamma);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        radiation = tag.getFloat("radiation");
        radEnv = tag.getFloat("radEnv");
        radBuf = tag.getFloat("radBuf");
        digamma = tag.getFloat("digamma");
    }

    /** Client-side apply from sync packet. */
    public void applyClientSync(float radiation, float digamma, float radBuf) {
        this.radiation = radiation;
        this.digamma = digamma;
        this.radBuf = radBuf;
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final HbmLivingProps backend = new HbmLivingProps();
        private final LazyOptional<HbmLivingProps> optional = LazyOptional.of(() -> backend);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CAPABILITY.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return backend.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            backend.deserializeNBT(nbt);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }
}
