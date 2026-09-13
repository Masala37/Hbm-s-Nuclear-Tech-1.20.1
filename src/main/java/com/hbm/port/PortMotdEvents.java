package com.hbm.port;

import com.hbm.lib.RefStrings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Chat notice on every login so players can file port bugs on GitHub.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class PortMotdEvents {
    private PortMotdEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        player.sendSystemMessage(Component.translatable("chat.hbm.port.report").withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal(RefStrings.ISSUES_URL)
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.RED)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, RefStrings.ISSUES_URL))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("chat.hbm.port.report.hover")))));
    }
}
