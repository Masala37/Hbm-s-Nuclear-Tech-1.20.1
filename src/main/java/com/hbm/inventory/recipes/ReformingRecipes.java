package com.hbm.inventory.recipes;

import com.hbm.registry.ModFluids;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code ReformingRecipes} rows whose fluids exist in this port.
 * Skipped: naphtha_crack/coker, lightoil(+crack), petroleum (unsaturateds), cholesterol.
 */
public final class ReformingRecipes {
    public static final int INPUT_MB = 100;
    public static final int POWER_PER_OP = 20_000;

    public static final int HEAT_NAPHTHA = 50;
    public static final int HEAT_PETRO = 15;
    public static final int HEAT_H2 = 10;
    public static final int NAPHTHA_REFORMATE = 50;
    public static final int NAPHTHA_PETRO = 15;
    public static final int NAPHTHA_H2 = 10;
    public static final int SOUR_ACID = 75;
    public static final int SOUR_PETRO = 10;
    public static final int SOUR_H2 = 15;

    private ReformingRecipes() {
    }

    public record Outputs(Fluid left, int leftMb, Fluid mid, int midMb, Fluid right, int rightMb) {
    }

    public static @Nullable Outputs get(@Nullable Fluid input) {
        if (input == null) {
            return null;
        }
        if (is(input, ModFluids.HEATINGOIL) && ready(ModFluids.NAPHTHA) && ready(ModFluids.PETROLEUM)
                && ready(ModFluids.HYDROGEN)) {
            return new Outputs(ModFluids.NAPHTHA.source.get(), HEAT_NAPHTHA,
                    ModFluids.PETROLEUM.source.get(), HEAT_PETRO,
                    ModFluids.HYDROGEN.source.get(), HEAT_H2);
        }
        if (is(input, ModFluids.NAPHTHA) && ready(ModFluids.REFORMATE) && ready(ModFluids.PETROLEUM)
                && ready(ModFluids.HYDROGEN)) {
            return new Outputs(ModFluids.REFORMATE.source.get(), NAPHTHA_REFORMATE,
                    ModFluids.PETROLEUM.source.get(), NAPHTHA_PETRO,
                    ModFluids.HYDROGEN.source.get(), NAPHTHA_H2);
        }
        if (is(input, ModFluids.SOURGAS) && ready(ModFluids.SULFURIC_ACID) && ready(ModFluids.PETROLEUM)
                && ready(ModFluids.HYDROGEN)) {
            return new Outputs(ModFluids.SULFURIC_ACID.source.get(), SOUR_ACID,
                    ModFluids.PETROLEUM.source.get(), SOUR_PETRO,
                    ModFluids.HYDROGEN.source.get(), SOUR_H2);
        }
        return null;
    }

    public static List<Fluid> inputs() {
        List<Fluid> list = new ArrayList<>();
        addIfRecipe(list, ModFluids.NAPHTHA);
        addIfRecipe(list, ModFluids.HEATINGOIL);
        addIfRecipe(list, ModFluids.SOURGAS);
        return list;
    }

    public static @Nullable Fluid cycleNext(@Nullable Fluid current) {
        List<Fluid> list = inputs();
        if (list.isEmpty()) {
            return current;
        }
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == current) {
                index = (i + 1) % list.size();
                break;
            }
        }
        return list.get(index);
    }

    private static void addIfRecipe(List<Fluid> list, ModFluids.FluidEntry entry) {
        if (!ready(entry)) {
            return;
        }
        Fluid fluid = entry.source.get();
        if (get(fluid) != null) {
            list.add(fluid);
        }
    }

    private static boolean is(Fluid fluid, ModFluids.FluidEntry entry) {
        return ready(entry) && fluid == entry.source.get();
    }

    private static boolean ready(ModFluids.FluidEntry entry) {
        return entry.source.isPresent();
    }
}
