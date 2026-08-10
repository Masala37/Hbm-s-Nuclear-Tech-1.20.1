package com.hbm.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Lightweight hazmat armor materials (yellow / red / grey).
 */
public enum HbmArmorMaterials implements ArmorMaterial {
    HAZMAT("hazmat", 12, new int[]{1, 2, 3, 1}, 8, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F),
    HAZMAT_RED("hazmat_red", 18, new int[]{2, 3, 4, 1}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F),
    HAZMAT_GREY("hazmat_grey", 24, new int[]{2, 4, 5, 2}, 12, SoundEvents.ARMOR_EQUIP_LEATHER, 0.5F, 0.0F);

    private static final int[] DURABILITY_PER_SLOT = {13, 15, 16, 11};

    private final String name;
    private final int durabilityMultiplier;
    private final int[] protection;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;

    HbmArmorMaterials(String name, int durabilityMultiplier, int[] protection, int enchantability,
                      SoundEvent equipSound, float toughness, float knockbackResistance) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protection = protection;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return DURABILITY_PER_SLOT[type.getSlot().getIndex()] * durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return protection[type.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    public String getName() {
        return "hbm:" + name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
