package com.hbm.items.special;

import com.hbm.util.ContaminationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Digamma particle (legacy {@code ItemDigamma}).
 */
public class DigammaParticleItem extends Item {
    private final int halfLifeTicks;

    public DigammaParticleItem(int halfLifeTicks) {
        super(new Item.Properties().stacksTo(16));
        this.halfLifeTicks = Math.max(1, halfLifeTicks);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player) {
            ContaminationUtil.applyDigammaData(entity, 1.0F / halfLifeTicks);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        float mDrx = ((int) ((1000.0F / halfLifeTicks) * 200.0F)) / 10.0F;
        tooltip.add(Component.literal("[Digamma]").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal(mDrx + " mDRX/s").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.literal("Player half-life: " + (halfLifeTicks / 20.0F) + "s").withStyle(ChatFormatting.GOLD));
    }
}
