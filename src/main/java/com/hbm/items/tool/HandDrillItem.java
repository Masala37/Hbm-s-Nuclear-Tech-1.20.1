package com.hbm.items.tool;

import api.hbm.block.IToolable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7 {@code ItemTooling(HAND_DRILL)}: press dummy removal and diode throughput.
 */
public class HandDrillItem extends Item {
    private final boolean desh;

    public HandDrillItem(boolean desh) {
        super(desh
                ? new Item.Properties().stacksTo(1)
                : new Item.Properties().stacksTo(1).durability(100));
        this.desh = desh;
    }

    public static HandDrillItem steel() {
        return new HandDrillItem(false);
    }

    public static HandDrillItem desh() {
        return new HandDrillItem(true);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.hand_drill.desc").withStyle(ChatFormatting.GRAY));
        if (desh) {
            tooltip.add(Component.translatable("item.hbm.hand_drill_desh.desc").withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return desh || super.isFoil(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (getMaxDamage() <= 0) {
            return stack.copy();
        }
        ItemStack copy = stack.copy();
        copy.setDamageValue(copy.getDamageValue() + 1);
        return copy.getDamageValue() >= copy.getMaxDamage() ? ItemStack.EMPTY : copy;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return IToolable.tryUseOn(context);
    }
}
