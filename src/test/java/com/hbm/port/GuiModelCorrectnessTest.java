package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiModelCorrectnessTest {
    private static final Path ROOT = Path.of("src/main/resources");
    private static final Path JAVA = Path.of("src/main/java");
    private static final Pattern TEXTURE = Pattern.compile(
            "new ResourceLocation\\(RefStrings\\.MODID,\\s*\"(textures/gui/[^\"]+\\.png)\"\\)");

    @Test
    void crateGuiUsesHbmTexturesAndLegacyPlayerSlots() throws IOException {
        String screen = Files.readString(JAVA.resolve("com/hbm/client/screen/StorageCrateScreen.java"));
        assertFalse(screen.contains("generic_54.png"));
        assertTrue(screen.contains("textures/gui/storage/gui_crate_iron.png"));
        assertTrue(screen.contains("textures/gui/storage/gui_crate_steel.png"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/storage/gui_crate_iron.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/storage/gui_crate_steel.png")));
        String menu = Files.readString(JAVA.resolve("com/hbm/inventory/menu/StorageCrateMenu.java"));
        assertTrue(menu.contains("addPlayerInventory(inv, 104, 162)"));
        assertTrue(menu.contains("addPlayerInventory(inv, 140, 198)"));
    }

    @Test
    void shredderAndTurbineUseLegacyFaceTextures() throws IOException {
        String shredder = Files.readString(ROOT.resolve("assets/hbm/models/block/machine_shredder.json"));
        assertTrue(shredder.contains("orientable_with_bottom"));
        assertTrue(shredder.contains("machine_shredder_front_alt"));
        assertTrue(shredder.contains("machine_shredder_side_alt"));
        assertTrue(shredder.contains("machine_shredder_top_alt"));
        assertTrue(shredder.contains("machine_shredder_bottom_alt"));
        String turbine = Files.readString(ROOT.resolve("assets/hbm/models/block/machine_turbine.json"));
        assertTrue(turbine.contains("machine_turbine_top"));
        assertTrue(turbine.contains("machine_turbine_base"));
        assertFalse(turbine.contains("\"side\": \"hbm:block/machine_turbine_top\""));
        for (String png : List.of(
                "assets/hbm/textures/block/machine_shredder_front_alt.png",
                "assets/hbm/textures/block/machine_shredder_side_alt.png",
                "assets/hbm/textures/block/machine_shredder_top_alt.png",
                "assets/hbm/textures/block/machine_shredder_bottom_alt.png",
                "assets/hbm/textures/block/machine_turbine_top.png",
                "assets/hbm/textures/block/machine_turbine_base.png")) {
            assertTrue(Files.isRegularFile(ROOT.resolve(png)), "missing " + png);
        }
    }

    @Test
    void electricFurnaceAndSteelBarrelModelsMatchLegacy() throws IOException {
        String furnace = Files.readString(ROOT.resolve("assets/hbm/models/block/electric_furnace.json"));
        assertTrue(furnace.contains("orientable_with_bottom"));
        assertTrue(furnace.contains("electric_furnace_bottom"));
        String barrel = Files.readString(ROOT.resolve("assets/hbm/models/block/fluid_barrel.json"));
        assertTrue(barrel.contains("barrel_steel.obj"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/models/obj/barrel_steel.obj")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/block/electric_furnace_bottom.png")));
    }

    @Test
    void dieselAndBarrelMenusWireLegacySlots() throws IOException {
        String dieselMenu = Files.readString(JAVA.resolve("com/hbm/inventory/menu/DieselGeneratorMenu.java"));
        assertTrue(dieselMenu.contains("SLOT_FUEL_IN, 44, 17"));
        assertTrue(dieselMenu.contains("SLOT_BATTERY, 116, 53"));
        String dieselScreen = Files.readString(JAVA.resolve("com/hbm/client/screen/DieselGeneratorScreen.java"));
        assertTrue(dieselScreen.contains("x + 115, y + 34, 208, 0, 18, 18"));
        String barrelMenu = Files.readString(JAVA.resolve("com/hbm/inventory/menu/FluidBarrelMenu.java"));
        assertTrue(barrelMenu.contains("SLOT_FILL_IN, 35, 17"));
        assertTrue(barrelMenu.contains("SLOT_EMPTY_IN, 125, 17"));
        String barrelScreen = Files.readString(JAVA.resolve("com/hbm/client/screen/FluidBarrelScreen.java"));
        assertTrue(barrelScreen.contains("x + 151, y + 34, 176, mode * 18, 18, 18"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/gui_diesel.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/storage/gui_barrel.png")));
        String working = Files.readString(JAVA.resolve("com/hbm/port/PortContentRegistry.java"));
        assertTrue(working.contains("\"fluid_barrel\""));
        String diFurnace = Files.readString(JAVA.resolve("com/hbm/client/screen/DiFurnaceScreen.java"));
        assertTrue(diFurnace.contains("textures/gui/gui_di_furnace.png"));
        assertFalse(diFurnace.contains("GUIDiFurnace"));
    }

    @Test
    void allScreenGuiTexturesExist() throws IOException {
        Path screens = JAVA.resolve("com/hbm/client/screen");
        try (Stream<Path> stream = Files.walk(screens)) {
            stream.filter(path -> path.toString().endsWith("Screen.java")).forEach(path -> {
                try {
                    String text = Files.readString(path);
                    Matcher matcher = TEXTURE.matcher(text);
                    while (matcher.find()) {
                        String rel = matcher.group(1);
                        assertTrue(rel.matches("[a-z0-9/._-]+"),
                                path.getFileName() + " invalid ResourceLocation path: " + rel);
                        Path png = ROOT.resolve("assets/hbm/" + rel);
                        assertTrue(Files.isRegularFile(png), path.getFileName() + " missing " + rel);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
