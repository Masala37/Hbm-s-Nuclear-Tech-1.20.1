package com.hbm.main;

import com.hbm.network.ModMessages;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerProxy {
    public void register(IEventBus modBus) {
        modBus.addListener(this::commonSetup);
    }

    protected void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModMessages::register);
    }
}
