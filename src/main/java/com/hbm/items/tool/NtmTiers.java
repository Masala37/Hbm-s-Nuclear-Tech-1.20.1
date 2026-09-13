package com.hbm.items.tool;

import com.hbm.lib.RefStrings;
import com.hbm.registry.ModTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * 1.7 {@code MainRegistry} tool materials (harvest / uses / speed / damage / enchant).
 */
public enum NtmTiers implements Tier {
    STEEL(3, 750, 8.0F, 2.0F, 10, () -> Ingredient.of(ModTags.Items.INGOTS_STEEL), BlockTags.NEEDS_DIAMOND_TOOL),
    TITANIUM(3, 1000, 9.0F, 2.5F, 15, () -> Ingredient.of(ModTags.Items.INGOTS_TITANIUM), BlockTags.NEEDS_DIAMOND_TOOL),
    DWARVEN(2, 250, 4.0F, 0.0F, 10, () -> Ingredient.of(ModTags.Items.INGOTS_COPPER), BlockTags.NEEDS_IRON_TOOL),
    COBALT(3, 750, 9.0F, 2.5F, 60, () -> Ingredient.of(ModTags.Items.INGOTS_COBALT), BlockTags.NEEDS_DIAMOND_TOOL),
    COBALT_DECORATED(3, 2500, 15.0F, 2.5F, 75, () -> Ingredient.of(ModTags.Items.INGOTS_COBALT), BlockTags.NEEDS_DIAMOND_TOOL),
    CMB(3, 8500, 40.0F, 55.0F, 100, () -> Ingredient.of(ModTags.Items.INGOTS_COMBINE_STEEL), BlockTags.NEEDS_DIAMOND_TOOL),
    DESH(2, 0, 7.5F, 2.0F, 10, () -> Ingredient.of(ModTags.Items.INGOTS_DESH), BlockTags.NEEDS_IRON_TOOL),
    STARMETAL(3, 3000, 20.0F, 2.5F, 100, () -> Ingredient.of(ModTags.Items.INGOTS_STARMETAL), BlockTags.NEEDS_DIAMOND_TOOL),
    SCHRABIDIUM(3, 10000, 50.0F, 100.0F, 200, () -> Ingredient.of(ModTags.Items.INGOTS_SCHRABIDIUM), BlockTags.NEEDS_DIAMOND_TOOL),
    BISMUTH(4, 0, 50.0F, 0.0F, 200, () -> Ingredient.of(ModTags.Items.INGOTS_BISMUTH), BlockTags.NEEDS_DIAMOND_TOOL),
    VOLCANIC(4, 0, 50.0F, 0.0F, 200, () -> Ingredient.of(ModTags.Items.INGOTS_BISMUTH), BlockTags.NEEDS_DIAMOND_TOOL),
    CHLOROPHYTE(4, 0, 75.0F, 0.0F, 200, () -> item("powder_chlorophyte"), BlockTags.NEEDS_DIAMOND_TOOL),
    MESE(4, 0, 100.0F, 0.0F, 200, () -> item("plate_paa"), BlockTags.NEEDS_DIAMOND_TOOL),
    BOTTLE_OPENER(1, 250, 1.5F, 0.5F, 200, () -> item("plate_steel"), BlockTags.NEEDS_STONE_TOOL);

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repair;
    private final TagKey<Block> tag;

    private static Ingredient item(String path) {
        Item found = ForgeRegistries.ITEMS.getValue(new ResourceLocation(RefStrings.MODID, path));
        return found == null ? Ingredient.EMPTY : Ingredient.of(found);
    }

    NtmTiers(int level, int uses, float speed, float damage, int enchantmentValue,
             Supplier<Ingredient> repair, TagKey<Block> tag) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repair = repair;
        this.tag = tag;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return damage;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repair.get();
    }

    @Override
    public TagKey<Block> getTag() {
        return tag;
    }
}
