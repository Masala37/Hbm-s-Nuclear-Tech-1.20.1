package com.hbm.client;

import com.hbm.client.render.entity.PrimedBombRenderer;
import com.hbm.client.render.entity.RenderTorex;
import com.hbm.client.screen.DieselGeneratorScreen;
import com.hbm.client.screen.ElectricFurnaceScreen;
import com.hbm.client.screen.FluidBarrelScreen;
import com.hbm.client.screen.MachineBatteryScreen;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModEntities;
import com.hbm.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ELECTRIC_FURNACE.get(), ElectricFurnaceScreen::new);
            MenuScreens.register(ModMenus.MACHINE_BATTERY.get(), MachineBatteryScreen::new);
            MenuScreens.register(ModMenus.FLUID_BARREL.get(), FluidBarrelScreen::new);
            MenuScreens.register(ModMenus.DIESEL_GENERATOR.get(), DieselGeneratorScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PRIMED_BOMB.get(), PrimedBombRenderer::new);
        event.registerEntityRenderer(ModEntities.NUKE_EXPLOSION_MK5.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.NUKE_TOREX.get(), RenderTorex::new);
        event.registerEntityRenderer(ModEntities.FALLOUT_RAIN.get(), NoopRenderer::new);
    }
}
