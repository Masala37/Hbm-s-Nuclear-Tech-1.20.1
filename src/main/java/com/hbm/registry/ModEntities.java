package com.hbm.registry;

import com.hbm.entity.bomb.PrimedBombEntity;
import com.hbm.entity.effect.EntityFalloutRain;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.lib.RefStrings;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RefStrings.MODID);

    public static final RegistryObject<EntityType<PrimedBombEntity>> PRIMED_BOMB = ENTITIES.register("primed_bomb",
            () -> EntityType.Builder.<PrimedBombEntity>of(PrimedBombEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_bomb"));

    public static final RegistryObject<EntityType<EntityNukeExplosionMK5>> NUKE_EXPLOSION_MK5 = ENTITIES.register(
            "nuke_explosion_mk5",
            () -> EntityType.Builder.<EntityNukeExplosionMK5>of(EntityNukeExplosionMK5::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(200)
                    .updateInterval(1)
                    .build("nuke_explosion_mk5"));

    public static final RegistryObject<EntityType<EntityNukeTorex>> NUKE_TOREX = ENTITIES.register(
            "nuke_torex",
            () -> EntityType.Builder.<EntityNukeTorex>of(EntityNukeTorex::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1.0F, 50.0F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .build("nuke_torex"));

    public static final RegistryObject<EntityType<EntityFalloutRain>> FALLOUT_RAIN = ENTITIES.register(
            "fallout_rain",
            () -> EntityType.Builder.<EntityFalloutRain>of(EntityFalloutRain::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(4.0F, 20.0F)
                    .clientTrackingRange(200)
                    .updateInterval(5)
                    .build("fallout_rain"));

    private ModEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
