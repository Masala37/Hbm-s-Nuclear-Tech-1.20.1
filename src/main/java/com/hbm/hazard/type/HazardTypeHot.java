package com.hbm.hazard.type;

import com.hbm.config.RadiationConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 1.7.10 {@code HazardTypeHot} without reacher (not ported).
 */
public class HazardTypeHot extends HazardTypeBase {
    @Override
    public void onUpdate(LivingEntity target, float level, ItemStack stack) {
        if (RadiationConfig.disableHot != null && RadiationConfig.disableHot.get()) {
            return;
        }
        if (!target.isInWater() && level > 0.0F) {
            target.setSecondsOnFire((int) Math.ceil(level));
        }
    }

    @Override
    public void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack) {
        if (level > 0.0F) {
            list.add(Component.empty().withStyle(ChatFormatting.GOLD)
                    .append("[")
                    .append(Component.translatable("trait.hot"))
                    .append("]"));
        }
    }
}
