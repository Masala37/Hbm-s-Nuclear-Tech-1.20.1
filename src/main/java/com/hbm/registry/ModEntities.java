package com.hbm.registry;

import com.hbm.entity.bomb.PrimedBombEntity;
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

    private ModEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
