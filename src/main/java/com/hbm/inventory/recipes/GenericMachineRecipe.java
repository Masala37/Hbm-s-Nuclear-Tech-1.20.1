package com.hbm.inventory.recipes;

import java.util.List;
import java.util.Locale;

/**
 * One 1.7.10 {@code GenericRecipe} row. Ingredients keep ore-dict / item ids from legacy.
 */
public final class GenericMachineRecipe {
    public record ItemInput(String ore, String item, int count, String meta, float chance) {
        public ItemInput(String ore, String item, int count, String meta) {
            this(ore, item, count, meta, 1.0F);
        }

        public boolean isOre() {
            return ore != null && !ore.isEmpty();
        }
    }

    public record FluidInput(String fluid, int amount) {
    }

    private final String name;
    private final int duration;
    private final long power;
    private final boolean named;
    private final List<String> pools;
    private final String autoSwitchGroup;
    private final List<ItemInput> inputItem;
    private final List<ItemInput> outputItem;
    private final List<FluidInput> inputFluid;
    private final List<FluidInput> outputFluid;

    public GenericMachineRecipe(String name, int duration, long power, boolean named, List<String> pools,
                                String autoSwitchGroup, List<ItemInput> inputItem, List<ItemInput> outputItem,
                                List<FluidInput> inputFluid, List<FluidInput> outputFluid) {
        this.name = name;
        this.duration = duration;
        this.power = power;
        this.named = named;
        this.pools = pools == null ? List.of() : List.copyOf(pools);
        this.autoSwitchGroup = autoSwitchGroup;
        this.inputItem = inputItem == null ? List.of() : List.copyOf(inputItem);
        this.outputItem = outputItem == null ? List.of() : List.copyOf(outputItem);
        this.inputFluid = inputFluid == null ? List.of() : List.copyOf(inputFluid);
        this.outputFluid = outputFluid == null ? List.of() : List.copyOf(outputFluid);
    }

    public String name() {
        return name;
    }

    public int duration() {
        return duration;
    }

    public long power() {
        return power;
    }

    public boolean named() {
        return named;
    }

    public List<String> pools() {
        return pools;
    }

    public String autoSwitchGroup() {
        return autoSwitchGroup;
    }

    public List<ItemInput> inputItem() {
        return inputItem;
    }

    public List<ItemInput> outputItem() {
        return outputItem;
    }

    public List<FluidInput> inputFluid() {
        return inputFluid;
    }

    public List<FluidInput> outputFluid() {
        return outputFluid;
    }

    /** 1.7 {@code GenericRecipe.matchesSearch}: internal name plus output ids. */
    public boolean matchesSearch(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.toLowerCase(Locale.ROOT);
        if (name.toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }
        for (ItemInput output : outputItem) {
            if (output.item() != null && output.item().toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
            if (output.ore() != null && output.ore().toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        for (FluidInput output : outputFluid) {
            if (output.fluid() != null && output.fluid().toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
