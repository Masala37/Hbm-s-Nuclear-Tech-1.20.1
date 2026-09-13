package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolderingPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void solderingAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_soldering_station\": \"Soldering Station\""));
        assertTrue(lang.contains("\"container.machineSolderingStation\": \"Soldering Station\""));
        assertTrue(lang.contains("\"item.hbm.circuit_analog\": \"Analog Circuit Board\""));
        assertTrue(lang.contains("\"item.hbm.circuit_vacuum_tube\": \"Vacuum Tube\""));
        assertTrue(lang.contains("\"item.hbm.wire_lead\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_soldering_station\""));
        assertTrue(ironTool.contains("\"hbm:machine_soldering_station\""));

        String state = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_soldering_station.json"));
        assertTrue(state.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/soldering_station_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/soldering_station.json"))
                .contains("soldering_station.obj"));

        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_soldering_station.json")));
        String anvil = Files.readString(ROOT.resolve("data/hbm/machine_recipes/anvil.json"));
        assertTrue(anvil.contains("\"hbm:machine_soldering_station\""));
        assertTrue(anvil.contains("\"hbm:circuit_vacuum_tube\""));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/recipes/circuit_vacuum_tube.json")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/recipes/circuit_capacitor.json")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/recipes/circuit_pcb.json")));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/item/circuit_analog.json"))
                .contains("hbm:item/circuit.analog"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/item/wire_lead.json"))
                .contains("hbm:item/wire_fine"));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/soldering_station.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String text = Files.readString(ROOT.resolve("assets/hbm/models/obj/soldering_station.obj"), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("mtllib soldering_station.mtl"));
        assertTrue(text.contains("usemtl material"));
        assertTrue(text.contains("\nf "));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/soldering_station.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/processing/gui_soldering_station.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/loot_tables/blocks/machine_soldering_station.json")));
        String dump = Files.readString(ROOT.resolve("data/hbm/machine_recipes/soldering.json"));
        assertTrue(dump.contains("\"hbm:circuit_analog\""));
        assertTrue(dump.contains("\"wireFineLead\""));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachineSolderingStationBlock.java"))
                .contains("{0, 0, 1, 0, 1, 0}"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/SolderingStationBlockEntity.java"))
                .contains("DEFAULT_CONSUMPTION = 100"));
    }
}
