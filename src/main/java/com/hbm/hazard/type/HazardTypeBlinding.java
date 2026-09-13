package com.hbm.hazard.type;

import com.hbm.config.RadiationConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 1.7.10 {@code HazardTypeBlinding} without armor class lookup (not ported).
 */
public class HazardTypeBlinding extends HazardTypeBase {
    @Override
    public void onUpdate(LivingEntity target, float level, ItemStack stack) {
        if (RadiationConfig.disableBlinding != null && RadiationConfig.disableBlinding.get()) {
            return;
        }
        if (level <= 0.0F) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) Math.ceil(level), 0, false, false, false));
    }

    @Override
    public void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack) {
        list.add(Component.empty().withStyle(ChatFormatting.DARK_AQUA)
                .append("[")
                .append(Component.translatable("trait.blinding"))
                .append("]"));
    }
}
