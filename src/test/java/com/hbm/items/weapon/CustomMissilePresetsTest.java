package com.hbm.items.weapon;

import net.minecraft.ChatFormatting;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomMissilePresetsTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hbm");

    @Test
    void preservesLegacyPresetOrderNamesAndColors() {
        List<CustomMissilePresets.Definition> presets = CustomMissilePresets.definitions();

        assertEquals(List.of(
                        "Lil Bub",
                        "Long Boy",
                        "Uncle Kim",
                        "Trotty's Toy Rocket",
                        "Stealthy Shark",
                        "Polite Lad",
                        "NERV's Leftover Missile",
                        "Auntie Blackjack",
                        "Hightower Missile"),
                presets.stream().map(CustomMissilePresets.Definition::displayName).toList());
        assertEquals(List.of(
                        ChatFormatting.DARK_PURPLE,
                        ChatFormatting.DARK_PURPLE,
                        ChatFormatting.DARK_PURPLE,
                        ChatFormatting.GREEN,
                        ChatFormatting.DARK_PURPLE,
                        ChatFormatting.DARK_PURPLE,
                        ChatFormatting.DARK_PURPLE,
                        ChatFormatting.RED,
                        ChatFormatting.GREEN),
                presets.stream().map(CustomMissilePresets.Definition::color).toList());
    }

    @Test
    void preservesExactLegacyPartCombinations() {
        assertEquals(List.of(
                        "Lil Bub|mp_c_3|mp_warhead_10_he|mp_fuselage_10_kerosene|mp_stability_10_flat|mp_thruster_10_kerosene",
                        "Long Boy|mp_c_3|mp_warhead_10_incendiary|mp_fuselage_10_long_solid|mp_stability_10_space|mp_thruster_10_solid",
                        "Uncle Kim|mp_c_3|mp_warhead_10_nuclear|mp_fuselage_10_15_kerosene|mp_stability_15_flat|mp_thruster_15_kerosene",
                        "Trotty's Toy Rocket|mp_c_3|mp_warhead_10_nuclear_large|mp_fuselage_10_15_balefire|mp_stability_15_flat|mp_thruster_15_balefire_large",
                        "Stealthy Shark|mp_c_3|mp_warhead_15_nuclear_shark|mp_fuselage_15_kerosene_camo|mp_stability_15_thin|mp_thruster_15_kerosene_triple",
                        "Polite Lad|mp_c_3|mp_warhead_15_he|mp_fuselage_15_kerosene_polite|mp_stability_15_thin|mp_thruster_15_kerosene_dual",
                        "NERV's Leftover Missile|mp_c_3|mp_warhead_15_n2|mp_fuselage_15_solid_desh|mp_stability_15_thin|mp_thruster_15_solid_hexdecuple",
                        "Auntie Blackjack|mp_c_5|mp_warhead_15_boxcar|mp_fuselage_15_kerosene_blackjack|mp_stability_15_thin|mp_thruster_15_kerosene",
                        "Hightower Missile|mp_c_4|mp_warhead_15_balefire|mp_fuselage_15_20_kerosene_magnusson|-|mp_thruster_20_kerosene"),
                CustomMissilePresets.definitions().stream().map(CustomMissilePresetsTest::signature).toList());
        assertNull(CustomMissilePresets.definitions().get(8).stability(),
                "Hightower intentionally keeps the legacy no-fins configuration");
    }

    @Test
    void everyPresetPartHasItemAndComposedModelAssets() {
        for (CustomMissilePresets.Definition preset : CustomMissilePresets.definitions()) {
            for (String part : preset.partIds()) {
                assertTrue(Files.isRegularFile(ASSETS.resolve("models/item/" + part + ".json")),
                        preset.displayName() + " missing item model for " + part);
                if (!part.equals(preset.chip())) {
                    assertTrue(Files.isRegularFile(ASSETS.resolve("models/block/missile_part/" + part + ".json")),
                            preset.displayName() + " missing composed model for " + part);
                }
            }
        }
    }

    private static String signature(CustomMissilePresets.Definition preset) {
        return String.join("|",
                preset.displayName(),
                preset.chip(),
                preset.warhead(),
                preset.fuselage(),
                preset.stability() == null ? "-" : preset.stability(),
                preset.thruster());
    }
}
