package com.hbm.hazard.type;

import com.hbm.util.ContaminationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Inventory digamma contamination (legacy {@code HazardTypeDigamma}).
 */
public class HazardTypeDigamma extends HazardTypeBase {

    @Override
    public void onUpdate(LivingEntity target, float level, ItemStack stack) {
        float dose = level / 20.0F * stack.getCount();
        if (dose > 0.0F) {
            ContaminationUtil.applyDigammaData(target, dose);
        }
    }

    @Override
    public void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack) {
        if (level < 1e-5F) {
            return;
        }
        float d = (float) (Math.floor(level * 10000.0F)) / 10.0F;
        list.add(Component.literal("[Digamma]").withStyle(ChatFormatting.RED));
        list.add(Component.literal(d + " mDRX/s").withStyle(ChatFormatting.DARK_RED));
        if (stack.getCount() > 1) {
            float stackD = (float) (Math.floor(level * 10000.0F * stack.getCount()) / 10.0F);
            list.add(Component.literal("Stack: " + stackD + " mDRX/s").withStyle(ChatFormatting.DARK_RED));
        }
    }
}
