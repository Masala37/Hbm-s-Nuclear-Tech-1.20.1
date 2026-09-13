package com.hbm.world.gen.nbt;

import java.util.Locale;
import java.util.Map;

/**
 * 1.7 NBT palette names → 1.20 registry ids. Pure strings so unit tests can run without Forge.
 */
public final class StructureBlockNames {
    private static final Map<String, String> VANILLA = Map.ofEntries(
            Map.entry("grass", "grass_block"),
            Map.entry("web", "cobweb"),
            Map.entry("fence", "oak_fence"),
            Map.entry("fence_gate", "oak_fence_gate"),
            Map.entry("brick_block", "bricks"),
            Map.entry("noteblock", "note_block"),
            Map.entry("deadbush", "dead_bush"),
            Map.entry("waterlily", "lily_pad"),
            Map.entry("mob_spawner", "spawner"),
            Map.entry("nether_brick", "nether_bricks"),
            Map.entry("red_nether_brick", "red_nether_bricks"),
            Map.entry("end_bricks", "end_stone_bricks"),
            Map.entry("melon_block", "melon"),
            Map.entry("snow_layer", "snow"),
            Map.entry("reeds", "sugar_cane"),
            Map.entry("lit_pumpkin", "jack_o_lantern"),
            Map.entry("yellow_flower", "dandelion"),
            Map.entry("hardened_clay", "terracotta"),
            Map.entry("grass_path", "dirt_path"),
            Map.entry("magma", "magma_block"),
            Map.entry("quartz_ore", "nether_quartz_ore"),
            Map.entry("lit_furnace", "furnace"),
            Map.entry("unlit_redstone_torch", "redstone_torch"),
            Map.entry("unpowered_repeater", "repeater"),
            Map.entry("powered_repeater", "repeater"),
            Map.entry("unpowered_comparator", "comparator"),
            Map.entry("powered_comparator", "comparator"),
            Map.entry("wooden_door", "oak_door"),
            Map.entry("wooden_slab", "oak_slab"),
            Map.entry("double_wooden_slab", "oak_slab"),
            Map.entry("trapdoor", "oak_trapdoor"),
            Map.entry("wooden_pressure_plate", "oak_pressure_plate"),
            Map.entry("wooden_button", "oak_button"),
            Map.entry("wall_sign", "oak_wall_sign"),
            Map.entry("standing_sign", "oak_sign"),
            Map.entry("stone_stairs", "cobblestone_stairs"),
            Map.entry("portal", "nether_portal"),
            Map.entry("lit_redstone_lamp", "redstone_lamp"),
            Map.entry("daylight_detector_inverted", "daylight_detector"),
            Map.entry("speckled_melon", "glistering_melon_slice")
    );

