package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");
    private static final String[] TOOLS = {
            "cobalt_sword", "cobalt_pickaxe", "cobalt_axe", "cobalt_shovel", "cobalt_hoe",
            "cobalt_decorated_sword", "cobalt_decorated_pickaxe", "cobalt_decorated_axe",
            "cobalt_decorated_shovel", "cobalt_decorated_hoe",
            "cmb_sword", "cmb_pickaxe", "cmb_axe", "cmb_shovel", "cmb_hoe",
            "desh_sword", "desh_pickaxe", "desh_axe", "desh_shovel", "desh_hoe",
            "starmetal_sword", "starmetal_pickaxe", "starmetal_axe", "starmetal_shovel", "starmetal_hoe",
            "schrabidium_sword", "schrabidium_pickaxe", "schrabidium_axe", "schrabidium_shovel", "schrabidium_hoe",
            "bismuth_pickaxe", "bismuth_axe", "volcanic_pickaxe", "volcanic_axe",
            "chlorophyte_pickaxe", "chlorophyte_axe", "mese_pickaxe", "mese_axe"
    };

    @Test
    void toolTexturesAndHandheldModelsExist() throws IOException {
        for (String id : TOOLS) {
            Path texture = ROOT.resolve("assets/hbm/textures/item/" + id + ".png");
            assertTrue(Files.isRegularFile(texture), "missing texture " + id);
            Path model = ROOT.resolve("assets/hbm/models/item/" + id + ".json");
            assertTrue(Files.isRegularFile(model), "missing model " + id);
            String json = Files.readString(model);
            assertTrue(json.contains("minecraft:item/handheld"), id + " should be handheld");
            assertTrue(json.contains("hbm:item/" + id), id + " should use its texture");
        }
    }

    @Test
    void toolTiersMatchOneSevenMaterials() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/hbm/items/tool/NtmTiers.java"));
        assertTrue(source.contains("COBALT(3, 750, 9.0F, 2.5F, 60"));
        assertTrue(source.contains("COBALT_DECORATED(3, 2500, 15.0F, 2.5F, 75"));
        assertTrue(source.contains("CMB(3, 8500, 40.0F, 55.0F, 100"));
        assertTrue(source.contains("DESH(2, 0, 7.5F, 2.0F, 10"));
        assertTrue(source.contains("STARMETAL(3, 3000, 20.0F, 2.5F, 100"));
        assertTrue(source.contains("SCHRABIDIUM(3, 10000, 50.0F, 100.0F, 200"));
        assertTrue(source.contains("BISMUTH(4, 0, 50.0F, 0.0F, 200"));
        assertTrue(source.contains("MESE(4, 0, 100.0F, 0.0F, 200"));
    }

    @Test
    void workingIdsIncludeNewTools() throws IOException {
        String working = Files.readString(Path.of("src/main/java/com/hbm/port/PortContentRegistry.java"));
        assertTrue(working.contains("\"cobalt_pickaxe\""));
        assertTrue(working.contains("\"cmb_sword\""));
        assertTrue(working.contains("\"desh_axe\""));
        assertTrue(working.contains("\"starmetal_hoe\""));
        assertTrue(working.contains("\"schrabidium_shovel\""));
        assertTrue(working.contains("\"mese_pickaxe\""));
        String skip = Files.readString(Path.of("src/main/java/com/hbm/registry/ModBulkContent.java"));
        assertTrue(skip.contains("\"cobalt_pickaxe\""));
        assertTrue(skip.contains("\"hand_drill\""));
        assertTrue(skip.contains("\"bottle_opener\""));
    }

    @Test
    void abilityToolsMatchOneSevenEffectsAndDamage() throws IOException {
        String abilities = Files.readString(Path.of("src/main/java/com/hbm/items/tool/NtmToolAbilities.java"));
        assertTrue(abilities.contains("int[] RECURSION_RADIUS = {3, 4, 5, 6, 7, 9, 10}"));
        assertTrue(abilities.contains("int[] HAMMER_RANGE = {1, 2, 3, 4}"));
        assertTrue(abilities.contains("int[] LUCK_FORTUNE = {1, 2, 3, 4, 5, 9}"));
        assertTrue(abilities.contains("int[] STUN_SECONDS = {2, 3, 5, 10, 15}"));
        assertTrue(abilities.contains("int RECURSION_DEPTH = 1000"));
        String items = Files.readString(Path.of("src/main/java/com/hbm/items/tool/NtmAbilityItems.java"));
        assertTrue(items.contains("NtmAbilityDiggerItem.pickaxe(NtmTiers.STEEL, 4.0F"));
        assertTrue(items.contains("NtmAbilitySwordItem.create(NtmTiers.STEEL, 6.0F"));
        assertTrue(items.contains("NtmAbilityDiggerItem.miner(NtmTiers.DWARVEN, 5.0F"));
        assertTrue(items.contains("b.area(Area.RECURSION, 0)"));
        assertTrue(items.contains("b.weapon(Weapon.STUN, 0)"));
        assertTrue(items.contains("b.weapon(Weapon.BEHEADER, 0)"));
        String modItems = Files.readString(Path.of("src/main/java/com/hbm/registry/ModItems.java"));
        assertTrue(modItems.contains("NtmAbilityItems::steelPick"));
        assertTrue(modItems.contains("HandDrillItem::steel"));
        assertTrue(modItems.contains("MatchstickItem::new"));
        assertTrue(modItems.contains("WeaponSpecialItem::woodGavel"));
        assertTrue(modItems.contains("WeaponSpecialItem::bottleOpener"));
        assertTrue(!modItems.contains("new PickaxeItem(NtmTiers.STEEL"));
    }

    @Test
    void utilityToolAssetsExist() throws IOException {
        for (String id : new String[]{"hand_drill", "hand_drill_desh", "matchstick", "wood_gavel", "bottle_opener"}) {
            assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/item/" + id + ".png")), "missing texture " + id);
            assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/models/item/" + id + ".json")), "missing model " + id);
        }
        String match = Files.readString(ROOT.resolve("assets/hbm/models/item/matchstick.json"));
        assertTrue(match.contains("minecraft:item/handheld"));
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"tool.ability.recursion\": \"Vein Miner\""));
        assertTrue(lang.contains("\"weapon.ability.stun\": \"Stunning\""));
        String tags = Files.readString(ROOT.resolve("data/minecraft/tags/items/shovels.json"));
        assertTrue(tags.contains("hbm:dwarven_pickaxe") || Files.readString(ROOT.resolve("data/minecraft/tags/items/pickaxes.json")).contains("hbm:dwarven_pickaxe"));
        String shovels = Files.readString(ROOT.resolve("data/minecraft/tags/items/shovels.json"));
        assertTrue(shovels.contains("hbm:bismuth_pickaxe"));
        String swords = Files.readString(ROOT.resolve("data/minecraft/tags/items/swords.json"));
        assertTrue(swords.contains("hbm:wood_gavel"));
        assertTrue(swords.contains("hbm:bottle_opener"));
    }
}
