package com.hbm.port;

import com.hbm.lib.RefStrings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Adds port-status lines to every HBM item/block-item tooltip.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class PortContentEvents {
    private PortContentEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        var key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null || !RefStrings.MODID.equals(key.getNamespace())) {
            return;
        }

        PortContentRegistry.Status status = PortContentRegistry.status(stack);
        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.empty());
        tooltip.add(PortContentRegistry.tooltipLabel(status));
        tooltip.add(PortContentRegistry.tooltipDetail(status));
        if (status == PortContentRegistry.Status.UNIMPLEMENTED) {
            tooltip.add(Component.literal("Alpha port — catalog placeholder.")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }
}
