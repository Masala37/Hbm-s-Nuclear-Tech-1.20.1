package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModRegistries {
    private ModRegistries() {
    }

    public static void register(IEventBus modBus) {
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModCreativeTabs.register(modBus);
        ModBlockEntities.register(modBus);
        ModEntities.register(modBus);
        ModFluids.register(modBus);
    }

    public static final class ModEntities {
        public static final DeferredRegister<EntityType<?>> ENTITIES =
                DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RefStrings.MODID);

        private ModEntities() {
        }

        public static void register(IEventBus modBus) {
            ENTITIES.register(modBus);
        }
    }
}
