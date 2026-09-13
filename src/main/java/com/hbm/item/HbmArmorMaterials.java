package com.hbm.item;

import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * 1.7 {@code MainRegistry} aMatHaz / aMatHaz2 / aMatHaz3.
 * Protection array is boots/legs/chest/helmet (1.20 {@code EquipmentSlot.getIndex()}).
 */
public enum HbmArmorMaterials implements ArmorMaterial {
    HAZMAT("hazmat", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, "hazmat_cloth"),
    HAZMAT_RED("hazmat_red", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, "hazmat_cloth_red"),
    HAZMAT_GREY("hazmat_grey", 60, new int[]{1, 4, 5, 2}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, "hazmat_cloth_grey");

    /** Vanilla 1.20 slot order: feet, legs, chest, head. */
    private static final int[] DURABILITY_PER_SLOT = {13, 15, 16, 11};

    private final String name;
    private final int durabilityMultiplier;
    private final int[] protection;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repair;

    HbmArmorMaterials(String name, int durabilityMultiplier, int[] protection, int enchantability,
                      SoundEvent equipSound, float toughness, float knockbackResistance, String repairId) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protection = protection;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repair = () -> cloth(repairId);
    }

    private static Ingredient cloth(String path) {
        Item found = ForgeRegistries.ITEMS.getValue(new ResourceLocation(RefStrings.MODID, path));
        return found == null ? Ingredient.EMPTY : Ingredient.of(found);
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
        return repair.get();
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
