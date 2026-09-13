package com.hbm.inventory.recipes;

import com.hbm.registry.ModFluids;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code HydrotreatingRecipes} rows whose fluids exist in this port.
 * Skipped: diesel_crack, diesel_crack_reform, coaloil (missing fluids).
 * Hydrogen pressure 1 is skipped — this port has no pressure tanks.
 * Inputs are always 100 mB.
 */
public final class HydrotreatingRecipes {
    public static final int INPUT_MB = 100;
    public static final int POWER_PER_OP = 20_000;
    public static final int TICK_DELAY = 2;

    public static final int OIL_H2 = 5;
    public static final int OIL_DS = 90;
    public static final int OIL_SOUR = 15;
    public static final int CRACK_H2 = 5;
    public static final int CRACK_DS = 90;
    public static final int CRACK_SOUR = 15;
    public static final int GAS_H2 = 5;
    public static final int GAS_PETRO = 80;
    public static final int GAS_SOUR = 15;

    private HydrotreatingRecipes() {
    }

    public record Outputs(int hydrogenMb, Fluid left, int leftMb, Fluid right, int rightMb) {
    }

    public static @Nullable Outputs get(@Nullable Fluid input) {
        if (input == null) {
            return null;
        }
        if (is(input, ModFluids.OIL) && ready(ModFluids.HYDROGEN) && ready(ModFluids.OIL_DS)
                && ready(ModFluids.SOURGAS)) {
            return new Outputs(OIL_H2, ModFluids.OIL_DS.source.get(), OIL_DS,
                    ModFluids.SOURGAS.source.get(), OIL_SOUR);
        }
        if (is(input, ModFluids.CRACKOIL) && ready(ModFluids.HYDROGEN) && ready(ModFluids.CRACKOIL_DS)
                && ready(ModFluids.SOURGAS)) {
            return new Outputs(CRACK_H2, ModFluids.CRACKOIL_DS.source.get(), CRACK_DS,
                    ModFluids.SOURGAS.source.get(), CRACK_SOUR);
        }
        if (is(input, ModFluids.GAS) && ready(ModFluids.HYDROGEN) && ready(ModFluids.PETROLEUM)
                && ready(ModFluids.SOURGAS)) {
            return new Outputs(GAS_H2, ModFluids.PETROLEUM.source.get(), GAS_PETRO,
                    ModFluids.SOURGAS.source.get(), GAS_SOUR);
        }
        return null;
    }

    public static List<Fluid> inputs() {
        List<Fluid> list = new ArrayList<>();
        addIfRecipe(list, ModFluids.OIL);
        addIfRecipe(list, ModFluids.CRACKOIL);
        addIfRecipe(list, ModFluids.GAS);
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
