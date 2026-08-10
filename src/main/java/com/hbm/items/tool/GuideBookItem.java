package com.hbm.items.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.List;

/**
 * Guide book (legacy {@code ItemGuideBook}). Opens the NTM wiki; full multipage GUI can follow later.
 */
public class GuideBookItem extends Item {
    public static final String WIKI_URL = "https://nucleartech.wiki/";

    public GuideBookItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            try {
                Util.getPlatform().openUri(new URI(WIKI_URL));
            } catch (Exception ignored) {
                // Fall through to chat link.
            }
        } else {
            Component link = Component.literal(WIKI_URL)
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.AQUA)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, WIKI_URL)));
            player.sendSystemMessage(Component.translatable("item.hbm.book_guide.open").append(" ").append(link));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.book_guide.desc"));
    }
}
