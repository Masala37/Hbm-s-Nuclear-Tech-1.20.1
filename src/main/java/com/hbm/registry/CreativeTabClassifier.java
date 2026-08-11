package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps registry paths to the nine legacy HBM creative tabs.
 * Prefers the extracted 1.7.10 assignment table; falls back to prefix heuristics.
 */
public final class CreativeTabClassifier {
    public enum Kind {
        PARTS,
        CONTROL,
        TEMPLATE,
        BLOCKS,
        MACHINE,
        NUKE,
        MISSILE,
        WEAPON,
        CONSUMABLE,
        HIDDEN
    }

    private static volatile Map<String, Kind> legacyMap;
    private static final Map<String, Kind> OVERRIDES = buildOverrides();

    private CreativeTabClassifier() {
    }

    public static Kind classify(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null || !RefStrings.MODID.equals(key.getNamespace())) {
            return Kind.HIDDEN;
        }
        return classify(key.getPath());
    }

    public static Kind classify(String path) {
        if (path == null || path.isEmpty()) {
            return Kind.HIDDEN;
        }
        String id = path.toLowerCase(Locale.ROOT);

        Kind override = OVERRIDES.get(id);
        if (override != null) {
            return override;
        }

        Kind legacy = legacy().get(id);
        if (legacy != null) {
            return legacy;
        }

        return heuristic(id);
    }

    private static Map<String, Kind> legacy() {
        Map<String, Kind> local = legacyMap;
        if (local == null) {
            synchronized (CreativeTabClassifier.class) {
                local = legacyMap;
                if (local == null) {
                    local = loadLegacyMap();
                    legacyMap = local;
                }
            }
        }
        return local;
    }

    private static Kind heuristic(String id) {
        if (id.endsWith("_bucket") || id.startsWith("bucket_")) {
            return Kind.CONTROL;
        }

        if (starts(id, "template_", "blueprint", "siren_track", "assembly_template", "chemistry_template",
                "crucible_template", "fluid_identifier", "fluid_icon", "chemplant_template")) {
            return Kind.TEMPLATE;
        }

        if (starts(id, "missile_", "sat_", "satellite_", "mp_", "warhead_", "thruster_", "fuselage_",
                "guidance_", "rocket_", "space_")) {
            return Kind.MISSILE;
        }

        if (starts(id, "gun_", "ammo_", "turret_", "bullet_", "grenade_", "launcher_", "weapon_")
                || id.contains("_gun_") || id.endsWith("_gun")) {
            return Kind.WEAPON;
        }

        if (starts(id, "nuke_", "mine_", "det_", "charge_", "bomb_", "dynamite", "semtex", "tnt", "c4",
                "gadget_", "boy_", "man_", "mike_", "tsar_", "fleija_", "solinium_", "detonator",
                "explosive_lenses", "flame_war", "therm_endo", "therm_exo", "emp_bomb")) {
            return Kind.NUKE;
        }

        if (starts(id, "bottle_", "can_", "canned_", "flask_", "cap_", "kit_", "armor_", "hazmat",
                "helmet", "boots", "legs", "mask_", "tool_", "geiger_counter", "dosimeter",
                "pill_", "med_", "radaway", "iv_pouch", "defuser", "wrench", "screwdriver", "hand_drill",
                "battery_potato", "battery_spark")
                || id.endsWith("_helmet") || id.endsWith("_boots") || id.endsWith("_legs")
                || id.endsWith("_plate") || id.endsWith("_axe") || id.endsWith("_pickaxe")
                || id.endsWith("_shovel") || id.endsWith("_hoe") || id.endsWith("_sword")) {
            return Kind.CONSUMABLE;
        }

        if (starts(id, "pellet_rtg", "rod_", "nuclear_waste", "upgrade_", "catalyst_", "fluid_barrel",
                "canister_", "tank_", "cell_", "battery_", "infinite_", "stamp_", "mechanism_",
                "piston_", "motor_")
                || id.contains("_fuel") || id.startsWith("rbmk_fuel")) {
            return Kind.CONTROL;
        }

        if (starts(id, "billet_")) {
            return Kind.PARTS;
        }

        if (starts(id, "waste_earth", "waste_leaves", "waste_planks", "waste_trinitite")) {
            return Kind.BLOCKS;
        }

        if (starts(id, "waste_")) {
            return Kind.CONTROL;
        }

        if (starts(id, "machine_", "rbmk_", "pwr_", "crate_", "cable_", "red_cable", "red_wire",
                "combustion_", "diesel_", "electric_", "anvil_", "hadron_", "cyclotron", "foundry_",
                "conveyor", "crane_", "boxduct", "fluid_duct", "fluid_valve", "fluid_switch",
                "fluid_counter", "condenser", "reactor_", "zirnox_", "watz_", "icf_", "dfc_", "fusion_",
                "boiler", "turbine", "pump_", "compressor", "centrifuge", "assembler", "chemplant",
                "crystallizer", "shredder", "press_", "soldering", "arc_", "geiger", "broadcaster",
                "radiobox", "sat_dock", "structure_")) {
            return Kind.MACHINE;
        }

        if (id.contains("generator") || id.contains("furnace")) {
            return Kind.MACHINE;
        }

        if (starts(id, "ore_", "block_", "brick_", "concrete_", "sand_", "dirt_", "stone_", "glass_",
                "deco_", "basalt", "meteor", "asphalt", "ducrete", "reinforced_", "scaffold_", "steel_",
                "ladder_", "fence_", "barbed_", "spikes", "barrel_", "sellafield", "ash", "slag",
                "gravel_", "tektite", "mush", "glyphid", "crt_", "toaster_", "lamp_", "cage_lamp",
                "flood_lamp", "fluorescent_", "cluster_", "absorber", "ancient_scrap", "electrical_scrap",
                "foam", "frozen_", "gas_", "gneiss_", "pink_", "sandbags", "rebar", "wood_barrier",
                "oil_spill", "taint", "therm_", "emitter", "factory_", "cm_", "depth_", "crystal_",
                "digamma_matter", "balefire", "corium")) {
            if (starts(id, "cm_engine", "cm_circuit", "cm_tank", "cm_port", "cm_casing")) {
                return Kind.MACHINE;
            }
            return Kind.BLOCKS;
        }

        return Kind.PARTS;
    }

    private static boolean starts(String id, String... prefixes) {
        for (String prefix : prefixes) {
            if (id.equals(prefix) || id.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Kind> buildOverrides() {
        Map<String, Kind> map = new HashMap<>();
        put(map, Kind.MACHINE,
                "electric_furnace", "diesel_generator", "combustion_generator", "machine_battery",
                "machine_battery_infinite", "battery_creative",
                "ethanol_bucket", "peroxide_bucket",
                "fluid_barrel", "red_cable", "red_cable_classic", "red_wire_coated", "cable_switch",
                "cable_detector", "cable_diode", "crate_iron", "crate_steel", "deco_rbmk",
                "deco_rbmk_smooth", "pwr_controller",
                "barrel_antimatter");
        put(map, Kind.NUKE,
                "detonator", "explosive_lenses", "gadget_wiring", "gadget_core", "boy_shielding",
                "boy_target", "boy_bullet", "boy_propellant", "boy_igniter", "man_igniter", "man_core",
                "mike_core", "mike_deut", "mike_cooling_unit", "tsar_core", "fleija_igniter",
                "fleija_propellant", "fleija_core", "solinium_igniter", "solinium_propellant",
                "solinium_core", "igniter", "defuser", "cell_sas3",
                "rod_quad_uranium", "rod_quad_lead", "rod_quad_np237",
                // explosive barrels (legacy Nuke tab; decorative iron/steel / antimatter stay Machine)
                "barrel_red", "barrel_pink", "barrel_yellow", "barrel_taint", "barrel_vitrified",
                "barrel_lox",
                "bomb_float", "bomb_multi", "emp_bomb", "fireworks", "fissure_bomb", "crashed_bomb",
                "nuke_custom", "det_cord", "det_charge", "det_miner", "det_nuke", "charge_miner",
                "flame_war", "therm_endo", "therm_exo",
                "ore_volcano", "volcano_core", "volcano_rad_core",
                "gadget_kit", "boy_kit", "man_kit", "mike_kit", "tsar_kit", "fleija_kit", "solinium_kit",
                "n2_kit", "fstbmb_kit", "prototype_kit", "custom_kit", "multi_kit",
                "custom_tnt", "custom_nuke", "custom_hydro", "custom_amat",
                "custom_dirty", "custom_schrab", "custom_fall", "custom_element",
                "powder_fire", "pellet_cluster", "pellet_gas", "powder_poison", "egg_balefire",
                "egg_balefire_shard", "battery_spark", "battery_trixite",
                "demon_core_open", "demon_core_closed");
        put(map, Kind.HIDDEN,
                "structure_anchor",
                "gadget_wireing",
                "demon_core_closed_still");
        // kits that look nuke-named but are gear packs / consumables
        put(map, Kind.CONSUMABLE,
                "nuke_starter_kit", "nuke_advanced_kit", "nuke_electric_kit", "nuke_commercially_kit",
                "bomb_caller", "bomb_waffle");
        put(map, Kind.CONTROL, "mold_base", "pwr_fuel_hot");
        put(map, Kind.MISSILE, "sat_base");
        return Collections.unmodifiableMap(map);
    }

    private static void put(Map<String, Kind> map, Kind kind, String... ids) {
        for (String id : ids) {
            map.put(id, kind);
        }
    }

    private static Map<String, Kind> loadLegacyMap() {
        Map<String, Kind> map = new HashMap<>();
        try (InputStream stream = CreativeTabClassifier.class.getResourceAsStream("/data/hbm/legacy_tab_map.tsv")) {
            if (stream == null) {
                return Collections.emptyMap();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split("\t");
                    if (parts.length < 2) {
                        continue;
                    }
                    Kind kind = parseKind(parts[1].trim());
                    if (kind != null) {
                        map.put(parts[0].trim().toLowerCase(Locale.ROOT), kind);
                    }
                }
            }
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    private static Kind parseKind(String raw) {
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "parts" -> Kind.PARTS;
            case "control" -> Kind.CONTROL;
            case "template" -> Kind.TEMPLATE;
            case "block", "blocks" -> Kind.BLOCKS;
            case "machine" -> Kind.MACHINE;
            case "nuke" -> Kind.NUKE;
            case "missile" -> Kind.MISSILE;
            case "weapon" -> Kind.WEAPON;
            case "consumable" -> Kind.CONSUMABLE;
            default -> null;
        };
    }
}
