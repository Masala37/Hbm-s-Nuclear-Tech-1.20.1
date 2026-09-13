package com.hbm.items.special;

import com.hbm.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Right-click equips a full hazmat set, dropping whatever was worn (legacy {@code ItemStarterKit.giveHaz}).
 */
public class HazmatKitItem extends Item {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final Supplier<Item> helmet;
    private final Supplier<Item> chest;
    private final Supplier<Item> legs;
    private final Supplier<Item> boots;
    private final boolean displaceHint;

    public HazmatKitItem(Supplier<Item> helmet, Supplier<Item> chest, Supplier<Item> legs, Supplier<Item> boots,
                         boolean displaceHint) {
        super(new Item.Properties().stacksTo(1));
        this.helmet = helmet;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
        this.displaceHint = displaceHint;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            equip(level, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.require("item.unpack"), SoundSource.PLAYERS, 1.0F, 1.0F);
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void equip(Level level, Player player) {
        for (EquipmentSlot slot : SLOTS) {
            ItemStack worn = player.getItemBySlot(slot);
            if (!worn.isEmpty()) {
                ItemEntity dropped = new ItemEntity(level, player.getX(), player.getY() + player.getEyeHeight(),
                        player.getZ(), worn.copy());
                dropped.setPickUpDelay(0);
                level.addFreshEntity(dropped);
                player.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmet.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legs.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots.get()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (displaceHint) {
            tooltip.add(Component.translatable("item.hbm.hazmat_kit.hint"));
        }
    }
}