    /** 1.7 palette ids that this port registers under a different path. */
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("ore_coal_oil", "minecraft:coal_ore"),
            Map.entry("tape_recorder", "hbm:deco_tape_recorder"),
            Map.entry("pole_satellite_receiver", "hbm:deco_satellite_receiver"),
            Map.entry("floodlight", "hbm:flood_lamp"),
            Map.entry("spotlight_halogen", "hbm:flood_lamp"),
            Map.entry("spotlight_fluoro", "hbm:fluorescent_lamp"),
            Map.entry("spotlight_incandescent", "hbm:cage_lamp"),
            Map.entry("machine_rtg_grey", "hbm:machine_rtg"),
            Map.entry("machine_boiler_off", "hbm:machine_boiler"),
            Map.entry("machine_electric_furnace_off", "hbm:electric_furnace"),
            Map.entry("machine_diesel", "hbm:diesel_generator"),
            Map.entry("tnt_ntm", "hbm:tnt"),
            Map.entry("red_barrel", "hbm:barrel_red"),
            Map.entry("yellow_barrel", "hbm:barrel_yellow"),
            Map.entry("pink_barrel", "hbm:barrel_pink"),
            Map.entry("lox_barrel", "hbm:barrel_lox"),
            Map.entry("vitrified_barrel", "hbm:barrel_vitrified"),
            Map.entry("dungeon_chain", "hbm:chain"),
            Map.entry("fluid_duct_gauge", "hbm:fluid_duct_neo"),
            Map.entry("concrete_double_slab", "hbm:concrete_slab"),
            Map.entry("brick_double_slab", "hbm:brick_slab"),
            Map.entry("concrete_brick_double_slab", "hbm:concrete_brick_slab"),
            Map.entry("machine_transformer", "hbm:machine_transformer_iron"),
            Map.entry("red_cable_gauge", "hbm:cable_gauge"),
            Map.entry("block_electrical_scrap", "hbm:electrical_scrap"),
            Map.entry("radio_torch_sender", "hbm:rtty_sender_off"),
            Map.entry("radio_torch_receiver", "hbm:rtty_rec_off"),
            Map.entry("reeds", "hbm:reeds_mid")
    );
    private static final String[] CRT = {"crt_clean", "crt_broken", "crt_blinking", "crt_bsod"};
    private static final String[] TOASTER = {"toaster_iron", "toaster_steel", "toaster_wood"};

    private static final String[] WOOD = {
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak"
    };
    private static final String[] DYE = {
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
    };
    private static final String[] STONE_SLAB = {
            "smooth_stone", "sandstone", "petrified_oak", "cobblestone", "brick", "stone_brick",
            "nether_brick", "quartz"
    };

    private StructureBlockNames() {
    }

    public static String portId(String rawName) {
        return portId(rawName, 0);
    }

    public static String portId(String rawName, int meta) {
        if (rawName == null || rawName.isEmpty()) {
            return "minecraft:air";
        }
        String name = rawName.toLowerCase(Locale.ROOT);
        if (name.startsWith("hbm:tile.")) {
            return hbmPath(sanitize(name.substring("hbm:tile.".length())), meta);
        }
        if (name.startsWith("hbm:")) {
            String path = name.substring(4);
            if (path.startsWith("tile.")) {
                path = path.substring(5);
            }
            return hbmPath(sanitize(path), meta);
        }
        String path = name;
        if (path.startsWith("minecraft:")) {
            path = path.substring("minecraft:".length());
        }
        return flattenVanilla(path, meta);
    }

    public static String flattenVanilla(String path, int meta) {
        path = VANILLA.getOrDefault(path, path);
        int dye = clamp(meta, DYE.length);
        int wood = clamp(meta & 7, WOOD.length);
        return switch (path) {
            case "planks" -> "minecraft:" + WOOD[wood] + "_planks";
            case "log" -> "minecraft:" + WOOD[clamp(meta & 3, 4)] + "_log";
            case "log2" -> "minecraft:" + WOOD[4 + clamp(meta & 3, 2)] + "_log";
            case "leaves" -> "minecraft:" + WOOD[clamp(meta & 3, 4)] + "_leaves";
            case "leaves2" -> "minecraft:" + WOOD[4 + clamp(meta & 3, 2)] + "_leaves";
            case "sapling" -> "minecraft:" + WOOD[clamp(meta, WOOD.length)] + "_sapling";
            case "wool" -> "minecraft:" + DYE[dye] + "_wool";
            case "carpet" -> "minecraft:" + DYE[dye] + "_carpet";
            case "stained_glass" -> "minecraft:" + DYE[dye] + "_stained_glass";
            case "stained_glass_pane" -> "minecraft:" + DYE[dye] + "_stained_glass_pane";
            case "stained_hardened_clay" -> "minecraft:" + DYE[dye] + "_terracotta";
            case "concrete" -> "minecraft:" + DYE[dye] + "_concrete";
            case "concrete_powder" -> "minecraft:" + DYE[dye] + "_concrete_powder";
            case "stonebrick" -> switch (meta & 3) {
                case 1 -> "minecraft:mossy_stone_bricks";
                case 2 -> "minecraft:cracked_stone_bricks";
                case 3 -> "minecraft:chiseled_stone_bricks";
                default -> "minecraft:stone_bricks";
            };
            case "stone_slab", "double_stone_slab" -> slab(STONE_SLAB[clamp(meta & 7, STONE_SLAB.length)]);
            case "wooden_slab", "double_wooden_slab" -> "minecraft:" + WOOD[wood] + "_slab";
            case "tallgrass" -> meta == 2 ? "minecraft:fern" : "minecraft:short_grass";
            case "red_flower" -> redFlower(meta);
            case "double_plant" -> doublePlant(meta);
            case "skull" -> "minecraft:skeleton_skull";
            case "bed" -> "minecraft:red_bed";
            case "wooden_door" -> "minecraft:oak_door";
            case "dirt" -> switch (meta & 3) {
                case 1 -> "minecraft:coarse_dirt";
                case 2 -> "minecraft:podzol";
                default -> "minecraft:dirt";
            };
            default -> path.contains(":") ? path : "minecraft:" + path;
        };
    }

    private static String hbmPath(String path, int meta) {
        String alias = ALIASES.get(path);
        if (alias != null) {
            return alias;
        }
        return switch (path) {
            case "deco_crt" -> "hbm:" + CRT[clamp(Math.abs(meta) % 16 / 4, CRT.length)];
            case "deco_toaster" -> "hbm:" + TOASTER[clamp(Math.abs(meta) % 12 / 4, TOASTER.length)];
            default -> "hbm:" + path;
        };
    }

    private static String slab(String kind) {
        return switch (kind) {
            case "smooth_stone" -> "minecraft:smooth_stone_slab";
            case "sandstone" -> "minecraft:sandstone_slab";
            case "petrified_oak" -> "minecraft:petrified_oak_slab";
            case "cobblestone" -> "minecraft:cobblestone_slab";
            case "brick" -> "minecraft:brick_slab";
            case "stone_brick" -> "minecraft:stone_brick_slab";
            case "nether_brick" -> "minecraft:nether_brick_slab";
            case "quartz" -> "minecraft:quartz_slab";
            default -> "minecraft:smooth_stone_slab";
        };
    }

    private static String redFlower(int meta) {
        return switch (meta) {
            case 1 -> "minecraft:blue_orchid";
            case 2 -> "minecraft:allium";
            case 3 -> "minecraft:azure_bluet";
            case 4 -> "minecraft:red_tulip";
            case 5 -> "minecraft:orange_tulip";
            case 6 -> "minecraft:white_tulip";
            case 7 -> "minecraft:pink_tulip";
            case 8 -> "minecraft:oxeye_daisy";
            default -> "minecraft:poppy";
        };
    }

    private static String doublePlant(int meta) {
        return switch (meta & 7) {
            case 1 -> "minecraft:lilac";
            case 2 -> "minecraft:tall_grass";
            case 3 -> "minecraft:large_fern";
            case 4 -> "minecraft:rose_bush";
            case 5 -> "minecraft:peony";
            default -> "minecraft:sunflower";
        };
    }

    private static String sanitize(String path) {
        return path.toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private static int clamp(int meta, int length) {
        if (meta < 0) {
            return 0;
        }
        return Math.min(meta, length - 1);
    }
}
