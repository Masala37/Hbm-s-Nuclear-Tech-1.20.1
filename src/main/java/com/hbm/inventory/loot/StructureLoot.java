package com.hbm.inventory.loot;

import com.hbm.blocks.generic.LootCrateBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 1.7 {@code ItemPool} / {@code LootGenerator} rows that resolve on this port. Missing items are skipped.
 */
public final class StructureLoot {
    public record Entry(String id, int min, int max, int weight) {
    }

    private static final Map<String, List<Entry>> POOLS = new HashMap<>();
    private static final String BACKUP = "BACKUP";

    static {
        pool("BACKUP",
                e("minecraft:bread", 1, 3, 8),
                e("minecraft:stick", 1, 4, 6),
                e("hbm:scrap", 1, 3, 10),
                e("hbm:dust", 1, 2, 6));
        pool("POOL_GENERIC",
                e("minecraft:bread", 1, 5, 8),
                e("hbm:twinkie", 1, 3, 6),
                e("minecraft:iron_ingot", 2, 6, 10),
                e("hbm:ingot_steel", 2, 5, 7),
                e("hbm:ingot_beryllium", 1, 2, 4),
                e("hbm:ingot_titanium", 1, 1, 3),
                e("hbm:circuit_vacuum_tube", 1, 1, 5),
                e("hbm:scrap", 1, 3, 10),
                e("hbm:dust", 2, 4, 9),
                e("hbm:bottle_nuka", 1, 3, 4),
                e("hbm:cap_nuka", 1, 15, 7));
        pool("POOL_ANTENNA",
                e("hbm:ingot_steel", 1, 3, 6),
                e("hbm:circuit_vacuum_tube", 1, 2, 5),
                e("hbm:circuit_analog", 1, 1, 3),
                e("hbm:wire_red_copper", 2, 8, 5),
                e("hbm:scrap", 1, 4, 8),
                e("hbm:bottle_nuka", 1, 2, 4));
        pool("POOL_EXPENSIVE",
                e("minecraft:diamond", 1, 2, 3),
                e("hbm:ingot_desh", 1, 2, 2),
                e("hbm:circuit_chip", 1, 2, 4),
                e("hbm:ingot_schrabidium", 1, 1, 1),
                e("minecraft:gold_ingot", 2, 6, 5));
        pool("POOL_VERTIBIRD",
                e("hbm:ingot_steel", 2, 6, 8),
                e("hbm:plate_steel", 1, 4, 6),
                e("hbm:circuit_analog", 1, 2, 4),
                e("hbm:motor", 1, 1, 3),
                e("hbm:bottle_nuka", 1, 3, 6),
                e("hbm:cap_nuka", 4, 16, 5),
                e("hbm:syringe_metal_stimpak", 1, 2, 4));
        pool("POOL_MACHINE_PARTS",
                e("hbm:plate_steel", 1, 5, 5),
                e("hbm:plate_polymer", 1, 6, 5),
                e("hbm:coil_tungsten", 1, 2, 5),
                e("hbm:motor", 1, 2, 4),
                e("hbm:coil_copper", 1, 3, 4),
                e("hbm:circuit_vacuum_tube", 1, 2, 4),
                e("hbm:circuit_pcb", 1, 3, 5),
                e("hbm:circuit_capacitor", 1, 1, 3));
        pool("POOL_NUKE_FUEL",
                e("hbm:billet_uranium", 1, 4, 4),
                e("hbm:billet_uranium_fuel", 1, 3, 5),
                e("hbm:billet_mox_fuel", 1, 3, 5),
                e("hbm:billet_beryllium", 1, 1, 1),
                e("hbm:nugget_u233", 1, 1, 1),
                e("hbm:ingot_graphite", 1, 4, 3),
                e("hbm:screwdriver", 1, 1, 2));
        pool("POOL_SILO",
                e("hbm:missile_generic", 1, 1, 4),
                e("hbm:missile_incendiary", 1, 1, 4),
                e("hbm:designator", 1, 1, 5),
                e("hbm:bottle_nuka", 1, 3, 10));
        pool("POOL_OFFICE_TRASH",
                e("minecraft:paper", 1, 12, 10),
                e("minecraft:book", 1, 3, 4),
                e("hbm:twinkie", 1, 2, 6),
                e("hbm:cap_nuka", 1, 16, 2));
        pool("POOL_FILING_CABINET",
                e("minecraft:paper", 1, 12, 240),
                e("minecraft:book", 1, 3, 90),
                e("minecraft:map", 1, 1, 50),
                e("minecraft:writable_book", 1, 1, 30),
                e("hbm:cigarette", 1, 16, 20),
                e("hbm:dust", 1, 1, 40),
                e("hbm:dust_tiny", 1, 3, 75),
                e("hbm:ink", 1, 1, 1),
                e("hbm:screwdriver", 1, 1, 10),
                e("hbm:blueprint_folder", 1, 1, 5),
                e("hbm:coin_token", 1, 1, 30));
        pool("POOL_SOLID_FUEL",
                e("minecraft:coal", 4, 16, 10),
                e("minecraft:charcoal", 4, 12, 6),
                e("hbm:lignite", 4, 12, 5));
        pool("POOL_VAULT_LAB",
                e("hbm:circuit_chip", 1, 3, 4),
                e("hbm:ingot_polymer", 1, 4, 5),
                e("hbm:niter", 2, 6, 4),
                e("hbm:sulfur", 2, 6, 4));
        pool("POOL_METEOR_SAFE",
                e("hbm:ingot_meteorite", 1, 3, 6),
                e("hbm:fragment_meteorite", 2, 8, 8),
                e("minecraft:iron_ingot", 2, 6, 6),
                e("hbm:ingot_steel", 1, 4, 5));
        pool("POOL_OIL_RIG",
                e("hbm:canister_full", 1, 2, 4),
                e("hbm:ingot_steel", 2, 6, 6),
                e("hbm:motor", 1, 1, 3),
                e("hbm:bottle_nuka", 1, 3, 5));
        pool("POOL_RTG",
                e("hbm:pellet_rtg_weak", 1, 1, 4),
                e("hbm:pellet_rtg", 1, 1, 2),
                e("hbm:ingot_lead", 2, 6, 5));
        pool("POOL_REPAIR_MATERIALS",
                e("hbm:plate_steel", 2, 8, 6),
                e("hbm:ingot_steel", 2, 6, 6),
                e("hbm:scrap", 2, 8, 8),
                e("minecraft:iron_ingot", 2, 8, 5));
        pool("POOL_METEORITE_TREASURE",
                e("hbm:cobalt_pickaxe", 1, 1, 10),
                e("hbm:ingot_zirconium", 1, 16, 10),
                e("hbm:ingot_niobium", 1, 16, 10),
                e("hbm:ingot_cobalt", 1, 16, 10),
                e("hbm:ingot_boron", 1, 16, 10),
                e("hbm:ingot_starmetal", 1, 1, 5),
                e("hbm:crystal_gold", 1, 4, 10),
                e("hbm:circuit_vacuum_tube", 4, 8, 10),
                e("hbm:circuit_chip", 2, 4, 10),
                e("hbm:crate_can", 1, 3, 10),
                e("hbm:pill_herbal", 1, 2, 10),
                e("hbm:serum", 1, 1, 5),
                e("hbm:heart_piece", 1, 1, 5),
                e("hbm:scrumpy", 1, 1, 5),
                e("hbm:launch_code_piece", 1, 1, 5),
                e("hbm:egg_glyphid", 1, 1, 5),
                e("hbm:gem_alexandrite", 1, 1, 1),
                e("hbm:blueprint_folder", 1, 1, 1));
        pool("POOL_BLUEPRINTS",
                e("minecraft:paper", 1, 4, 8),
                e("minecraft:book", 1, 1, 4));
        pool("POOL_WEAPONS",
                e("minecraft:iron_ingot", 2, 6, 6),
                e("hbm:ingot_steel", 1, 4, 5),
                e("hbm:scrap", 1, 4, 8));
        pool("POOL_AMMO",
                e("hbm:cap_nuka", 8, 24, 10),
                e("hbm:syringe_metal_stimpak", 1, 2, 4));
        pool("POOL_SUPPLIES",
                e("hbm:syringe_metal_stimpak", 1, 3, 10),
                e("hbm:bottle_nuka", 1, 3, 8),
                e("minecraft:bread", 1, 4, 6),
                e("hbm:scrap", 1, 3, 5));
        pool("POOL_BLACK_PART",
                e("hbm:ingot_steel", 1, 3, 5),
                e("hbm:scrap", 2, 6, 8));
        pool("LOOT_BOOKLET",
                e("minecraft:book", 1, 1, 1),
                e("minecraft:paper", 2, 6, 4));
        pool("LOOT_MEDICINE",
                e("hbm:syringe_metal_stimpak", 1, 1, 10),
                e("hbm:syringe_metal_medx", 1, 1, 5),
                e("hbm:syringe_metal_psycho", 1, 1, 5));
        pool("POOL_PILE_MED_PILLS",
                e("hbm:radaway", 1, 1, 10),
                e("hbm:radx", 1, 1, 10),
                e("hbm:iv_blood", 1, 1, 15),
                e("hbm:siox", 1, 1, 5));
        pool("LOOT_CAPSTASH",
                e("hbm:cap_nuka", 4, 4, 20),
                e("hbm:cap_quantum", 4, 4, 3),
                e("hbm:cap_sparkle", 4, 4, 1));
        pool("LOOT_CAPNUKE",
                e("hbm:cap_nuka", 2, 2, 8),
                e("hbm:syringe_metal_stimpak", 1, 1, 3));
        pool("LOOT_BONES",
                e("minecraft:bone", 1, 1, 10),
                e("minecraft:rotten_flesh", 1, 1, 5),
                e("hbm:biomass", 1, 1, 2));
        pool("POOL_PILE_BONES",
                e("minecraft:bone", 1, 1, 10),
                e("minecraft:rotten_flesh", 1, 1, 5),
                e("hbm:biomass", 1, 1, 2));
        pool("LOOT_METEOR",
                e("hbm:fragment_meteorite", 1, 4, 8),
                e("hbm:ingot_meteorite", 1, 1, 3));
        pool("LOOT_SHIT",
                e("hbm:scrap", 1, 5, 20),
                e("hbm:dust", 1, 3, 40),
                e("hbm:dust_tiny", 1, 7, 40),
                e("hbm:cap_nuka", 0, 8, 15),
                e("minecraft:string", 0, 1, 15),
                e("minecraft:rotten_flesh", 1, 2, 4));
        pool("LOOT_MECHANICAL",
                e("hbm:defuser", 1, 1, 30),
                e("hbm:screwdriver", 1, 1, 30),
                e("hbm:plate_steel", 3, 8, 40),
                e("hbm:coil_copper", 2, 5, 40),
                e("hbm:coil_tungsten", 2, 5, 40));
        pool("LOOT_GEAR",
                e("hbm:defuser", 1, 1, 40),
                e("hbm:screwdriver", 1, 1, 30),
                e("hbm:canteen_vodka", 1, 1, 40));
        pool("LOOT_GLYPHID_HIVE",
                e("minecraft:iron_ingot", 1, 3, 10),
                e("hbm:ingot_steel", 1, 2, 10),
                e("hbm:scrap", 3, 6, 10),
                e("hbm:bottle_nuka", 1, 2, 20),
                e("hbm:syringe_metal_stimpak", 1, 1, 5));
        pool("POOL_PILE_MAKESHIFT_GUN",
                e("hbm:gun_maresleg", 1, 1, 10));
        pool("POOL_PILE_MAKESHIFT_WRENCH",
                e("hbm:wrench", 1, 1, 10));
        pool("POOL_PILE_MAKESHIFT_PLATES",
                e("hbm:plate_steel", 1, 1, 10));
        pool("POOL_PILE_MAKESHIFT_WIRE",
                e("hbm:wire_aluminium", 1, 1, 10));
        pool("POOL_PILE_NUKE_STORAGE",
                e("hbm:nugget_u233", 1, 1, 10),
                e("hbm:billet_uranium", 1, 1, 50));
        pool("CRATE_SUPPLY",
                e("hbm:syringe_metal_stimpak", 1, 1, 10),
                e("hbm:bottle_nuka", 1, 2, 6),
                e("hbm:scrap", 1, 2, 4));
        pool("CRATE_WEAPON",
                e("minecraft:iron_ingot", 2, 6, 8),
                e("hbm:ingot_steel", 1, 4, 6),
                e("hbm:scrap", 1, 3, 6));
        pool("CRATE_LEAD",
                e("hbm:ingot_uranium", 1, 2, 10),
                e("hbm:nugget_uranium", 1, 3, 10),
                e("hbm:pellet_rtg_weak", 1, 1, 7),
                e("hbm:powder_yellowcake", 1, 2, 10));
        pool("CRATE_METAL",
                e("hbm:machine_press", 1, 1, 10),
                e("hbm:machine_wood_burner", 1, 1, 10),
                e("hbm:diesel_generator", 1, 1, 8),
                e("hbm:machine_rtg", 1, 1, 4),
                e("hbm:red_pylon", 1, 1, 9),
                e("hbm:electric_furnace", 1, 1, 8),
                e("hbm:machine_assembly_machine", 1, 1, 10),
                e("hbm:motor", 1, 1, 8),
                e("hbm:coil_copper", 1, 2, 10));
        pool("CRATE_RED",
                e("hbm:broadcaster_pc", 1, 1, 1),
                e("hbm:scrap", 1, 1, 1));
        pool("CRATE_AMMO",
                e("hbm:cap_nuka", 12, 32, 10),
                e("hbm:syringe_metal_stimpak", 1, 3, 6));
        pool("CRATE_CAN",
                e("hbm:canned_beef", 1, 1, 1),
                e("hbm:canned_tuna", 1, 1, 1),
                e("hbm:canned_mystery", 1, 1, 1),
                e("hbm:canned_pashtet", 1, 1, 1),
                e("hbm:canned_cheese", 1, 1, 1),
                e("hbm:canned_slime", 1, 1, 1),
                e("hbm:canned_milk", 1, 1, 1),
                e("hbm:canned_ass", 1, 1, 1),
                e("hbm:canned_pizza", 1, 1, 1),
                e("hbm:canned_tube", 1, 1, 1),
                e("hbm:canned_tomato", 1, 1, 1),
                e("hbm:canned_asbestos", 1, 1, 1),
                e("hbm:canned_bhole", 1, 1, 1),
                e("hbm:canned_hotdogs", 1, 1, 1),
                e("hbm:canned_leftovers", 1, 1, 1),
                e("hbm:canned_yogurt", 1, 1, 1),
                e("hbm:canned_stew", 1, 1, 1),
                e("hbm:canned_chinese", 1, 1, 1),
                e("hbm:canned_oil", 1, 1, 1),
                e("hbm:canned_fist", 1, 1, 1),
                e("hbm:canned_spam", 1, 1, 1),
                e("hbm:canned_fried", 1, 1, 1),
                e("hbm:canned_napalm", 1, 1, 1),
                e("hbm:canned_diesel", 1, 1, 1),
                e("hbm:canned_kerosene", 1, 1, 1),
                e("hbm:canned_recursion", 1, 1, 1),
                e("hbm:canned_bark", 1, 1, 1),
                e("hbm:can_smart", 1, 1, 1),
                e("hbm:can_creature", 1, 1, 1),
                e("hbm:can_redbomb", 1, 1, 1),
                e("hbm:can_mrsugar", 1, 1, 1),
                e("hbm:can_overcharge", 1, 1, 1),
                e("hbm:can_luna", 1, 1, 1),
                e("hbm:can_breen", 1, 1, 1),
                e("hbm:can_bepis", 1, 1, 1),
                e("hbm:pudding", 1, 1, 1));
        alias("POOL_NUKE_TRASH", "POOL_GENERIC");
        alias("POOL_NUKE_MISC", "POOL_GENERIC");
        alias("POOL_SPACESHIP", "POOL_VERTIBIRD");
        alias("POOL_VAULT_LOCKERS", "POOL_OFFICE_TRASH");
        alias("LOOT_MAKESHIFT_GUN", "POOL_WEAPONS");
        alias("LOOT_FLAREGUN", "POOL_WEAPONS");
        alias("LOOT_NUKE_STORAGE", "POOL_NUKE_FUEL");
        alias("POOL_PILE_HIVE", "LOOT_GLYPHID_HIVE");
        alias("POOL_PILE_MED_SYRINGE", "LOOT_MEDICINE");
        alias("POOL_PILE_CAPS", "LOOT_CAPSTASH");
        alias("POOL_PILE_OF_GARBAGE", "LOOT_SHIT");
        alias("POOL_PILE_MECHANICAL", "LOOT_MECHANICAL");
        alias("POOL_PILE_GEAR", "LOOT_GEAR");
        alias("POOL_VAULT_RUSTY", "POOL_EXPENSIVE");
        alias("POOL_VAULT_STANDARD", "POOL_EXPENSIVE");
        alias("POOL_VAULT_REINFORCED", "POOL_EXPENSIVE");
        alias("POOL_VAULT_UNBREAKABLE", "POOL_EXPENSIVE");
        pool("POOL_LAUNCH_KEY",
                e("hbm:launch_key", 1, 1, 1));
    }

