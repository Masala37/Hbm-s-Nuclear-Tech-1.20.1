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
            "barrel_",
            "sellafield_",
            "hazmat_",
            "cable_red",
            "waste_"
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
                "geiger_counter", "digamma_diagnostic", "particle_digamma",
                "launch_pad", "designator", "designator_range", "machine_missile_assembly", "missile_chip",
                "missile_generic", "missile_strong",
                "missile_incendiary", "missile_incendiary_strong",
                "missile_cluster", "missile_cluster_strong",
                "missile_buster", "missile_buster_strong",
                "missile_taint", "missile_micro", "missile_bhole",
                "fire_digamma", "taint",
                "toxic_block", "bucket_toxic",
                "gas_radon", "gas_radon_dense", "gas_radon_tomb",
                "fallout", "volcano_core", "volcano_rad_core",
                "electric_furnace", "machine_battery", "machine_battery_infinite", "battery_creative",
                "ethanol_bucket", "peroxide_bucket",
                "machine_diesel", "machine_combustion",
                "fluid_barrel_steel", "fluid_barrel_infinite", "red_cable", "red_cable_classic", "red_cable_paintable",
                "cable_switch", "cable_detector", "cable_diode",
                "crate_iron", "crate_steel",
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
                "designator_manual", "designator_tracker", "designator_arty", "sat_designator",
                "rbmk_blank", "rbmk_reflector", "rbmk_absorber", "rbmk_moderator",
                "compact_launcher", "launch_table");
        return set;
    }
}
