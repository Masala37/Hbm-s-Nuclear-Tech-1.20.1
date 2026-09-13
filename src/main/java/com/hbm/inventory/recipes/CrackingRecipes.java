package com.hbm.inventory.recipes;

import com.hbm.registry.ModFluids;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code CrackingRecipes} rows whose fluids exist in this port.
 * Skipped: bitumen/wood/xylene/gas (aromatics/unsaturateds), diesel_crack, vacuum, reformate, biogas.
 * Inputs are always 100 mB oil + 200 mB steam; spent steam is 2 mB per op.
 */
public final class CrackingRecipes {
    public static final int INPUT_MB = 100;
    public static final int STEAM_MB = 200;
    public static final int SPENT_MB = 2;
    public static final int TICK_DELAY = 5;
    public static final int OPS_PER_PULSE = 2;

    public static final int OIL_CRACKOIL = 80;
    public static final int OIL_PETRO = 20;
    public static final int SMEAR_NAPHTHA = 60;
    public static final int SMEAR_PETRO = 40;
    public static final int DIESEL_KERO = 40;
    public static final int DIESEL_PETRO = 30;
    public static final int KERO_PETRO = 60;

    private CrackingRecipes() {
    }

    public record Outputs(Fluid left, int leftMb, @Nullable Fluid right, int rightMb) {
    }

    public static @Nullable Outputs get(@Nullable Fluid input) {
        if (input == null) {
            return null;
        }
        if (is(input, ModFluids.OIL) && ready(ModFluids.CRACKOIL) && ready(ModFluids.PETROLEUM)) {
            return new Outputs(ModFluids.CRACKOIL.source.get(), OIL_CRACKOIL,
                    ModFluids.PETROLEUM.source.get(), OIL_PETRO);
        }
        if (is(input, ModFluids.SMEAR) && ready(ModFluids.NAPHTHA) && ready(ModFluids.PETROLEUM)) {
            return new Outputs(ModFluids.NAPHTHA.source.get(), SMEAR_NAPHTHA,
                    ModFluids.PETROLEUM.source.get(), SMEAR_PETRO);
        }
        if (is(input, ModFluids.DIESEL) && ready(ModFluids.KEROSENE) && ready(ModFluids.PETROLEUM)) {
            return new Outputs(ModFluids.KEROSENE.source.get(), DIESEL_KERO,
                    ModFluids.PETROLEUM.source.get(), DIESEL_PETRO);
        }
        if (is(input, ModFluids.KEROSENE) && ready(ModFluids.PETROLEUM)) {
            return new Outputs(ModFluids.PETROLEUM.source.get(), KERO_PETRO, null, 0);
        }
        return null;
    }

    public static List<Fluid> inputs() {
        List<Fluid> list = new ArrayList<>();
        addIfRecipe(list, ModFluids.OIL);
        addIfRecipe(list, ModFluids.SMEAR);
        addIfRecipe(list, ModFluids.DIESEL);
        addIfRecipe(list, ModFluids.KEROSENE);
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
