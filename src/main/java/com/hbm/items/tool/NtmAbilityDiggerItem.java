package com.hbm.items.tool;

import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 1.7 {@code ItemToolAbility} pick / shovel / miner.
 */
public class NtmAbilityDiggerItem extends DiggerItem {
    public enum Kind {
        PICKAXE,
        SHOVEL,
        MINER
    }

    private final Kind kind;
    private final NtmToolAbilities.Profile profile;

    public NtmAbilityDiggerItem(Kind kind, NtmTiers tier, float oneSevenDamage, Rarity rarity,
                                NtmToolAbilities.Profile profile) {
        super(NtmToolAbilities.attackModifier(tier, oneSevenDamage),
                kind == Kind.SHOVEL ? -3.0F : -2.8F,
                tier,
                kind == Kind.SHOVEL ? BlockTags.MINEABLE_WITH_SHOVEL : BlockTags.MINEABLE_WITH_PICKAXE,
                rarity == Rarity.COMMON ? new Item.Properties() : new Item.Properties().rarity(rarity));
        this.kind = kind;
        this.profile = profile;
    }

    public static NtmAbilityDiggerItem pickaxe(NtmTiers tier, float damage, Consumer<NtmToolAbilities.Builder> op) {
        return pickaxe(tier, damage, Rarity.COMMON, op);
    }

    public static NtmAbilityDiggerItem pickaxe(NtmTiers tier, float damage, Rarity rarity,
                                               Consumer<NtmToolAbilities.Builder> op) {
        NtmToolAbilities.Builder builder = NtmToolAbilities.Profile.builder();
        op.accept(builder);
        return new NtmAbilityDiggerItem(Kind.PICKAXE, tier, damage, rarity, builder.build());
    }

    public static NtmAbilityDiggerItem shovel(NtmTiers tier, float damage, Consumer<NtmToolAbilities.Builder> op) {
        return shovel(tier, damage, Rarity.COMMON, op);
    }

    public static NtmAbilityDiggerItem shovel(NtmTiers tier, float damage, Rarity rarity,
                                              Consumer<NtmToolAbilities.Builder> op) {
        NtmToolAbilities.Builder builder = NtmToolAbilities.Profile.builder();
        op.accept(builder);
        return new NtmAbilityDiggerItem(Kind.SHOVEL, tier, damage, rarity, builder.build());
    }

    public static NtmAbilityDiggerItem miner(NtmTiers tier, float damage, Consumer<NtmToolAbilities.Builder> op) {
        NtmToolAbilities.Builder builder = NtmToolAbilities.Profile.builder();
        op.accept(builder);
        return new NtmAbilityDiggerItem(Kind.MINER, tier, damage, Rarity.COMMON, builder.build());
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
        if (kind == Kind.MINER) {
            return NtmToolAbilities.minerCorrect(stack, state, profile);
        }
        return profile.silkActive(stack) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (kind == Kind.MINER && (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL))) {
            return getTier().getSpeed();
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        if (kind == Kind.MINER) {
            return ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(action)
                    || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(action);
        }
        return super.canPerformAction(stack, action);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND
                ? NtmToolAbilities.withMovement(super.getDefaultAttributeModifiers(slot), profile)
                : super.getDefaultAttributeModifiers(slot);
    }
}
