package com.hbm.items.special;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Bomb / nuke component with a tooltip key (egg, pellets, powders, batteries).
 */
public class BombComponentItem extends Item {
    private final String descKey;
    private final Rarity rarity;

    public BombComponentItem(String descKey, Rarity rarity, int maxStack) {
        super(new Item.Properties().stacksTo(maxStack).rarity(rarity));
        this.descKey = descKey;
        this.rarity = rarity;
    }

    public static BombComponentItem of(String descKey) {
        return new BombComponentItem(descKey, Rarity.UNCOMMON, 64);
    }

    public static BombComponentItem rare(String descKey) {
        return new BombComponentItem(descKey, Rarity.RARE, 16);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(descKey).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return rarity;
    }
}
