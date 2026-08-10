package com.hbm.items.tool;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Safe bomb / landmine disarm tool (legacy ItemDefuser).
 */
public class DefuserItem extends Item {
    public DefuserItem() {
        super(new Item.Properties().stacksTo(1).durability(100));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.defuser.desc"));
    }
}
