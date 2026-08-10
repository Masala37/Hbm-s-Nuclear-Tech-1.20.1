package com.hbm.capability;

import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class HbmCapabilityEvents {
    private static final ResourceLocation LIVING_PROPS_RL = new ResourceLocation(RefStrings.MODID, "living_props");

    private HbmCapabilityEvents() {
    }

    @Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void registerCaps(RegisterCapabilitiesEvent event) {
            event.register(HbmLivingProps.class);
        }
    }

    @Mod.EventBusSubscriber(modid = RefStrings.MODID)
    public static final class ForgeBus {
        private ForgeBus() {
        }

        @SubscribeEvent
        public static void attachEntity(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LivingEntity) {
                event.addCapability(LIVING_PROPS_RL, new HbmLivingProps.Provider());
            }
        }

        @SubscribeEvent
        public static void clonePlayer(PlayerEvent.Clone event) {
            // Always copy on dimension change; on death keep dose (legacy-ish) unless we decide otherwise.
            Player original = event.getOriginal();
            Player clone = event.getEntity();
            original.reviveCaps();
            original.getCapability(HbmLivingProps.CAPABILITY).ifPresent(oldProps ->
                    clone.getCapability(HbmLivingProps.CAPABILITY).ifPresent(newProps -> {
                        newProps.deserializeNBT(oldProps.serializeNBT());
                        newProps.applyDigammaHealth(clone);
                    }));
            original.invalidateCaps();
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                HbmLivingProps.get(player).applyDigammaHealth(player);
                HbmLivingProps.get(player).sendSync(player);
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                HbmLivingProps.get(player).applyDigammaHealth(player);
                HbmLivingProps.get(player).sendSync(player);
            }
        }
    }
}
