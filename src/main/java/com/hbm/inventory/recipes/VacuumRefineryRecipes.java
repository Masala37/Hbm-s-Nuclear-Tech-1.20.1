package com.hbm.inventory.recipes;

import com.hbm.registry.ModFluids;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code VacuumRefineryRecipes}. Input pressure skipped (compressor is not ported).
 */
public final class VacuumRefineryRecipes {
    public static final int INPUT_MB = 100;
    public static final int POWER_PER_OP = 10_000;

    public static final int VAC_HEAVY = 40;
    public static final int VAC_REFORM = 25;
    public static final int VAC_LIGHT = 20;
    public static final int VAC_GAS = 15;

    private VacuumRefineryRecipes() {
    }

    public record Outputs(Fluid heavy, int heavyMb, Fluid reformate, int reformateMb,
                          Fluid light, int lightMb, Fluid gas, int gasMb) {
    }

    public static @Nullable Outputs get(@Nullable Fluid input) {
        if (input == null) {
            return null;
        }
        if (is(input, ModFluids.OIL) && ready(ModFluids.HEAVYOIL_VACUUM) && ready(ModFluids.REFORMATE)
                && ready(ModFluids.LIGHTOIL_VACUUM) && ready(ModFluids.SOURGAS)) {
            return new Outputs(ModFluids.HEAVYOIL_VACUUM.source.get(), VAC_HEAVY,
                    ModFluids.REFORMATE.source.get(), VAC_REFORM,
                    ModFluids.LIGHTOIL_VACUUM.source.get(), VAC_LIGHT,
                    ModFluids.SOURGAS.source.get(), VAC_GAS);
        }
        if (is(input, ModFluids.OIL_DS) && ready(ModFluids.HEAVYOIL_VACUUM) && ready(ModFluids.REFORMATE)
                && ready(ModFluids.LIGHTOIL_VACUUM) && ready(ModFluids.REFORMGAS)) {
            return new Outputs(ModFluids.HEAVYOIL_VACUUM.source.get(), VAC_HEAVY,
                    ModFluids.REFORMATE.source.get(), VAC_REFORM,
                    ModFluids.LIGHTOIL_VACUUM.source.get(), VAC_LIGHT,
                    ModFluids.REFORMGAS.source.get(), VAC_GAS);
        }
        return null;
    }

    public static List<Fluid> inputs() {
        List<Fluid> list = new ArrayList<>();
        addIfRecipe(list, ModFluids.OIL);
        addIfRecipe(list, ModFluids.OIL_DS);
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
