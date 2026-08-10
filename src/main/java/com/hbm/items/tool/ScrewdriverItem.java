package com.hbm.items.tool;

import com.hbm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Screwdriver used to open a closed demon core (and general tooling).
 * Right-click while holding a closed demon core in the other hand to pry it open.
 */
public class ScrewdriverItem extends Item {
    private final boolean desh;

    public ScrewdriverItem(boolean desh) {
        super(desh
                ? new Item.Properties().stacksTo(1)
                : new Item.Properties().stacksTo(1).durability(100));
        this.desh = desh;
    }

    public static ScrewdriverItem steel() {
        return new ScrewdriverItem(false);
    }

    public static ScrewdriverItem desh() {
        return new ScrewdriverItem(true);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.screwdriver.desc").withStyle(ChatFormatting.GRAY));
        if (desh) {
            tooltip.add(Component.translatable("item.hbm.screwdriver_desh.desc").withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return desh || super.isFoil(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);

        Item closed = ForgeRegistries.ITEMS.getValue(new ResourceLocation("hbm", "demon_core_closed"));
        if (closed == null || closed == Items.AIR || !other.is(closed)) {
            return InteractionResultHolder.pass(tool);
        }

        if (!level.isClientSide) {
            other.shrink(1);
            ItemStack open = new ItemStack(ModItems.DEMON_CORE_OPEN.get());
            if (!player.getInventory().add(open)) {
                player.drop(open, false);
            }
            if (!desh) {
                tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
        }
        return InteractionResultHolder.sidedSuccess(tool, level.isClientSide());
    }
}
