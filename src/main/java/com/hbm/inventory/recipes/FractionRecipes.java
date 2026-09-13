package com.hbm.inventory.recipes;

import com.hbm.registry.ModFluids;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code FractionRecipes} Oil-1 plus vacuum rows whose fluids exist.
 * Crack/DS/coker/aromatics skipped.
 * Inputs are always 100 mB.
 */
public final class FractionRecipes {
    public static final int INPUT_MB = 100;
    public static final int TICK_DELAY = 10;

    public static final int HEAVYOIL_BITUMEN = 30;
    public static final int HEAVYOIL_SMEAR = 70;
    public static final int SMEAR_HEATINGOIL = 60;
    public static final int SMEAR_LUBRICANT = 40;
    public static final int NAPHTHA_HEATINGOIL = 40;
    public static final int NAPHTHA_DIESEL = 60;
    public static final int LIGHTOIL_DIESEL = 40;
    public static final int LIGHTOIL_KEROSENE = 60;
    public static final int VAC_HEAVY_SMEAR = 40;
    public static final int VAC_HEAVY_HEAT = 60;
    public static final int VAC_LIGHT_KEROSENE = 70;
    public static final int VAC_LIGHT_REFORMGAS = 30;

    private FractionRecipes() {
    }

    public record Outputs(Fluid left, int leftMb, Fluid right, int rightMb) {
    }

    public static @Nullable Outputs get(@Nullable Fluid input) {
        if (input == null) {
            return null;
        }
        if (is(input, ModFluids.HEAVYOIL) && ready(ModFluids.BITUMEN) && ready(ModFluids.SMEAR)) {
            return new Outputs(ModFluids.BITUMEN.source.get(), HEAVYOIL_BITUMEN,
                    ModFluids.SMEAR.source.get(), HEAVYOIL_SMEAR);
        }
        if (is(input, ModFluids.SMEAR) && ready(ModFluids.HEATINGOIL) && ready(ModFluids.LUBRICANT)) {
            return new Outputs(ModFluids.HEATINGOIL.source.get(), SMEAR_HEATINGOIL,
                    ModFluids.LUBRICANT.source.get(), SMEAR_LUBRICANT);
        }
        if (is(input, ModFluids.NAPHTHA) && ready(ModFluids.HEATINGOIL) && ready(ModFluids.DIESEL)) {
            return new Outputs(ModFluids.HEATINGOIL.source.get(), NAPHTHA_HEATINGOIL,
                    ModFluids.DIESEL.source.get(), NAPHTHA_DIESEL);
        }
        if (is(input, ModFluids.LIGHTOIL) && ready(ModFluids.DIESEL) && ready(ModFluids.KEROSENE)) {
            return new Outputs(ModFluids.DIESEL.source.get(), LIGHTOIL_DIESEL,
                    ModFluids.KEROSENE.source.get(), LIGHTOIL_KEROSENE);
        }
        if (is(input, ModFluids.HEAVYOIL_VACUUM) && ready(ModFluids.SMEAR) && ready(ModFluids.HEATINGOIL_VACUUM)) {
            return new Outputs(ModFluids.SMEAR.source.get(), VAC_HEAVY_SMEAR,
                    ModFluids.HEATINGOIL_VACUUM.source.get(), VAC_HEAVY_HEAT);
        }
        if (is(input, ModFluids.LIGHTOIL_VACUUM) && ready(ModFluids.KEROSENE) && ready(ModFluids.REFORMGAS)) {
            return new Outputs(ModFluids.KEROSENE.source.get(), VAC_LIGHT_KEROSENE,
                    ModFluids.REFORMGAS.source.get(), VAC_LIGHT_REFORMGAS);
        }
        return null;
    }

    public static List<Fluid> inputs() {
        List<Fluid> list = new ArrayList<>();
        addIfRecipe(list, ModFluids.HEAVYOIL);
        addIfRecipe(list, ModFluids.SMEAR);
        addIfRecipe(list, ModFluids.NAPHTHA);
        addIfRecipe(list, ModFluids.LIGHTOIL);
        addIfRecipe(list, ModFluids.HEAVYOIL_VACUUM);
        addIfRecipe(list, ModFluids.LIGHTOIL_VACUUM);
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
