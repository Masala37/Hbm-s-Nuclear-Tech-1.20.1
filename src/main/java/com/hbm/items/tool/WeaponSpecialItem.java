package com.hbm.items.tool;

import com.hbm.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7 {@code WeaponSpecial} for the wood gavel and bottle opener.
 */
public class WeaponSpecialItem extends SwordItem {
    public enum Kind {
        WOOD_GAVEL,
        BOTTLE_OPENER
    }

    private final Kind kind;

    public WeaponSpecialItem(Kind kind) {
        super(kind == Kind.BOTTLE_OPENER ? NtmTiers.BOTTLE_OPENER : Tiers.WOOD,
                kind == Kind.BOTTLE_OPENER ? 4 : 4,
                -2.4F,
                new Item.Properties().stacksTo(1));
        this.kind = kind;
    }

    public static WeaponSpecialItem woodGavel() {
        return new WeaponSpecialItem(Kind.WOOD_GAVEL);
    }

    public static WeaponSpecialItem bottleOpener() {
        return new WeaponSpecialItem(Kind.BOTTLE_OPENER);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (kind == Kind.WOOD_GAVEL) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    ModSounds.require("weapon.whack"), SoundSource.PLAYERS, 3.0F, 1.0F);
        } else if (kind == Kind.BOTTLE_OPENER) {
            if (!level.isClientSide) {
                int roll = level.random.nextInt(7);
                if (roll == 0) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 60 * 20, 0));
                } else if (roll == 1) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 60 * 20, 2));
                } else if (roll == 2) {
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 5 * 60 * 20, 2));
                } else if (roll == 3) {
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60 * 20, 0));
                }
            }
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 3.0F, 1.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (kind == Kind.WOOD_GAVEL) {
            tooltip.add(Component.translatable("item.hbm.wood_gavel.desc").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.hbm.bottle_opener.desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.hbm.bottle_opener.desc.warn").withStyle(ChatFormatting.RED));
        }
    }
}
