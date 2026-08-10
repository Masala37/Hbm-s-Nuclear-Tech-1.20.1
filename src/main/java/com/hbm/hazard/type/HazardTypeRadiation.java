package com.hbm.hazard.type;

import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Inventory radiation contamination (legacy {@code HazardTypeRadiation}, without reacher scaling).
 */
public class HazardTypeRadiation extends HazardTypeBase {

    @Override
    public void onUpdate(LivingEntity target, float level, ItemStack stack) {
        float dose = level * stack.getCount() / 20.0F;
        if (dose > 0.0F) {
            ContaminationUtil.contaminate(target, HazardType.RADIATION, ContaminationType.CREATIVE, dose);
        }
    }

    @Override
    public void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack) {
        if (level < 1e-5F) {
            return;
        }
        list.add(Component.literal("[Radioactive]").withStyle(ChatFormatting.GREEN));
        String rad = String.valueOf(Math.floor(level * 1000.0D) / 1000.0D);
        list.add(Component.literal(rad + " RAD/s").withStyle(ChatFormatting.YELLOW));
        if (stack.getCount() > 1) {
            String stackRad = String.valueOf(Math.floor(level * 1000.0D * stack.getCount()) / 1000.0D);
            list.add(Component.literal("Stack: " + stackRad + " RAD/s").withStyle(ChatFormatting.YELLOW));
        }
    }
}
