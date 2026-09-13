package com.hbm.items.weapon;

import com.hbm.entity.missile.MissileAssemblyRecipes;
import com.hbm.lib.RefStrings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * The nine assembled custom missiles appended by the 1.7.10 missile tab.
 * Each entry is a normal {@link ItemCustomMissile} stack, so launchers, fuel,
 * composed part models, flight effects, sounds, and warhead behavior all use
 * the same live systems as a missile assembled by the player.
 */
public final class CustomMissilePresets {
    private static final List<Definition> DEFINITIONS = List.of(
            new Definition(
                    "Lil Bub", ChatFormatting.DARK_PURPLE,
                    "mp_c_3", "mp_warhead_10_he", "mp_fuselage_10_kerosene",
                    "mp_stability_10_flat", "mp_thruster_10_kerosene"),
            new Definition(
                    "Long Boy", ChatFormatting.DARK_PURPLE,
                    "mp_c_3", "mp_warhead_10_incendiary", "mp_fuselage_10_long_solid",
                    "mp_stability_10_space", "mp_thruster_10_solid"),
            new Definition(
                    "Uncle Kim", ChatFormatting.DARK_PURPLE,
                    "mp_c_3", "mp_warhead_10_nuclear", "mp_fuselage_10_15_kerosene",
                    "mp_stability_15_flat", "mp_thruster_15_kerosene"),
            new Definition(
                    "Trotty's Toy Rocket", ChatFormatting.GREEN,
                    "mp_c_3", "mp_warhead_10_nuclear_large", "mp_fuselage_10_15_balefire",
                    "mp_stability_15_flat", "mp_thruster_15_balefire_large"),
            new Definition(
                    "Stealthy Shark", ChatFormatting.DARK_PURPLE,
                    "mp_c_3", "mp_warhead_15_nuclear_shark", "mp_fuselage_15_kerosene_camo",
                    "mp_stability_15_thin", "mp_thruster_15_kerosene_triple"),
            new Definition(
                    "Polite Lad", ChatFormatting.DARK_PURPLE,
                    "mp_c_3", "mp_warhead_15_he", "mp_fuselage_15_kerosene_polite",
                    "mp_stability_15_thin", "mp_thruster_15_kerosene_dual"),
            new Definition(
                    "NERV's Leftover Missile", ChatFormatting.DARK_PURPLE,
                    "mp_c_3", "mp_warhead_15_n2", "mp_fuselage_15_solid_desh",
                    "mp_stability_15_thin", "mp_thruster_15_solid_hexdecuple"),
            new Definition(
                    "Auntie Blackjack", ChatFormatting.RED,
                    "mp_c_5", "mp_warhead_15_boxcar", "mp_fuselage_15_kerosene_blackjack",
                    "mp_stability_15_thin", "mp_thruster_15_kerosene"),
            new Definition(
                    "Hightower Missile", ChatFormatting.GREEN,
                    "mp_c_4", "mp_warhead_15_balefire", "mp_fuselage_15_20_kerosene_magnusson",
                    null, "mp_thruster_20_kerosene")
    );

    private CustomMissilePresets() {
    }

    public static List<Definition> definitions() {
        return DEFINITIONS;
    }

    public static List<ItemStack> createAll() {
        return DEFINITIONS.stream().map(Definition::createStack).toList();
    }

    public static void appendTo(CreativeModeTab.Output output) {
        for (Definition definition : DEFINITIONS) {
            output.accept(definition.createStack());
        }
    }

    public record Definition(String displayName, ChatFormatting color,
                             String chip, String warhead, String fuselage,
                             String stability, String thruster) {
        public ItemStack createStack() {
            ItemStack chipStack = part(chip);
            ItemStack warheadStack = part(warhead);
            ItemStack fuselageStack = part(fuselage);
            ItemStack stabilityStack = stability == null ? ItemStack.EMPTY : part(stability);
            ItemStack thrusterStack = part(thruster);

            if (!MissileAssemblyRecipes.canBuild(
                    chipStack, warheadStack, fuselageStack, stabilityStack, thrusterStack, ItemStack.EMPTY)) {
                throw new IllegalStateException("Invalid custom missile preset: " + displayName);
            }

            return ItemCustomMissile.buildMissile(
                            chipStack, warheadStack, fuselageStack,
                            stabilityStack.isEmpty() ? null : stabilityStack, thrusterStack)
                    .setHoverName(Component.literal(displayName).withStyle(color));
        }

        public List<String> partIds() {
            if (stability == null) {
                return List.of(chip, warhead, fuselage, thruster);
            }
            return List.of(chip, warhead, fuselage, stability, thruster);
        }

        private static ItemStack part(String path) {
            ResourceLocation id = new ResourceLocation(RefStrings.MODID, path);
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (!(item instanceof ItemCustomMissilePart)) {
                throw new IllegalStateException("Missing custom missile part: " + id);
            }
            return new ItemStack(item);
        }
    }
}
