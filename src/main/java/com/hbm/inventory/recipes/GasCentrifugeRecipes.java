package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 1.7.10 {@code GasCentrifugeRecipes}. Source JSON is copied from legacy {@code register()}.
 * Pseudofluids are internal cascade types, not Forge fluids.
 */
public final class GasCentrifugeRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/gas_centrifuge.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/gas_centrifuge.json");
    public static final String NONE = "NONE";

    public static final class PseudoFluidType {
        private final String name;
        private final int consumed;
        private final int produced;
        private final String output;
        private final boolean highSpeed;
        private final List<IngredientRef> items;

        public PseudoFluidType(String name, int consumed, int produced, String output, boolean highSpeed,
                               List<IngredientRef> items) {
            this.name = name;
            this.consumed = consumed;
            this.produced = produced;
            this.output = output == null || output.isEmpty() ? NONE : output;
            this.highSpeed = highSpeed;
            this.items = items == null ? List.of() : List.copyOf(items);
        }

        public String name() {
            return name;
        }

        public int fluidConsumed() {
            return consumed;
        }

        public int fluidProduced() {
            return produced;
        }

        public boolean highSpeed() {
            return highSpeed;
        }

        public boolean isNone() {
            return NONE.equals(name);
        }

        public PseudoFluidType outputType() {
            return byName(output);
        }

        public List<IngredientRef> items() {
            return items;
        }

        public ItemStack[] outputStacks() {
            List<ItemStack> stacks = new ArrayList<>();
            for (IngredientRef item : items) {
                ItemStack stack = item.resultStack();
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            return stacks.toArray(ItemStack[]::new);
        }

        public Component displayName() {
            return Component.translatable("hbmpseudofluid." + name.toLowerCase(Locale.ROOT));
        }
    }

    private static Map<String, PseudoFluidType> types = Map.of(NONE, none());
    private static List<String> conversionFluids = List.of();
    private static Map<String, String> conversionNames = Map.of();

    private GasCentrifugeRecipes() {
    }

    public static PseudoFluidType byName(String name) {
        if (name == null || name.isEmpty()) {
            return types.get(NONE);
        }
        PseudoFluidType type = types.get(name);
        return type == null ? types.get(NONE) : type;
    }

    public static Map<String, PseudoFluidType> types() {
        return types;
    }

    public static @Nullable PseudoFluidType conversion(Fluid fluid) {
        if (fluid == null) {
            return null;
        }
        ResourceLocation key = ForgeRegistries.FLUIDS.getKey(fluid);
        if (key == null) {
            return null;
        }
        String name = conversionNames.get(key.toString());
        return name == null ? null : byName(name);
    }

    public static @Nullable Fluid cycleNext(@Nullable Fluid current) {
        List<Fluid> fluids = conversionFluids();
        if (fluids.isEmpty()) {
            return current;
        }
        int index = 0;
        for (int i = 0; i < fluids.size(); i++) {
            if (fluids.get(i) == current) {
                index = (i + 1) % fluids.size();
                break;
            }
        }
        return fluids.get(index);
    }

    public static @Nullable Fluid firstConversion() {
        List<Fluid> fluids = conversionFluids();
        return fluids.isEmpty() ? null : fluids.get(0);
    }

    public static boolean isConversion(@Nullable Fluid fluid) {
        return conversion(fluid) != null;
    }

    public static boolean isConversionTarget(PseudoFluidType type) {
        return type != null && conversionNames.containsValue(type.name());
    }

    public static List<Fluid> conversionFluids() {
        List<Fluid> fluids = new ArrayList<>();
        for (String id : conversionFluids) {
            Fluid fluid = GenericRecipeMatch.fluid(id);
            if (fluid != null) {
                fluids.add(fluid);
            }
        }
        return fluids;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        Map<String, PseudoFluidType> loaded = new LinkedHashMap<>();
        loaded.put(NONE, none());
        if (root.has("types")) {
            for (JsonElement el : root.getAsJsonArray("types")) {
                JsonObject obj = el.getAsJsonObject();
                String name = obj.get("name").getAsString();
                List<IngredientRef> items = obj.has("items")
                        ? IngredientRef.list(obj.getAsJsonArray("items"))
                        : List.of();
                if (filterMissing) {
                    List<IngredientRef> kept = new ArrayList<>();
                    for (IngredientRef item : items) {
                        if (item.existsInRegistry()) {
                            kept.add(item);
                        }
                    }
                    items = kept;
                }
                loaded.put(name, new PseudoFluidType(
                        name,
                        obj.has("consumed") ? obj.get("consumed").getAsInt() : 0,
                        obj.has("produced") ? obj.get("produced").getAsInt() : 0,
                        obj.has("output") ? obj.get("output").getAsString() : NONE,
                        obj.has("highSpeed") && obj.get("highSpeed").getAsBoolean(),
                        items));
            }
        }
        types = Collections.unmodifiableMap(loaded);

        List<String> fluids = new ArrayList<>();
        Map<String, String> conversions = new LinkedHashMap<>();
        if (root.has("conversions")) {
            for (JsonElement el : root.getAsJsonArray("conversions")) {
                JsonObject obj = el.getAsJsonObject();
                String fluid = obj.get("fluid").getAsString();
                String pseudo = obj.get("pseudo").getAsString();
                if (filterMissing && GenericRecipeMatch.fluid(fluid) == null) {
                    continue;
                }
                fluids.add(fluid);
                conversions.put(fluid, pseudo);
            }
        }
        conversionFluids = Collections.unmodifiableList(fluids);
        conversionNames = Collections.unmodifiableMap(conversions);
    }

    private static PseudoFluidType none() {
        return new PseudoFluidType(NONE, 0, 0, NONE, false, List.of());
    }
}