    private StructureLoot() {
    }

    /**
     * 1.7 {@code LootGenerator.applyLoot} pile sizes. Missing items are skipped.
     */
    public static List<ItemStack> decoLoot(String poolName, RandomSource random) {
        String name = poolName == null ? "" : poolName;
        List<ItemStack> out = new ArrayList<>();
        switch (name) {
            case "LOOT_BOOKLET" -> add(out, pick(resolve("LOOT_BOOKLET"), random));
            case "LOOT_CAPNUKE" -> {
                for (int i = 0; i < 4; i++) {
                    add(out, stackOrEmpty("hbm:cap_nuka", 2));
                }
                for (int i = 0; i < 2; i++) {
                    add(out, stackOrEmpty("hbm:syringe_metal_stimpak", 1));
                }
                for (int i = 0; i < 6; i++) {
                    add(out, stackOrEmpty("hbm:cap_nuka", 2));
                }
            }
            case "LOOT_MEDICINE" -> {
                for (int i = 0; i < 4; i++) {
                    add(out, pick(resolve("LOOT_MEDICINE"), random));
                }
                add(out, pick(resolve("POOL_PILE_MED_PILLS"), random));
            }
            case "LOOT_CAPSTASH" -> {
                for (int i = 0; i < 9; i++) {
                    int count = random.nextInt(5) + 3;
                    for (int k = 0; k < count; k++) {
                        add(out, pick(resolve("LOOT_CAPSTASH"), random));
                    }
                }
            }
            case "LOOT_MAKESHIFT_GUN" -> {
                boolean r = random.nextBoolean();
                if (r) {
                    add(out, pick(resolve("POOL_PILE_MAKESHIFT_GUN"), random));
                }
                if (!r || random.nextBoolean()) {
                    add(out, pick(resolve("POOL_PILE_MAKESHIFT_WRENCH"), random));
                }
                int plates = random.nextInt(2) + 1;
                for (int i = 0; i < plates; i++) {
                    add(out, pick(resolve("POOL_PILE_MAKESHIFT_PLATES"), random));
                }
                int wire = random.nextInt(2) + 2;
                for (int i = 0; i < wire; i++) {
                    add(out, pick(resolve("POOL_PILE_MAKESHIFT_WIRE"), random));
                }
            }
            case "LOOT_NUKE_STORAGE" -> {
                for (int i = 0; i < 16; i++) {
                    if (random.nextBoolean()) {
                        add(out, pick(resolve("POOL_PILE_NUKE_STORAGE"), random));
                    }
                }
            }
            case "LOOT_BONES" -> rolls(out, "LOOT_BONES", 3 + random.nextInt(3), random);
            case "LOOT_GLYPHID_HIVE" -> rolls(out, "LOOT_GLYPHID_HIVE", 3 + random.nextInt(3), random);
            case "LOOT_METEOR" -> rolls(out, "LOOT_METEOR", 2, random);
            case "LOOT_FLAREGUN" -> rolls(out, "LOOT_FLAREGUN", 3 + random.nextInt(3), random);
            case "LOOT_SHIT" -> rolls(out, "LOOT_SHIT", 3 + random.nextInt(3), random);
            case "LOOT_MECHANICAL" -> rolls(out, "LOOT_MECHANICAL", 1 + random.nextInt(6), random);
            case "LOOT_GEAR" -> rolls(out, "LOOT_GEAR", 1 + random.nextInt(6), random);
            default -> {
                if (name.startsWith("LOOT_") || name.startsWith("POOL_PILE_")) {
                    rolls(out, name, 3 + random.nextInt(3), random);
                } else {
                    return generate(name, 1, random);
                }
            }
        }
        if (out.isEmpty()) {
            return generate(name.isEmpty() ? "BACKUP" : name, 1, random);
        }
        return out;
    }

