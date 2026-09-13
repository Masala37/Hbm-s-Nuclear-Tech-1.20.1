package com.hbm.items.tool;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import static com.hbm.items.tool.NtmToolAbilities.Area;
import static com.hbm.items.tool.NtmToolAbilities.Harvest;
import static com.hbm.items.tool.NtmToolAbilities.Weapon;

/** 1.7 {@code ModItems} tool ability lists and entity damage. */
public final class NtmAbilityItems {
    private NtmAbilityItems() {
    }

    public static Item steelSword() {
        return NtmAbilitySwordItem.create(NtmTiers.STEEL, 6.0F, b -> b.weapon(Weapon.STUN, 0));
    }

    public static Item steelPick() {
        return NtmAbilityDiggerItem.pickaxe(NtmTiers.STEEL, 4.0F, b -> b.area(Area.RECURSION, 0));
    }

    public static Item steelAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.STEEL, 5.0F, b -> b.area(Area.RECURSION, 0).weapon(Weapon.BEHEADER, 0));
    }

    public static Item steelShovel() {
        return NtmAbilityDiggerItem.shovel(NtmTiers.STEEL, 3.0F, b -> b.area(Area.RECURSION, 0));
    }

    public static Item titaniumSword() {
        return NtmAbilitySwordItem.create(NtmTiers.TITANIUM, 6.5F, b -> {
        });
    }

    public static Item titaniumPick() {
        return NtmAbilityDiggerItem.pickaxe(NtmTiers.TITANIUM, 4.5F, b -> {
        });
    }

    public static Item titaniumAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.TITANIUM, 5.5F, b -> b.weapon(Weapon.BEHEADER, 0));
    }

    public static Item titaniumShovel() {
        return NtmAbilityDiggerItem.shovel(NtmTiers.TITANIUM, 3.5F, b -> {
        });
    }

    public static Item dwarvenPick() {
        return NtmAbilityDiggerItem.miner(NtmTiers.DWARVEN, 5.0F,
                b -> b.movement(-0.1).area(Area.HAMMER, 0).area(Area.HAMMER_FLAT, 0));
    }

    public static Item cobaltSword() {
        return NtmAbilitySwordItem.create(NtmTiers.COBALT, 12.0F, b -> {
        });
    }

    public static Item cobaltPick() {
        return NtmAbilityDiggerItem.pickaxe(NtmTiers.COBALT, 4.0F,
                b -> b.area(Area.RECURSION, 1).harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 0));
    }

    public static Item cobaltAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.COBALT, 6.0F,
                b -> b.area(Area.RECURSION, 1).harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 0).weapon(Weapon.BEHEADER, 0));
    }

    public static Item cobaltShovel() {
        return NtmAbilityDiggerItem.shovel(NtmTiers.COBALT, 3.5F,
                b -> b.area(Area.RECURSION, 1).harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 0));
    }

    public static Item cobaltDecoratedSword() {
        return NtmAbilitySwordItem.create(NtmTiers.COBALT_DECORATED, 15.0F, b -> {
        });
    }

    public static Item cobaltDecoratedPick() {
        return NtmAbilityDiggerItem.pickaxe(NtmTiers.COBALT_DECORATED, 6.0F,
                b -> b.area(Area.RECURSION, 1).area(Area.HAMMER, 0).area(Area.HAMMER_FLAT, 0)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 2));
    }

    public static Item cobaltDecoratedAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.COBALT_DECORATED, 8.0F,
                b -> b.area(Area.RECURSION, 1).area(Area.HAMMER, 0).area(Area.HAMMER_FLAT, 0)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 2).weapon(Weapon.BEHEADER, 0));
    }

    public static Item cobaltDecoratedShovel() {
        return NtmAbilityDiggerItem.shovel(NtmTiers.COBALT_DECORATED, 5.0F,
                b -> b.area(Area.RECURSION, 1).area(Area.HAMMER, 0).area(Area.HAMMER_FLAT, 0)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 2));
    }

    public static Item cmbSword() {
        return NtmAbilitySwordItem.create(NtmTiers.CMB, 35.0F,
                b -> b.weapon(Weapon.STUN, 0).weapon(Weapon.VAMPIRE, 0));
    }

    public static Item cmbPick() {
        return NtmAbilityDiggerItem.pickaxe(NtmTiers.CMB, 10.0F,
                b -> b.area(Area.RECURSION, 2).harvest(Harvest.SMELTER, 0).harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 2));
    }

    public static Item cmbAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.CMB, 30.0F,
                b -> b.area(Area.RECURSION, 2).harvest(Harvest.SMELTER, 0).harvest(Harvest.SILK, 0)
                        .harvest(Harvest.LUCK, 2).weapon(Weapon.BEHEADER, 0));
    }

    public static Item cmbShovel() {
        return NtmAbilityDiggerItem.shovel(NtmTiers.CMB, 8.0F,
                b -> b.area(Area.RECURSION, 2).harvest(Harvest.SMELTER, 0).harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 2));
    }

    public static Item deshSword() {
        return NtmAbilitySwordItem.create(NtmTiers.DESH, 12.5F, b -> b.weapon(Weapon.STUN, 0));
    }

    public static Item deshPick() {
        return NtmAbilityDiggerItem.pickaxe(NtmTiers.DESH, 5.0F,
                b -> b.movement(-0.05).area(Area.HAMMER, 0).area(Area.HAMMER_FLAT, 0).area(Area.RECURSION, 0)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 1));
    }

    public static Item deshAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.DESH, 7.5F,
                b -> b.movement(-0.05).area(Area.HAMMER, 0).area(Area.HAMMER_FLAT, 0).area(Area.RECURSION, 0)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 1).weapon(Weapon.BEHEADER, 0));
    }

    public static Item deshShovel() {
        return NtmAbilityDiggerItem.shovel(NtmTiers.DESH, 4.0F,
                b -> b.movement(-0.05).area(Area.HAMMER, 0).area(Area.HAMMER_FLAT, 0).area(Area.RECURSION, 0)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 1));
    }

    public static Item starmetalSword() {
        return NtmAbilitySwordItem.create(NtmTiers.STARMETAL, 25.0F,
                b -> b.weapon(Weapon.BEHEADER, 0).weapon(Weapon.STUN, 1));
    }

    public static Item starmetalPick() {
        return NtmAbilityDiggerItem.pickaxe(NtmTiers.STARMETAL, 8.0F,
                b -> b.area(Area.RECURSION, 3).area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 4).weapon(Weapon.STUN, 1));
    }

    public static Item starmetalAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.STARMETAL, 12.0F,
                b -> b.area(Area.RECURSION, 3).area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 4)
                        .weapon(Weapon.BEHEADER, 0).weapon(Weapon.STUN, 1));
    }

    public static Item starmetalShovel() {
        return NtmAbilityDiggerItem.shovel(NtmTiers.STARMETAL, 7.0F,
                b -> b.area(Area.RECURSION, 3).area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 4).weapon(Weapon.STUN, 1));
    }

    public static Item schrabidiumSword() {
        return NtmAbilitySwordItem.create(NtmTiers.SCHRABIDIUM, 75.0F, Rarity.RARE, b -> b.weapon(Weapon.VAMPIRE, 0));
    }

    public static Item schrabidiumPick() {
        return NtmAbilityDiggerItem.pickaxe(NtmTiers.SCHRABIDIUM, 20.0F, Rarity.RARE,
                b -> b.area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1).area(Area.RECURSION, 6)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 4).harvest(Harvest.SMELTER, 0));
    }

    public static Item schrabidiumAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.SCHRABIDIUM, 25.0F, Rarity.RARE,
                b -> b.area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1).area(Area.RECURSION, 6)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 4).harvest(Harvest.SMELTER, 0)
                        .weapon(Weapon.BEHEADER, 0));
    }

    public static Item schrabidiumShovel() {
        return NtmAbilityDiggerItem.shovel(NtmTiers.SCHRABIDIUM, 15.0F, Rarity.RARE,
                b -> b.area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1).area(Area.RECURSION, 6)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 4).harvest(Harvest.SMELTER, 0));
    }

    public static Item bismuthPick() {
        return NtmAbilityDiggerItem.miner(NtmTiers.BISMUTH, 15.0F,
                b -> b.area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1).area(Area.RECURSION, 1)
                        .harvest(Harvest.LUCK, 1).harvest(Harvest.SILK, 0)
                        .weapon(Weapon.STUN, 2).weapon(Weapon.VAMPIRE, 0).weapon(Weapon.BEHEADER, 0));
    }

    public static Item bismuthAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.BISMUTH, 25.0F,
                b -> b.area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1).area(Area.RECURSION, 1)
                        .harvest(Harvest.LUCK, 1).harvest(Harvest.SILK, 0)
                        .weapon(Weapon.STUN, 3).weapon(Weapon.VAMPIRE, 1).weapon(Weapon.BEHEADER, 0));
    }

    public static Item volcanicPick() {
        return NtmAbilityDiggerItem.miner(NtmTiers.VOLCANIC, 15.0F,
                b -> b.area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1).area(Area.RECURSION, 1)
                        .harvest(Harvest.SMELTER, 0).harvest(Harvest.LUCK, 2).harvest(Harvest.SILK, 0)
                        .weapon(Weapon.FIRE, 0).weapon(Weapon.VAMPIRE, 0).weapon(Weapon.BEHEADER, 0));
    }

    public static Item volcanicAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.VOLCANIC, 25.0F,
                b -> b.area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1).area(Area.RECURSION, 1)
                        .harvest(Harvest.SMELTER, 0).harvest(Harvest.LUCK, 2).harvest(Harvest.SILK, 0)
                        .weapon(Weapon.FIRE, 1).weapon(Weapon.VAMPIRE, 1).weapon(Weapon.BEHEADER, 0));
    }

    public static Item chlorophytePick() {
        return NtmAbilityDiggerItem.miner(NtmTiers.CHLOROPHYTE, 20.0F,
                b -> b.area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1).area(Area.RECURSION, 1)
                        .harvest(Harvest.LUCK, 3)
                        .weapon(Weapon.STUN, 3).weapon(Weapon.VAMPIRE, 2).weapon(Weapon.BEHEADER, 0));
    }

    public static Item chlorophyteAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.CHLOROPHYTE, 50.0F,
                b -> b.area(Area.HAMMER, 1).area(Area.HAMMER_FLAT, 1).area(Area.RECURSION, 1)
                        .harvest(Harvest.LUCK, 3)
                        .weapon(Weapon.STUN, 4).weapon(Weapon.VAMPIRE, 3).weapon(Weapon.BEHEADER, 0));
    }

    public static Item mesePick() {
        return NtmAbilityDiggerItem.miner(NtmTiers.MESE, 35.0F,
                b -> b.area(Area.HAMMER, 2).area(Area.HAMMER_FLAT, 2).area(Area.RECURSION, 2)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 5)
                        .weapon(Weapon.STUN, 3).weapon(Weapon.BEHEADER, 0));
    }

    public static Item meseAxe() {
        return NtmAbilityAxeItem.create(NtmTiers.MESE, 75.0F,
                b -> b.area(Area.HAMMER, 2).area(Area.HAMMER_FLAT, 2).area(Area.RECURSION, 2)
                        .harvest(Harvest.SILK, 0).harvest(Harvest.LUCK, 5)
                        .weapon(Weapon.STUN, 4).weapon(Weapon.BEHEADER, 0));
    }
}
