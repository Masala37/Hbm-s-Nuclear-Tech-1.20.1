package com.hbm.inventory.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Maps 1.7.10 ore-dict names onto 1.20 item ids and Forge tags. Does not invent recipe rows.
 */
public final class OreDictMatch {
    private static final String[] PREFIXES = {
            "wireDense", "wireFine", "dustTiny", "plateWelded", "plateCast", "plateTriple", "plateSextuple", "ntmpipe",
            "ingot", "dust", "plate", "gem", "ore", "nugget", "billet", "block", "crystal", "shell", "bolt", "any"
    };

    private static final Map<String, String> MATERIAL_ALIAS = Map.ofEntries(
            Map.entry("Aluminum", "aluminium"),
            Map.entry("NetherQuartz", "quartz"),
            Map.entry("WorkersAlloy", "desh"),
            Map.entry("Mingrade", "red_copper"),
            Map.entry("CMBSteel", "combine_steel"),
            Map.entry("DuraSteel", "dura_steel"),
            Map.entry("GunMetal", "gunmetal"),
            Map.entry("WeaponSteel", "weaponsteel"),
            Map.entry("Saturnite", "saturnite"),
            Map.entry("Technetium99", "tc99"),
            Map.entry("AnyCoke", "coke"),
            Map.entry("Coke", "coke"),
            Map.entry("AnyPlastic", "polymer"),
            Map.entry("Nb", "niobium"),
            Map.entry("W", "tungsten"),
            Map.entry("Li", "lithium"),
            Map.entry("RareEarth", "rare"),
            Map.entry("Cinnabar", "cinnebar"),
            Map.entry("Saltpeter", "niter"),
            Map.entry("Thorium232", "thorium"),
            Map.entry("Th232", "thorium"),
            Map.entry("Gold198", "au198"),
            Map.entry("Au198", "au198"),
            Map.entry("Lead209", "pb209"),
            Map.entry("Pb209", "pb209")
    );

    private OreDictMatch() {
    }

    public static boolean matches(ItemStack stack, IngredientRef ref) {
        if (stack == null || stack.isEmpty() || ref == null) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) {
            return false;
        }
        if (ref.item() != null && !ref.item().isEmpty()) {
            ResourceLocation want = parseId(ref.item());
            return want != null && want.equals(key);
        }
        if (ref.ore() == null || ref.ore().isEmpty()) {
            return false;
        }
        for (ResourceLocation candidate : itemIds(ref.ore())) {
            if (candidate.equals(key)) {
                return true;
            }
        }
        for (TagKey<Item> tag : tags(ref.ore())) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    public static boolean exists(IngredientRef ref) {
        if (ref.item() != null && !ref.item().isEmpty()) {
            ResourceLocation id = parseId(ref.item());
            return id != null && ForgeRegistries.ITEMS.containsKey(id);
        }
        if (ref.ore() == null || ref.ore().isEmpty()) {
            return false;
        }
        for (ResourceLocation id : itemIds(ref.ore())) {
            if (ForgeRegistries.ITEMS.containsKey(id)) {
                return true;
            }
        }
        return !tags(ref.ore()).isEmpty();
    }

