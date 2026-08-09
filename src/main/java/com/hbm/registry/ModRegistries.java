package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

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
}