    public static List<ItemStack> generate(String poolName, int count, Random random) {
        return generate(poolName, count, RandomSource.create(random.nextLong()));
    }

    public static List<ItemStack> generate(String poolName, int count, RandomSource random) {
        if (count <= 0) {
            return List.of();
        }
        List<Entry> pool = resolve(poolName);
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ItemStack stack = pick(pool, random);
            if (!stack.isEmpty()) {
                out.add(stack);
            }
        }
        if (out.isEmpty()) {
            ItemStack backup = pick(POOLS.get(BACKUP), random);
            if (!backup.isEmpty()) {
                out.add(backup);
            }
        }
        return out;
    }

    public static void dropCrate(Level level, BlockPos pos, LootCrateBlock.Kind kind, RandomSource random) {
        String pool = switch (kind) {
            case WEAPON -> "CRATE_WEAPON";
            case LEAD -> "CRATE_LEAD";
            case METAL -> "CRATE_METAL";
            case RED -> "CRATE_RED";
            case AMMO -> "CRATE_AMMO";
            case CAN -> "CRATE_CAN";
            default -> "CRATE_SUPPLY";
        };
        int rolls = 3 + random.nextInt(3);
        if (kind == LootCrateBlock.Kind.WEAPON) {
            rolls = 1 + random.nextInt(2);
        }
        if (kind == LootCrateBlock.Kind.CAN) {
            rolls = 5 + random.nextInt(4);
        }
        if (kind == LootCrateBlock.Kind.RED) {
            rolls = Math.max(1, resolve("CRATE_RED").size());
        }
        List<ItemStack> stacks = generate(pool, rolls, random);
        if (kind == LootCrateBlock.Kind.RED) {
            stacks = new ArrayList<>();
            for (Entry entry : resolve("CRATE_RED")) {
                ItemStack stack = stack(entry, random);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
        }
        for (ItemStack stack : stacks) {
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
        }
    }

    private static void rolls(List<ItemStack> out, String pool, int count, RandomSource random) {
        for (int i = 0; i < count; i++) {
            add(out, pick(resolve(pool), random));
        }
    }

    private static void add(List<ItemStack> out, ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            out.add(stack);
        }
    }

    private static ItemStack stackOrEmpty(String id, int count) {
        Item found = item(id);
        if (found == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(found, Math.max(1, count));
    }

    private static List<Entry> resolve(String name) {
        if (name == null || name.isEmpty()) {
            return POOLS.get(BACKUP);
        }
        return POOLS.getOrDefault(name, POOLS.get(BACKUP));
    }

    private static ItemStack pick(List<Entry> pool, RandomSource random) {
        int total = 0;
        List<Entry> present = new ArrayList<>();
        for (Entry entry : pool) {
            if (item(entry.id) != null) {
                present.add(entry);
                total += entry.weight;
            }
        }
        if (total <= 0) {
            return ItemStack.EMPTY;
        }
        int weight = random.nextInt(total);
        for (Entry entry : present) {
            weight -= entry.weight;
            if (weight < 0) {
                return stack(entry, random);
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack stack(Entry entry, RandomSource random) {
        Item item = item(entry.id);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        int count = entry.min;
        if (entry.max > entry.min) {
            count += random.nextInt(entry.max - entry.min + 1);
        }
        return new ItemStack(item, Math.max(1, count));
    }

    private static Item item(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null || !ForgeRegistries.ITEMS.containsKey(key)) {
            return null;
        }
        return ForgeRegistries.ITEMS.getValue(key);
    }

    private static void pool(String name, Entry... entries) {
        POOLS.put(name, List.of(entries));
    }

    private static void alias(String name, String target) {
        POOLS.put(name, POOLS.get(target));
    }

    private static Entry e(String id, int min, int max, int weight) {
        return new Entry(id, min, max, weight);
    }
}
