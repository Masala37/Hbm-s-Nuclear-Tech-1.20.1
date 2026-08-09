package com.hbm.main;

import com.hbm.registry.ModCreativeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy extends ServerProxy {
    @Override
    public void register(IEventBus modBus) {
        super.register(modBus);
        modBus.addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModCreativeTabs::registerItemIcons);
    }
}
