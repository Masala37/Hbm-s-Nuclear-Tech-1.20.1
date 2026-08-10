package com.hbm.hazard;

import com.hbm.lib.RefStrings;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Inventory hazard ticks + tooltip wiring (legacy {@code ModEventHandler} hazard call sites).
 * Registration runs from {@link com.hbm.main.ServerProxy#commonSetup}.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class HazardEvents {

    private HazardEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }
        HazardSystem.updatePlayerInventory(player);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        HazardSystem.addFullTooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
    }
}
