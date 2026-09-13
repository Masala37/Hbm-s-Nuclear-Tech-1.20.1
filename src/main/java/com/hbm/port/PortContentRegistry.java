package com.hbm.port;

import com.hbm.lib.RefStrings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Tracks which HBM content is playable vs placeholder for tooltips and creative tabs.
 */
public final class PortContentRegistry {
    public enum Status {
        WORKING(0, ChatFormatting.GREEN, "✔ Working", "Playable in this 1.20.1 port."),
        PARTIAL(1, ChatFormatting.YELLOW, "⚠ WIP", "Partially implemented — expect missing features."),
        UNIMPLEMENTED(2, ChatFormatting.RED, "✘ Not implemented", "Placeholder only — no real behavior yet.");

        public final int sortKey;
        public final ChatFormatting color;
        public final String label;
        public final String detail;

        Status(int sortKey, ChatFormatting color, String label, String detail) {
            this.sortKey = sortKey;
            this.color = color;
            this.label = label;
            this.detail = detail;
        }
    }

    private static final Set<String> STUB_IDS = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> WORKING = workingIds();
    private static final Set<String> PARTIAL = partialIds();

    private PortContentRegistry() {
    }

    public static void markStub(String path) {
        if (path != null && !path.isEmpty()) {
            STUB_IDS.add(path.toLowerCase(Locale.ROOT));
        }
    }

    public static boolean isStub(String path) {
        return path != null && STUB_IDS.contains(path.toLowerCase(Locale.ROOT));
    }

