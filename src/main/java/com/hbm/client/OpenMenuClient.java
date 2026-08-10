package com.hbm.client;

import com.hbm.HbmNuclearTechMod;
import com.hbm.network.OpenMenuS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Client-only open-menu handling. Kept out of {@link OpenMenuS2CPacket} so the packet
 * class can load on the server thread without resolving Minecraft client classes.
 */
@OnlyIn(Dist.CLIENT)
public final class OpenMenuClient {
    private OpenMenuClient() {
    }

    public static void open(OpenMenuS2CPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            HbmNuclearTechMod.LOGGER.error("OpenMenuS2C: no player/level");
            return;
        }

        MenuType<?> type = ForgeRegistries.MENU_TYPES.getValue(packet.menuType());
        if (type == null) {
            HbmNuclearTechMod.LOGGER.error("OpenMenuS2C: unknown menu type {}", packet.menuType());
            return;
        }

        FriendlyByteBuf extra = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            extra.writeBlockPos(packet.pos());

            AbstractContainerMenu menu;
            try {
                @SuppressWarnings("unchecked")
                IForgeMenuType<AbstractContainerMenu> forgeType =
                        (IForgeMenuType<AbstractContainerMenu>) (IForgeMenuType<?>) type;
                menu = forgeType.create(packet.containerId(), mc.player.getInventory(), extra);
            } catch (ClassCastException ex) {
                menu = type.create(packet.containerId(), mc.player.getInventory());
            }
            if (menu == null) {
                HbmNuclearTechMod.LOGGER.error("OpenMenuS2C: menu factory returned null for {}", packet.menuType());
                return;
            }

            var factoryOpt = MenuScreens.getScreenFactory(type, mc, packet.containerId(), packet.title());
            if (factoryOpt.isEmpty()) {
                HbmNuclearTechMod.LOGGER.error("OpenMenuS2C: no screen factory for {} — re-registering", packet.menuType());
                ClientModEvents.registerMenuScreens();
                factoryOpt = MenuScreens.getScreenFactory(type, mc, packet.containerId(), packet.title());
            }
            if (factoryOpt.isEmpty()) {
                HbmNuclearTechMod.LOGGER.error("OpenMenuS2C: still no screen factory for {}", packet.menuType());
                return;
            }

            @SuppressWarnings({"unchecked", "rawtypes"})
            Screen screen = ((MenuScreens.ScreenConstructor) factoryOpt.get())
                    .create(menu, mc.player.getInventory(), packet.title());

            mc.player.containerMenu = menu;
            mc.setScreen(screen);
            HbmNuclearTechMod.LOGGER.info("OpenMenuS2C opened {} -> {}",
                    menu.getClass().getSimpleName(), screen.getClass().getSimpleName());
        } catch (Throwable t) {
            HbmNuclearTechMod.LOGGER.error("OpenMenuS2C FAILED for {} at {}", packet.menuType(), packet.pos(), t);
        } finally {
            extra.release();
        }
    }
}
