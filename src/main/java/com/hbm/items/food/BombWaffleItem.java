package com.hbm.items.food;

import com.hbm.explosion.ExplosionNukeSmall;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Bomb waffle (legacy {@code ItemWaffle}) — edible, then medium muke blast.
 */
public class BombWaffleItem extends Item {
    public BombWaffleItem() {
        super(new Item.Properties()
                .stacksTo(16)
                .rarity(Rarity.EPIC)
                .food(new FoodProperties.Builder()
                        .nutrition(20)
                        .saturationMod(1.0F)
                        .alwaysEat()
                        .build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            ExplosionNukeSmall.explode(level, entity.getX(), entity.getY() + 0.5D, entity.getZ(),
                    ExplosionNukeSmall.PARAMS_MEDIUM);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Tastes like victory. Ends like a Fat Man.")
                .withStyle(ChatFormatting.RED));
    }
}
