package com.hbm.client.render.missile;

import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MissilePartModels {
    public record Spec(ResourceLocation model, double height, double guiheight, PartType type) {
    }

    private static final Map<String, Spec> BY_PATH = new LinkedHashMap<>();

    static {
        thruster("mp_thruster_10_kerosene", 1, 1);
        thruster("mp_thruster_10_solid", 0.5, 1);
        thruster("mp_thruster_10_xenon", 0.5, 1);
        fin("mp_stability_10_flat", 0, 2);
        fin("mp_stability_10_cruise", 0, 3);
        fin("mp_stability_10_space", 0, 2);
        fuselage("mp_fuselage_10_kerosene", 4, 3);
        fuselage("mp_fuselage_10_kerosene_camo", 4, 3);
        fuselage("mp_fuselage_10_kerosene_desert", 4, 3);
        fuselage("mp_fuselage_10_kerosene_sky", 4, 3);
        fuselage("mp_fuselage_10_kerosene_flames", 4, 3);
        fuselage("mp_fuselage_10_kerosene_insulation", 4, 3);
        fuselage("mp_fuselage_10_kerosene_sleek", 4, 3);
        fuselage("mp_fuselage_10_kerosene_metal", 4, 3);
        fuselage("mp_fuselage_10_kerosene_taint", 4, 3);
        fuselage("mp_fuselage_10_solid", 4, 3);
        fuselage("mp_fuselage_10_solid_flames", 4, 3);
        fuselage("mp_fuselage_10_solid_insulation", 4, 3);
        fuselage("mp_fuselage_10_solid_sleek", 4, 3);
        fuselage("mp_fuselage_10_solid_soviet_glory", 4, 3);
        fuselage("mp_fuselage_10_solid_cathedral", 4, 3);
        fuselage("mp_fuselage_10_solid_moonlit", 4, 3);
        fuselage("mp_fuselage_10_solid_battery", 4, 3);
        fuselage("mp_fuselage_10_solid_duracell", 4, 3);
        fuselage("mp_fuselage_10_xenon", 4, 3);
        fuselage("mp_fuselage_10_xenon_bhole", 4, 3);
        fuselage("mp_fuselage_10_long_kerosene", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_camo", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_desert", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_sky", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_flames", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_insulation", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_sleek", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_metal", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_dash", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_taint", 7, 5);
        fuselage("mp_fuselage_10_long_kerosene_vap", 7, 5);
        fuselage("mp_fuselage_10_long_solid", 7, 5);
        fuselage("mp_fuselage_10_long_solid_flames", 7, 5);
        fuselage("mp_fuselage_10_long_solid_insulation", 7, 5);
        fuselage("mp_fuselage_10_long_solid_sleek", 7, 5);
        fuselage("mp_fuselage_10_long_solid_soviet_glory", 7, 5);
        fuselage("mp_fuselage_10_long_solid_bullet", 7, 5);
        fuselage("mp_fuselage_10_long_solid_silvermoonlight", 7, 5);
        fuselage("mp_fuselage_10_15_kerosene", 9, 5.5);
        fuselage("mp_fuselage_10_15_solid", 9, 5.5);
        fuselage("mp_fuselage_10_15_hydrogen", 9, 5.5);
        fuselage("mp_fuselage_10_15_balefire", 9, 5.5);
        warhead("mp_warhead_10_he", 2, 1.5);
        warhead("mp_warhead_10_incendiary", 2.5, 2);
        warhead("mp_warhead_10_buster", 0.5, 1);
        warhead("mp_warhead_10_nuclear", 2, 1.5);
        warhead("mp_warhead_10_nuclear_large", 2.5, 1.5);
        warhead("mp_warhead_10_taint", 2.25, 1.5);
        warhead("mp_warhead_10_cloud", 2.25, 1.5);
        thruster("mp_thruster_15_kerosene", 1.5, 1.5);
        thruster("mp_thruster_15_kerosene_dual", 1, 1.5);
        thruster("mp_thruster_15_kerosene_triple", 1, 1.5);
        thruster("mp_thruster_15_solid", 0.5, 1);
        thruster("mp_thruster_15_solid_hexdecuple", 0.5, 1);
        thruster("mp_thruster_15_hydrogen", 1.5, 1.5);
        thruster("mp_thruster_15_hydrogen_dual", 1, 1.5);
        thruster("mp_thruster_15_balefire_short", 2, 2);
        thruster("mp_thruster_15_balefire", 3, 2.5);
        thruster("mp_thruster_15_balefire_large", 3, 2.5);
        thruster("mp_thruster_15_balefire_large_rad", 3, 2.5);
        fin("mp_stability_15_flat", 0, 3);
        fin("mp_stability_15_thin", 0, 3);
        fin("mp_stability_15_soyuz", 0, 3);
        fuselage("mp_fuselage_15_kerosene", 10, 6);
        fuselage("mp_fuselage_15_kerosene_camo", 10, 6);
        fuselage("mp_fuselage_15_kerosene_desert", 10, 6);
        fuselage("mp_fuselage_15_kerosene_sky", 10, 6);
        fuselage("mp_fuselage_15_kerosene_insulation", 10, 6);
        fuselage("mp_fuselage_15_kerosene_metal", 10, 6);
        fuselage("mp_fuselage_15_kerosene_decorated", 10, 6);
        fuselage("mp_fuselage_15_kerosene_steampunk", 10, 6);
        fuselage("mp_fuselage_15_kerosene_polite", 10, 6);
        fuselage("mp_fuselage_15_kerosene_blackjack", 10, 6);
        fuselage("mp_fuselage_15_kerosene_lambda", 10, 6);
        fuselage("mp_fuselage_15_kerosene_minuteman", 10, 6);
        fuselage("mp_fuselage_15_kerosene_pip", 10, 6);
        fuselage("mp_fuselage_15_kerosene_taint", 10, 6);
        fuselage("mp_fuselage_15_kerosene_yuck", 10, 6);
        fuselage("mp_fuselage_15_solid", 10, 6);
        fuselage("mp_fuselage_15_solid_insulation", 10, 6);
        fuselage("mp_fuselage_15_solid_desh", 10, 6);
        fuselage("mp_fuselage_15_solid_soviet_glory", 10, 6);
        fuselage("mp_fuselage_15_solid_soviet_stank", 10, 6);
        fuselage("mp_fuselage_15_solid_faust", 10, 6);
        fuselage("mp_fuselage_15_solid_silvermoonlight", 10, 6);
        fuselage("mp_fuselage_15_solid_snowy", 10, 6);
        fuselage("mp_fuselage_15_solid_panorama", 10, 6);
        fuselage("mp_fuselage_15_solid_roses", 10, 6);
        fuselage("mp_fuselage_15_solid_mimi", 10, 6);
        fuselage("mp_fuselage_15_hydrogen", 10, 6);
        fuselage("mp_fuselage_15_hydrogen_cathedral", 10, 6);
        fuselage("mp_fuselage_15_balefire", 10, 6);
        fuselage("mp_fuselage_15_20_kerosene", 16, 10);
        fuselage("mp_fuselage_15_20_kerosene_magnusson", 16, 10);
        fuselage("mp_fuselage_15_20_solid", 16, 10);
        warhead("mp_warhead_15_he", 2, 1.5);
        warhead("mp_warhead_15_incendiary", 2, 1.5);
        warhead("mp_warhead_15_nuclear", 3.5, 2);
        warhead("mp_warhead_15_nuclear_shark", 3.5, 2);
        warhead("mp_warhead_15_nuclear_mimi", 3.5, 2);
        warhead("mp_warhead_15_boxcar", 2.25, 7.5);
        warhead("mp_warhead_15_n2", 3, 2);
        warhead("mp_warhead_15_balefire", 2.75, 2);
        warhead("mp_warhead_15_turbine", 2.25, 2);
        thruster("mp_thruster_20_kerosene", 3, 2.5);
        thruster("mp_thruster_20_kerosene_dual", 2, 2);
        thruster("mp_thruster_20_kerosene_triple", 2, 2);
        thruster("mp_thruster_20_solid", 1, 1.75);
        thruster("mp_thruster_20_solid_multi", 0.5, 1.5);
        thruster("mp_thruster_20_solid_multier", 0.5, 1.5);
        fin("mp_s_20", 0, 3);
    }

    private MissilePartModels() {
    }

    private static void thruster(String id, double height, double gui) {
        put(id, height, gui, PartType.THRUSTER);
    }

    private static void fin(String id, double height, double gui) {
        put(id, height, gui, PartType.FINS);
    }

    private static void fuselage(String id, double height, double gui) {
        put(id, height, gui, PartType.FUSELAGE);
    }

    private static void warhead(String id, double height, double gui) {
        put(id, height, gui, PartType.WARHEAD);
    }

    private static void put(String id, double height, double gui, PartType type) {
        BY_PATH.put(id, new Spec(new ResourceLocation(RefStrings.MODID, "block/missile_part/" + id),
                height, gui, type));
    }

    public static Spec get(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) {
            return null;
        }
        return BY_PATH.get(key.getPath());
    }

    public static Spec get(ItemCustomMissilePart part) {
        return part == null ? null : get((Item) part);
    }

    public static Collection<ResourceLocation> allModels() {
        return Collections.unmodifiableList(BY_PATH.values().stream().map(Spec::model).distinct().toList());
    }
}
