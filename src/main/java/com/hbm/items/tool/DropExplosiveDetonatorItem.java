package com.hbm.items.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Drop-to-explode detonator (legacy {@code ItemDrop} detonator_de).
 */
public class DropExplosiveDetonatorItem extends Item {
    public DropExplosiveDetonatorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.detonator_de.desc"));
        tooltip.add(Component.translatable("trait.hbm.drop").withStyle(ChatFormatting.RED));
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level world = entity.level();
        if (!world.isClientSide) {
            world.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 15.0F, true, Level.ExplosionInteraction.TNT);
        }
        entity.discard();
        return true;
    }
}
