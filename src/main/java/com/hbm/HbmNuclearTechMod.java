package com.hbm;

import com.hbm.config.ModConfig;
import com.hbm.lib.RefStrings;
import com.hbm.main.ClientProxy;
import com.hbm.main.ServerProxy;
import com.hbm.registry.ModRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RefStrings.MODID)
public class HbmNuclearTechMod {
    public static final Logger LOGGER = LogManager.getLogger("HBM");

    public static ServerProxy proxy;

    public HbmNuclearTechMod(IEventBus modBus) {
        ModConfig.register();
        ModRegistries.register(modBus);

        proxy = switch (net.minecraftforge.fml.loading.FMLEnvironment.dist) {
            case CLIENT -> new ClientProxy();
            default -> new ServerProxy();
        };
        proxy.register(modBus);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("HBM's Nuclear Tech Mod 1.20.1 port bootstrap loaded.");
    }
}