    public static Status status(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Status.UNIMPLEMENTED;
        }
        return status(stack.getItem());
    }

    public static Status status(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null || !RefStrings.MODID.equals(key.getNamespace())) {
            return Status.WORKING;
        }
        return statusPath(key.getPath());
    }

    public static Status status(Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        if (key == null || !RefStrings.MODID.equals(key.getNamespace())) {
            return Status.WORKING;
        }
        return statusPath(key.getPath());
    }

    public static Status statusPath(String path) {
        if (path == null || path.isEmpty()) {
            return Status.UNIMPLEMENTED;
        }
        String id = path.toLowerCase(Locale.ROOT);

        // Explicit allowlists win (also overrides stubs with real behavior, e.g. fire_digamma)
        if (WORKING.contains(id)) {
            return Status.WORKING;
        }
        if (PARTIAL.contains(id)) {
            return Status.PARTIAL;
        }

        // Bulk catalog stubs are placeholders unless allowlisted above
        if (STUB_IDS.contains(id)) {
            return Status.UNIMPLEMENTED;
        }

        if (matchesPrefix(id, WORKING_PREFIXES)) {
            return Status.WORKING;
        }
        if (matchesPrefix(id, PARTIAL_PREFIXES)) {
            return Status.PARTIAL;
        }

        // Intentionally registered ModBlocks/ModItems without a status → WIP by default
        return Status.PARTIAL;
    }

    public static Component tooltipLabel(Status status) {
        return Component.literal(status.label).withStyle(status.color, ChatFormatting.BOLD);
    }

    public static Component tooltipDetail(Status status) {
        return Component.literal(status.detail).withStyle(ChatFormatting.DARK_GRAY);
    }

    private static boolean matchesPrefix(String id, String[] prefixes) {
        for (String prefix : prefixes) {
            if (id.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static final String[] WORKING_PREFIXES = {
            "nuke_",
            "bomb_",
            "charge_",
            "det_",
            "mine_",
            "pipe_",
            "shell_",
            "barrel_",
            "sellafield_",
            "hazmat_",
            "cable_red",
            "waste_",
            "mp_thruster_10_",
            "mp_thruster_15_",
            "mp_thruster_20_",
            "mp_stability_10_",
            "mp_stability_15_",
            "mp_s_20",
            "mp_fuselage_10_",
            "mp_fuselage_15_",
            "mp_warhead_10_",
            "mp_warhead_15_",
            "mp_c_"
    };

    private static final String[] PARTIAL_PREFIXES = {
            "rbmk_",
            "missile_",
            "designator",
            "warhead_",
            "thruster_",
            "mp_",
            "ore_",
            "ingot_",
            "nugget_",
            "billet_",
            "powder_",
            "plate_",
            "wire_",
            "circuit_",
            "upgrade_",
            "rod_",
            "pellet_"
    };

    private static Set<String> workingIds() {
        Set<String> set = new HashSet<>();
        // Explosives / bombs / nukes
        Collections.addAll(set,
                "dynamite", "semtex", "c4", "tnt",
                "bomb_multi", "bomb_float", "emp_bomb", "fireworks", "fissure_bomb",
                "crashed_bomb", "nuke_boy", "nuke_man", "nuke_gadget", "nuke_mike",
                "nuke_tsar", "nuke_fleija", "nuke_solinium", "nuke_n2", "nuke_prototype",
                "nuke_custom", "nuke_fstbmb",
                "detonator", "detonator_deadman", "detonator_laser", "detonator_multi", "detonator_de",
                "defuser", "screwdriver", "screwdriver_desh",
                "bomb_caller", "bomb_waffle", "guide_book",
                "geiger_counter", "geiger", "oil_detector", "digamma_diagnostic", "particle_digamma",
                "launch_pad", "launch_pad_large", "launch_pad_rusted",
                "compact_launcher", "launch_table",
                "struct_launcher", "struct_scaffold", "struct_launcher_core", "struct_launcher_core_large",
                "launch_code", "launch_key",
                "designator", "designator_range", "designator_manual", "radar_linker",
                "machine_missile_assembly", "machine_radar", "machine_radar_large", "radar_screen", "missile_chip",
                "missile_custom",
                "missile_generic", "missile_strong",
                "missile_incendiary", "missile_incendiary_strong",
                "missile_cluster", "missile_cluster_strong",
                "missile_buster", "missile_buster_strong",
                "missile_taint", "missile_micro", "missile_bhole", "missile_schrabidium",
                "missile_emp", "missile_emp_strong", "missile_decoy", "missile_stealth",
                "missile_burst", "missile_inferno", "missile_rain", "missile_drill",
                "missile_shuttle",
                "missile_nuclear", "missile_nuclear_cluster", "missile_volcano",
                "missile_doomsday", "missile_doomsday_rusted",
                "missile_anti_ballistic",
                "fire_digamma", "taint",
                "toxic_block", "bucket_toxic",
                "gas_radon", "gas_radon_dense", "gas_radon_tomb",
                "fallout", "volcano_core", "volcano_rad_core",
                "electric_furnace", "machine_battery", "machine_battery_infinite", "battery_creative",
                "ethanol_bucket", "woodoil_bucket", "peroxide_bucket",
                "kerosene_bucket", "oxygen_bucket", "kerosene_reform_bucket",
                "hydrogen_bucket", "xenon_bucket", "balefire_bucket",
                "uf6_bucket", "puf6_bucket", "watz_bucket", "death_bucket", "vitriol_bucket", "redmud_bucket",
                "machine_diesel", "machine_combustion",
                "diesel_generator", "combustion_generator",
                "heater_firebox", "machine_boiler", "machine_turbine", "machine_condenser",
                "machine_wood_burner", "woodoil_bucket",
                "machine_well", "machine_pumpjack", "machine_refinery", "oil_pipe",
                "ore_oil", "ore_oil_empty", "ore_oil_sand", "ore_oil_sand_alt",
                "dirt_oily", "dirt_dead", "sand_dirty", "sand_dirty_red", "oil_spill",
                "machine_fraction_tower", "fraction_spacer", "machine_catalytic_cracker",
                "machine_hydrotreater", "machine_catalytic_reformer", "machine_vacuum_distill",
                "hotoil_bucket", "naphtha_bucket", "gas_bucket",
                "bitumen_bucket", "smear_bucket", "heatingoil_bucket", "crackoil_bucket",
                "oil_ds_bucket", "crackoil_ds_bucket", "sourgas_bucket", "reformate_bucket",
                "heavyoil_vacuum_bucket", "lightoil_vacuum_bucket", "heatingoil_vacuum_bucket",
                "reformgas_bucket",
                "niter",
                "catalytic_converter",
                "anvil_iron", "anvil_lead", "anvil_steel", "anvil_desh", "anvil_ferrouranium",
                "anvil_saturnite", "anvil_bismuth_bronze", "anvil_arsenic_bronze",
                "anvil_schrabidate", "anvil_dnt", "anvil_osmiridium", "anvil_murky",
                "machine_press", "machine_epress", "machine_shredder", "machine_centrifuge", "machine_gascent", "machine_fel", "machine_silex", "machine_crystallizer", "machine_mixer", "machine_arc_welder", "machine_purex", "machine_soldering_station", "machine_difurnace", "machine_blast_furnace",
                "machine_difurnace_extension", "machine_difurnace_rtg_off",
                "machine_rtg", "machine_assembly_machine", "machine_chemical_plant",
                "pellet_rtg", "pellet_rtg_weak", "pellet_rtg_polonium", "pellet_rtg_gold", "pellet_rtg_americium",
                "pellet_rtg_cobalt", "pellet_rtg_lead", "pellet_rtg_radium", "pellet_rtg_strontium",
                "pellet_rtg_actinium", "pellet_rtg_depleted",
                "battery_pack", "battery_potato", "battery_potatos",
                "wire_steel",
                "laser_crystal_co2", "laser_crystal_bismuth", "laser_crystal_cmb",
                "laser_crystal_dnt", "laser_crystal_digamma",
                "stamp_stone_flat", "stamp_stone_plate", "stamp_stone_wire", "stamp_stone_circuit",
                "stamp_iron_flat", "stamp_iron_plate", "stamp_iron_wire", "stamp_iron_circuit",
                "stamp_steel_flat", "stamp_steel_plate", "stamp_steel_wire", "stamp_steel_circuit",
                "stamp_titanium_flat", "stamp_titanium_plate", "stamp_titanium_wire", "stamp_titanium_circuit",
                "stamp_obsidian_flat", "stamp_obsidian_plate", "stamp_obsidian_wire", "stamp_obsidian_circuit",
                "stamp_desh_flat", "stamp_desh_plate", "stamp_desh_wire", "stamp_desh_circuit",
                "stamp_9", "stamp_9_desh", "stamp_44", "stamp_44_desh",
                "stamp_50", "stamp_50_desh", "stamp_357", "stamp_357_desh",
                "blades_steel", "blades_titanium", "blades_desh",
                "machine_siren", "siren_track", "broadcaster_pc",
                "fence_metal", "fence_metal_post",
                "fluid_barrel", "fluid_barrel_steel", "fluid_barrel_infinite", "red_cable", "red_cable_classic", "red_cable_paintable",
                "cable_switch", "cable_detector", "cable_diode", "fluid_duct_neo",
                "fluid_valve", "fluid_switch", "fluid_counter_valve",
                "fluid_duct_paintable",
                "red_connector", "red_pylon", "red_pylon_large", "substation",
                "red_pylon_medium_wood", "red_pylon_medium_wood_transformer",
                "red_pylon_medium_steel", "red_pylon_medium_steel_transformer",
                "wiring_red_copper",
                "conveyor", "conveyor_wand", "crane_inserter", "crane_extractor",
                "crate_iron", "crate_steel",
                "filing_cabinet", "safe",
                "steel_grate", "steel_grate_wide", "steel_corner", "steel_poles", "pole_top",
                "trapdoor_steel", "door_metal", "door_office", "door_bunker",
                "steel_roof",
                "concrete_colored", "concrete_colored_ext", "concrete_pillar",
                "concrete_slab", "concrete_brick_slab", "brick_slab",
                "concrete_stairs", "concrete_smooth_stairs", "concrete_asbestos_stairs",
                "brick_concrete_stairs", "brick_concrete_mossy_stairs", "brick_concrete_cracked_stairs",
                "brick_concrete_broken_stairs", "brick_light_stairs", "brick_compound_stairs",
                "brick_obsidian_stairs", "reinforced_brick_stairs", "reinforced_stone_stairs",
                "lightstone", "lightstone_bricks_stairs",
                "plant_dead", "plant_flower", "leaves_layer", "ntm_dirt", "wood_structure", "meteor_battery",
                "charger", "tesla", "radiorec", "hev_battery",
                "capacitor_copper", "silo_hatch", "silo_hatch_large",
                "machine_funnel", "machine_microwave", "machine_controller", "machine_fluidtank",
                "machine_weapon_table", "machine_rotary_furnace", "rail_narrow",
                "bobblehead", "skeleton_holder", "turret_howard_damaged", "turret_sentry_damaged",
                "deco_pipe", "deco_pipe_rusted", "deco_pipe_red", "deco_pipe_marked",
                "deco_pipe_rim_green", "deco_pipe_rim_marked", "deco_pipe_rim_rusted",
                "deco_pipe_rim_green_rusted",
                "deco_pipe_framed", "deco_pipe_framed_rusted", "deco_pipe_framed_red",
                "deco_pipe_framed_green_rusted",
                "deco_pipe_quad", "deco_pipe_quad_rusted", "deco_pipe_quad_red", "deco_pipe_quad_marked",
                "crate", "crate_can", "crate_lead", "crate_metal", "crate_red", "crate_weapon",
                "crate_ammo", "crate_supply", "deco_loot", "meteor_spawner",
                "wand_jigsaw", "wand_loot", "wand_logic", "wand_tandem", "crowbar",
                "ball_fireclay", "ingot_firebrick",
                "coil_copper", "coil_gold", "coil_tungsten", "coil_magnetized_tungsten",
                "coil_copper_torus", "coil_gold_torus", "motor",
                "steel_sword", "steel_pickaxe", "steel_axe", "steel_shovel", "steel_hoe",
                "titanium_sword", "titanium_pickaxe", "titanium_axe", "titanium_shovel", "titanium_hoe",
                "dwarven_pickaxe",
                "cobalt_sword", "cobalt_pickaxe", "cobalt_axe", "cobalt_shovel", "cobalt_hoe",
                "cobalt_decorated_sword", "cobalt_decorated_pickaxe", "cobalt_decorated_axe",
                "cobalt_decorated_shovel", "cobalt_decorated_hoe",
                "cmb_sword", "cmb_pickaxe", "cmb_axe", "cmb_shovel", "cmb_hoe",
                "desh_sword", "desh_pickaxe", "desh_axe", "desh_shovel", "desh_hoe",
                "starmetal_sword", "starmetal_pickaxe", "starmetal_axe", "starmetal_shovel", "starmetal_hoe",
                "schrabidium_sword", "schrabidium_pickaxe", "schrabidium_axe", "schrabidium_shovel", "schrabidium_hoe",
                "bismuth_pickaxe", "bismuth_axe", "volcanic_pickaxe", "volcanic_axe",
                "chlorophyte_pickaxe", "chlorophyte_axe", "mese_pickaxe", "mese_axe",
                "hand_drill", "hand_drill_desh", "matchstick", "wood_gavel", "bottle_opener",
                "hazmat_kit", "hazmat_red_kit", "hazmat_grey_kit",
                "hazmat_cloth", "hazmat_cloth_red", "hazmat_cloth_grey",
                "stone_resource_hematite", "stone_resource_malachite", "stone_resource_bauxite",
                "stone_resource_limestone", "chunk_ore_malachite", "chunk_ore_cryolite", "chunk_ore_rare",
                "fragment_coltan", "fragment_boron", "fragment_cobalt", "fragment_cerium",
                "fragment_meteorite", "trinitite",
                "fragment_lanthanium", "fragment_neodymium", "fragment_niobium",
                "crystal_iron", "crystal_titanium", "crystal_aluminium", "crystal_copper", "crystal_tungsten",
                "coal_infernal", "cinnebar", "lignite", "gem_alexandrite", "gem_rad", "gem_volcanic",
                "powder_borax", "powder_molysite", "powder_fire", "nugget_zirconium",
                "flame_war", "therm_endo", "therm_exo",
                "det_miner", "igniter");
        // Hazmat sets
        for (String color : new String[]{"", "_red", "_grey"}) {
            Collections.addAll(set,
                    "hazmat_helmet" + color,
                    "hazmat_plate" + color,
                    "hazmat_legs" + color,
                    "hazmat_boots" + color);
        }
        return set;
    }

    private static Set<String> partialIds() {
        Set<String> set = new HashSet<>();
        Collections.addAll(set,
                "designator_tracker", "designator_arty", "sat_designator",
                "rbmk_blank", "rbmk_reflector", "rbmk_absorber", "rbmk_moderator");
        return set;
    }
}
