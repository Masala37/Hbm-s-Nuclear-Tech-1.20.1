package com.hbm.items.special;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Right-click unpack kit: bomb casing + assembly parts (legacy {@code ItemStarterKit} bomb kits).
 */
public class BombKitItem extends Item {
    private final Supplier<List<ItemStack>> contents;

    public BombKitItem(Supplier<List<ItemStack>> contents) {
        super(new Item.Properties().stacksTo(1));
        this.contents = contents;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            for (ItemStack gift : contents.get()) {
                ItemStack copy = gift.copy();
                if (!player.getInventory().add(copy)) {
                    player.drop(copy, false);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.bomb_kit.hint"));
    }
}
