package com.hbm.items.tool;

import api.hbm.item.IDesignatorItem;
import com.hbm.HbmNuclearTechMod;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DesignatorManualItem extends Item implements IDesignatorItem {
    public static final String TAG_X = DesignatorManualCoords.TAG_X;
    public static final String TAG_Z = DesignatorManualCoords.TAG_Z;

    public DesignatorManualItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack held(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof DesignatorManualItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof DesignatorManualItem) {
            return off;
        }
        return ItemStack.EMPTY;
    }

    public static int readX(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_X);
    }

    public static int readZ(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_Z);
    }

    public static void apply(ItemStack stack, int operator, int value, int reference, int playerX, int playerZ) {
        CompoundTag tag = stack.getOrCreateTag();
        if (operator == 2) {
            if (reference == 0) {
                tag.putInt(TAG_X, playerX);
            } else {
                tag.putInt(TAG_Z, playerZ);
            }
            return;
        }
        int delta = 0;
        if (operator == 0) {
            delta = value;
        }
        if (operator == 1) {
            delta = -value;
        }
        if (reference == 0) {
            tag.putInt(TAG_X, tag.getInt(TAG_X) + delta);
        } else {
            tag.putInt(TAG_Z, tag.getInt(TAG_Z) + delta);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            HbmNuclearTechMod.proxy.openDesignatorScreen(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag()) {
            tooltip.add(Component.literal("Target Coordinates:").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal("X: " + readX(stack)));
            tooltip.add(Component.literal("Z: " + readZ(stack)));
        } else {
            tooltip.add(Component.literal("Please select a target.").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isReady(Level world, ItemStack stack, int x, int y, int z) {
        return stack.hasTag();
    }

    @Override
    public Vec3 getCoords(Level world, ItemStack stack, int x, int y, int z) {
        return new Vec3(readX(stack), 0, readZ(stack));
    }
}