    public static ItemStack toStack(IngredientRef ref) {
        if (ref.item() != null && !ref.item().isEmpty()) {
            ResourceLocation id = parseId(ref.item());
            if (id != null && ForgeRegistries.ITEMS.containsKey(id)) {
                return new ItemStack(ForgeRegistries.ITEMS.getValue(id), ref.count());
            }
        }
        for (ResourceLocation id : itemIds(ref.ore())) {
            if (ForgeRegistries.ITEMS.containsKey(id)) {
                return new ItemStack(ForgeRegistries.ITEMS.getValue(id), ref.count());
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> allStacks(IngredientRef ref) {
        if (ref == null) {
            return List.of();
        }
        LinkedHashSet<ItemStack> unique = new LinkedHashSet<>();
        if (!ref.anyOf().isEmpty()) {
            for (IngredientRef option : ref.anyOf()) {
                unique.addAll(allStacks(option));
            }
            return List.copyOf(unique);
        }
        if (ref.item() != null && !ref.item().isEmpty()) {
            ItemStack stack = toStack(ref);
            if (!stack.isEmpty()) {
                unique.add(stack);
            }
            return List.copyOf(unique);
        }
        if (ref.ore() == null || ref.ore().isEmpty()) {
            return List.of();
        }
        for (ResourceLocation id : itemIds(ref.ore())) {
            if (ForgeRegistries.ITEMS.containsKey(id)) {
                unique.add(new ItemStack(ForgeRegistries.ITEMS.getValue(id), ref.count()));
            }
        }
        var tags = ForgeRegistries.ITEMS.tags();
        if (tags != null) {
            for (TagKey<Item> tag : tags(ref.ore())) {
                if (!tags.isKnownTagName(tag)) {
                    continue;
                }
                for (Item item : tags.getTag(tag)) {
                    unique.add(new ItemStack(item, ref.count()));
                }
            }
        }
        return List.copyOf(unique);
    }

    public static Split split(String ore) {
        if (ore == null) {
            return new Split("", "");
        }
        for (String prefix : PREFIXES) {
            if (ore.startsWith(prefix) && ore.length() > prefix.length()) {
                char next = ore.charAt(prefix.length());
                if (Character.isUpperCase(next) || prefix.equals("ntmpipe")) {
                    return new Split(prefix, ore.substring(prefix.length()));
                }
            }
        }
        return new Split("", ore);
    }

    public static List<String> candidateIdStrings(String ore) {
        List<String> ids = new ArrayList<>();
        for (ResourceLocation id : itemIds(ore)) {
            ids.add(id.toString());
        }
        return ids;
    }

    public static List<String> tagIdStrings(String ore) {
        List<String> ids = new ArrayList<>();
        for (TagKey<Item> tag : tags(ore)) {
            ids.add(tag.location().toString());
        }
        return ids;
    }

    static List<ResourceLocation> itemIds(String ore) {
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        if (ore == null || ore.isEmpty()) {
            return List.of();
        }
        addVanilla(ore, ids);
        Split parts = split(ore);
        String mat = materialPath(parts.material);
        if (!mat.isEmpty()) {
            if ("dust".equals(parts.prefix) || "dustTiny".equals(parts.prefix)) {
                String powder = "dustTiny".equals(parts.prefix) ? "powder_" + mat + "_tiny" : "powder_" + mat;
                ids.add(hbm(powder));
                if ("dust".equals(parts.prefix)) {
                    ids.add(hbm(mat));
                }
            } else if ("ingot".equals(parts.prefix)) {
                ids.add(hbm("ingot_" + mat));
                if ("malachite".equals(mat)) {
                    ids.add(hbm("chunk_ore_malachite"));
                }
                if ("rare".equals(mat)) {
                    ids.add(hbm("chunk_ore_rare"));
                }
            } else if ("plate".equals(parts.prefix)) {
                ids.add(hbm("plate_" + mat));
            } else if ("nugget".equals(parts.prefix)) {
                ids.add(hbm("nugget_" + mat));
                if ("thorium".equals(mat)) {
                    ids.add(hbm("nugget_th232"));
                }
            } else if ("billet".equals(parts.prefix)) {
                ids.add(hbm("billet_" + mat));
                if ("thorium".equals(mat)) {
                    ids.add(hbm("billet_th232"));
                }
            } else if ("gem".equals(parts.prefix)) {
                ids.add(hbm(mat));
                ids.add(hbm("gem_" + mat));
                if ("bauxite".equals(mat)) {
                    ids.add(hbm("stone_resource_bauxite"));
                }
            } else if ("ore".equals(parts.prefix)) {
                ids.add(hbm("ore_" + mat));
                if ("limestone".equals(mat) || "hematite".equals(mat) || "malachite".equals(mat)
                        || "bauxite".equals(mat)) {
                    ids.add(hbm("stone_resource_" + mat));
                }
                if ("rare".equals(mat)) {
                    ids.add(hbm("ore_gneiss_rare"));
                }
            } else if ("crystal".equals(parts.prefix)) {
                ids.add(hbm("crystal_" + mat));
                if ("cryolite".equals(mat)) {
                    ids.add(hbm("chunk_ore_cryolite"));
                }
            } else if ("block".equals(parts.prefix)) {
                ids.add(hbm("block_" + mat));
            } else if ("shell".equals(parts.prefix)) {
                ids.add(hbm("shell_" + mat));
                ids.add(hbm("shell"));
            } else if ("ntmpipe".equals(parts.prefix)) {
                ids.add(hbm("pipe_" + mat));
                ids.add(hbm("pipe"));
            } else if ("any".equals(parts.prefix) && "concrete".equals(mat)) {
                ids.add(hbm("concrete"));
                ids.add(hbm("concrete_smooth"));
                ids.add(hbm("concrete_asbestos"));
                ids.add(hbm("ducrete"));
            } else if ("wireFine".equals(parts.prefix)) {
                ids.add(hbm("wire_" + mat));
            } else if ("wireDense".equals(parts.prefix)) {
                ids.add(hbm("wire_dense_" + mat));
                ids.add(hbm("wire_dense"));
            } else if ("plateWelded".equals(parts.prefix) || "plateSextuple".equals(parts.prefix)) {
                ids.add(hbm("plate_welded_" + mat));
                ids.add(hbm("plate_welded"));
            } else if ("plateCast".equals(parts.prefix)) {
                ids.add(hbm("plate_cast_" + mat));
                ids.add(hbm("plate_cast"));
            } else if ("plateTriple".equals(parts.prefix)) {
                ids.add(hbm("plate_triple_" + mat));
                ids.add(hbm("plate_triple"));
            } else if ("bolt".equals(parts.prefix)) {
                ids.add(hbm("bolt_" + mat));
                ids.add(hbm("bolt"));
            }
        }
        if ("ingotAnyHighexplosive".equals(ore)) {
            ids.add(hbm("ball_tnt"));
            ids.add(hbm("ball_tatb"));
        }
        if ("ingotAnyHardPlastic".equals(ore)) {
            ids.add(hbm("ingot_pc"));
            ids.add(hbm("ingot_pvc"));
        }
        if ("sand".equals(ore)) {
            ids.add(mc("sand"));
        }
        if ("cobblestone".equals(ore)) {
            ids.add(mc("cobblestone"));
        }
        if ("slimeball".equals(ore)) {
            ids.add(mc("slime_ball"));
        }
        ids.remove(null);
        return List.copyOf(ids);
    }

    static List<TagKey<Item>> tags(String ore) {
        List<TagKey<Item>> tags = new ArrayList<>();
        Split parts = split(ore);
        String snake = materialPath(parts.material);
        if (snake.isEmpty() && parts.prefix.isEmpty()) {
            addPlainTag(ore, tags);
            return tags;
        }
        String forgePath = switch (parts.prefix) {
            case "ingot" -> "ingots/" + snake;
            case "dust" -> "dusts/" + snake;
            case "plate" -> "plates/" + snake;
            case "nugget" -> "nuggets/" + snake;
            case "gem" -> "gems/" + snake;
            case "ore" -> "ores/" + snake;
            case "block" -> "storage_blocks/" + snake;
            default -> null;
        };
        if (forgePath != null) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("forge", forgePath)));
        }
        if ("plankWood".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "planks")));
        }
        if ("logWood".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "logs")));
        }
        if ("KEY_SAND".equals(ore) || "sand".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "sand")));
        }
        if ("KEY_ANYPANE".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes")));
        }
        if ("KEY_GREEN".equals(ore) || "dyeGreen".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/green")));
        }
        if ("any".equals(parts.prefix) && "concrete".equals(snake)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("hbm", "any_concrete")));
        }
        return tags;
    }

    private static void addPlainTag(String ore, List<TagKey<Item>> tags) {
        if ("plankWood".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "planks")));
        } else if ("logWood".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "logs")));
        } else if ("KEY_SAND".equals(ore) || "sand".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "sand")));
        } else if ("KEY_ANYPANE".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes")));
        } else if ("KEY_GREEN".equals(ore) || "dyeGreen".equals(ore)) {
            tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/green")));
        }
    }

    private static void addVanilla(String ore, Set<ResourceLocation> ids) {
        switch (ore) {
            case "ingotIron" -> ids.add(mc("iron_ingot"));
            case "blockIron" -> ids.add(mc("iron_block"));
            case "oreIron" -> {
                ids.add(mc("iron_ore"));
                ids.add(mc("deepslate_iron_ore"));
            }
            case "ingotGold" -> ids.add(mc("gold_ingot"));
            case "nuggetGold" -> ids.add(mc("gold_nugget"));
            case "oreGold" -> {
                ids.add(mc("gold_ore"));
                ids.add(mc("deepslate_gold_ore"));
            }
            case "ingotCopper" -> ids.add(mc("copper_ingot"));
            case "oreCopper" -> {
                ids.add(mc("copper_ore"));
                ids.add(mc("deepslate_copper_ore"));
            }
            case "oreCoal" -> {
                ids.add(mc("coal_ore"));
                ids.add(mc("deepslate_coal_ore"));
            }
            case "oreDiamond" -> {
                ids.add(mc("diamond_ore"));
                ids.add(mc("deepslate_diamond_ore"));
            }
            case "oreEmerald" -> {
                ids.add(mc("emerald_ore"));
                ids.add(mc("deepslate_emerald_ore"));
            }
            case "oreLapis" -> {
                ids.add(mc("lapis_ore"));
                ids.add(mc("deepslate_lapis_ore"));
            }
            case "oreRedstone" -> {
                ids.add(mc("redstone_ore"));
                ids.add(mc("deepslate_redstone_ore"));
            }
            case "oreNetherQuartz" -> ids.add(mc("nether_quartz_ore"));
            case "gemCoal", "ingotCoal" -> ids.add(mc("coal"));
            case "dustCoal" -> ids.add(hbm("powder_coal"));
            case "blockCoal" -> ids.add(mc("coal_block"));
            case "blockRedstone" -> ids.add(mc("redstone_block"));
            case "dustRedstone", "gemRedstone" -> ids.add(mc("redstone"));
            case "gemDiamond" -> ids.add(mc("diamond"));
            case "gemEmerald" -> ids.add(mc("emerald"));
            case "gemNetherQuartz", "gemQuartz" -> ids.add(mc("quartz"));
            case "dustLapis" -> ids.add(mc("lapis_lazuli"));
            case "ingotBrick" -> ids.add(mc("brick"));
            case "dustGlowstone" -> ids.add(mc("glowstone_dust"));
            case "KEY_SAND" -> ids.add(mc("sand"));
            case "KEY_ANYPANE" -> ids.add(mc("glass_pane"));
            case "KEY_GREEN", "dyeGreen" -> ids.add(mc("green_dye"));
            default -> {
            }
        }
    }

    public static String materialPath(String material) {
        if (material == null || material.isEmpty()) {
            return "";
        }
        String alias = MATERIAL_ALIAS.get(material);
        if (alias != null) {
            return alias;
        }
        return camelToSnake(material);
    }

    public static String camelToSnake(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                out.append('_');
            }
            out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }

    private static ResourceLocation parseId(String id) {
        return ResourceLocation.tryParse(id.toLowerCase(Locale.ROOT));
    }

    private static ResourceLocation hbm(String path) {
        return new ResourceLocation("hbm", path);
    }

    private static ResourceLocation mc(String path) {
        return new ResourceLocation("minecraft", path);
    }

    public record Split(String prefix, String material) {
    }
}
