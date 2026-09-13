package com.hbm.items.tool;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 1.7 {@code ItemSwordAbility}.
 */
public class NtmAbilitySwordItem extends SwordItem {
    private final NtmToolAbilities.Profile profile;
    private final float oneSevenDamage;

    public NtmAbilitySwordItem(NtmTiers tier, float oneSevenDamage, Rarity rarity, NtmToolAbilities.Profile profile) {
        super(tier, 0, -2.4F,
                rarity == Rarity.COMMON ? new Item.Properties() : new Item.Properties().rarity(rarity));
        this.profile = profile;
        this.oneSevenDamage = oneSevenDamage;
    }

    public static NtmAbilitySwordItem create(NtmTiers tier, float damage, Consumer<NtmToolAbilities.Builder> op) {
        return create(tier, damage, Rarity.COMMON, op);
    }

    public static NtmAbilitySwordItem create(NtmTiers tier, float damage, Rarity rarity,
                                             Consumer<NtmToolAbilities.Builder> op) {
        NtmToolAbilities.Builder builder = NtmToolAbilities.Profile.builder();
        op.accept(builder);
        return new NtmAbilitySwordItem(tier, damage, rarity, builder.build());
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
        return super.isFoil(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                oneSevenDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                -2.4, AttributeModifier.Operation.ADDITION));
        return NtmToolAbilities.withMovement(builder.build(), profile);
    }
}
