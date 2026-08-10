package com.hbm.client;

import com.hbm.HbmNuclearTechMod;
import com.hbm.lib.RefStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * If the server opens a container but no Screen appears, bind the registered MenuScreen
 * to the existing menu (do not recreate the menu — Forge BlockPos extra data would be lost).
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Dist.CLIENT)
public final class ClientMenuDebug {
    private static int silence;
    private static int healCooldown;

    private ClientMenuDebug() {
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        HbmNuclearTechMod.LOGGER.info("Screen opening: {}", event.getScreen().getClass().getName());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (healCooldown > 0) {
            healCooldown--;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;
        Screen screen = mc.screen;
        if (menu == mc.player.inventoryMenu || screen != null) {
            silence = 0;
            return;
        }

        if (silence <= 0) {
            var key = ForgeRegistries.MENU_TYPES.getKey(menu.getType());
            HbmNuclearTechMod.LOGGER.error(
                    "DESYNC: containerMenu={} type={} but mc.screen is null — attempting heal",
                    menu.getClass().getSimpleName(), key);
            silence = 40;
        } else {
            silence--;
        }

        if (healCooldown > 0) {
            return;
        }
        healCooldown = 5;
        tryHealScreen(mc, menu);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void tryHealScreen(Minecraft mc, AbstractContainerMenu menu) {
        MenuType type = menu.getType();
        Component title = Component.literal("HBM");
        java.util.Optional factoryOpt = MenuScreens.getScreenFactory(type, mc, menu.containerId, title);
        if (factoryOpt.isEmpty()) {
            HbmNuclearTechMod.LOGGER.error("No MenuScreens factory for {} — re-registering HBM screens",
                    ForgeRegistries.MENU_TYPES.getKey(type));
            try {
                ClientModEvents.registerMenuScreens();
            } catch (Exception e) {
                HbmNuclearTechMod.LOGGER.error("Re-register MenuScreens failed", e);
            }
            factoryOpt = MenuScreens.getScreenFactory(type, mc, menu.containerId, title);
            if (factoryOpt.isEmpty()) {
                return;
            }
        }
        try {
            MenuScreens.ScreenConstructor constructor = (MenuScreens.ScreenConstructor) factoryOpt.get();
            Screen screen = constructor.create(menu, mc.player.getInventory(), title);
            mc.setScreen(screen);
            HbmNuclearTechMod.LOGGER.info("Healed missing screen -> {}", screen.getClass().getName());
        } catch (Exception e) {
            HbmNuclearTechMod.LOGGER.error("Failed healing screen for {}", menu.getClass().getSimpleName(), e);
        }
    }
}
