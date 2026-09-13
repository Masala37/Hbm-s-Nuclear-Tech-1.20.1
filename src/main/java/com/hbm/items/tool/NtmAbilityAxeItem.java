package com.hbm.items.tool;

import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 1.7 {@code ItemToolAbility} axe (keeps vanilla stripping).
 */
public class NtmAbilityAxeItem extends AxeItem {
    private final NtmToolAbilities.Profile profile;

    public NtmAbilityAxeItem(NtmTiers tier, float oneSevenDamage, Rarity rarity, NtmToolAbilities.Profile profile) {
        super(tier, NtmToolAbilities.attackModifier(tier, oneSevenDamage), -3.1F,
                rarity == Rarity.COMMON ? new Item.Properties() : new Item.Properties().rarity(rarity));
        this.profile = profile;
    }

    public static NtmAbilityAxeItem create(NtmTiers tier, float damage, Consumer<NtmToolAbilities.Builder> op) {
        return create(tier, damage, Rarity.COMMON, op);
    }

    public static NtmAbilityAxeItem create(NtmTiers tier, float damage, Rarity rarity,
                                           Consumer<NtmToolAbilities.Builder> op) {
        NtmToolAbilities.Builder builder = NtmToolAbilities.Profile.builder();
        op.accept(builder);
        return new NtmAbilityAxeItem(tier, damage, rarity, builder.build());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return NtmToolAbilities.cycleUse(level, player, hand, player.getItemInHand(hand), profile);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        return NtmToolAbilities.onBlockStartBreak(stack, pos, player, profile);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        NtmToolAbilities.hurtEnemy(stack, target, attacker, profile);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        NtmToolAbilities.tooltip(stack, tooltip, profile);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return NtmToolAbilities.foil(stack, profile) || super.isFoil(stack);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return profile.silkActive(stack) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND
                ? NtmToolAbilities.withMovement(super.getDefaultAttributeModifiers(slot), profile)
                : super.getDefaultAttributeModifiers(slot);
    }
}
