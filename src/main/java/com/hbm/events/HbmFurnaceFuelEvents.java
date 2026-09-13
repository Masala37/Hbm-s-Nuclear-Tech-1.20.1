package com.hbm.events;

import com.hbm.inventory.recipes.HbmFurnaceBurnTimes;
import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 1.7.10 {@code FuelHandler} for vanilla furnaces, presses, fireboxes, and blast furnaces.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HbmFurnaceFuelEvents {
    private HbmFurnaceFuelEvents() {
    }

    @SubscribeEvent
    public static void onFurnaceFuel(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !RefStrings.MODID.equals(id.getNamespace())) {
            return;
        }
        int burn = HbmFurnaceBurnTimes.burnTime(id.getPath());
        if (burn > 0) {
            event.setBurnTime(burn);
        }
    }
}
