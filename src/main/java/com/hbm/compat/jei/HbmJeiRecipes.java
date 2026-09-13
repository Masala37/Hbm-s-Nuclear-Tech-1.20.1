package com.hbm.compat.jei;

import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.CrackingRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.DiFurnaceRecipes;
import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.inventory.recipes.GasCentrifugeRecipes;
import com.hbm.inventory.recipes.GenericMachineRecipe;
import com.hbm.inventory.recipes.HydrotreatingRecipes;
import com.hbm.inventory.recipes.MachineRecipeBootstrap;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.PUREXRecipes;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.inventory.recipes.RefineryRecipes;
import com.hbm.inventory.recipes.ReformingRecipes;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.recipes.VacuumRefineryRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModItems;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

final class HbmJeiRecipes {
    static final RecipeType<PressRecipes.PressRecipe> PRESS = type("press", PressRecipes.PressRecipe.class);
    static final RecipeType<ShredderRow> SHREDDER = type("shredder", ShredderRow.class);
    static final RecipeType<CentrifugeRecipes.CentrifugeRecipe> CENTRIFUGE =
            type("centrifuge", CentrifugeRecipes.CentrifugeRecipe.class);
    static final RecipeType<GasCentRow> GAS_CENTRIFUGE = type("gas_centrifuge", GasCentRow.class);
    static final RecipeType<AnvilRecipes.AnvilConstructionRecipe> ANVIL =
            type("anvil", AnvilRecipes.AnvilConstructionRecipe.class);
    static final RecipeType<AnvilRecipes.AnvilSmithingRecipe> SMITHING =
            type("smithing", AnvilRecipes.AnvilSmithingRecipe.class);
    static final RecipeType<DiFurnaceRecipes.DiFurnaceRecipe> DI_FURNACE =
            type("di_furnace", DiFurnaceRecipes.DiFurnaceRecipe.class);
    static final RecipeType<BlastFurnaceRecipes.BlastFurnaceRecipe> BLAST =
            type("blast_furnace", BlastFurnaceRecipes.BlastFurnaceRecipe.class);
    static final RecipeType<GenericMachineRecipe> ASSEMBLER =
            type("assembler", GenericMachineRecipe.class);
    static final RecipeType<GenericMachineRecipe> CHEMPLANT =
            type("chemplant", GenericMachineRecipe.class);
    static final RecipeType<GenericMachineRecipe> PUREX = type("purex", GenericMachineRecipe.class);
    static final RecipeType<CrystallizerRecipes.CrystallizerRecipe> CRYSTALLIZER =
            type("crystallizer", CrystallizerRecipes.CrystallizerRecipe.class);
    static final RecipeType<MixerRecipes.MixerRecipe> MIXER = type("mixer", MixerRecipes.MixerRecipe.class);
    static final RecipeType<SolderingRecipes.SolderingRecipe> SOLDERING =
            type("soldering", SolderingRecipes.SolderingRecipe.class);
    static final RecipeType<ArcWelderRecipes.ArcWelderRecipe> ARC_WELDER =
            type("arc_welder", ArcWelderRecipes.ArcWelderRecipe.class);
    static final RecipeType<SILEXRecipes.SILEXRecipe> SILEX = type("silex", SILEXRecipes.SILEXRecipe.class);
    static final RecipeType<FluidIoRow> REFINERY = type("refinery", FluidIoRow.class);
    static final RecipeType<FluidIoRow> FRACTION = type("fraction", FluidIoRow.class);
    static final RecipeType<FluidIoRow> CRACKING = type("cracking", FluidIoRow.class);
    static final RecipeType<FluidIoRow> HYDROTREATER = type("hydrotreater", FluidIoRow.class);
    static final RecipeType<FluidIoRow> REFORMER = type("reformer", FluidIoRow.class);
    static final RecipeType<FluidIoRow> VACUUM = type("vacuum", FluidIoRow.class);

    record ShredderRow(List<ItemStack> input, ItemStack output) {
    }

    record GasCentRow(Fluid conversion, GasCentrifugeRecipes.PseudoFluidType type) {
    }

    record FluidIoRow(List<FluidStack> inputs, List<FluidStack> outputs, List<ItemStack> extras, String note) {
    }

    private HbmJeiRecipes() {
    }

    static void ensureLoaded() {
        if (PressRecipes.recipes().isEmpty()
                || ShredderRecipes.recipes().isEmpty()
                || AnvilRecipes.getConstruction().isEmpty()) {
            MachineRecipeBootstrap.load();
        }
    }

    static List<ShredderRow> shredder() {
        List<ShredderRow> rows = new ArrayList<>();
        for (ShredderRecipes.ShredderRecipe recipe : ShredderRecipes.recipes()) {
            List<ItemStack> in = HbmJeiIngredients.stacks(recipe.input());
            ItemStack out = recipe.output().resultStack();
            if (!in.isEmpty() && !out.isEmpty()) {
                rows.add(new ShredderRow(in, out));
            }
        }
        for (var entry : ShredderRecipes.generated().entrySet()) {
            ItemStack in = new ItemStack(ForgeRegistries.ITEMS.getValue(entry.getKey()));
            ItemStack out = entry.getValue();
            if (!in.isEmpty() && out != null && !out.isEmpty()) {
                rows.add(new ShredderRow(List.of(in), out.copy()));
            }
        }
        return rows;
    }

    static List<GasCentRow> gasCentrifuge() {
        List<GasCentRow> rows = new ArrayList<>();
        for (GasCentrifugeRecipes.PseudoFluidType type : GasCentrifugeRecipes.types().values()) {
            if (type.isNone() || type.items().isEmpty()) {
                continue;
            }
            Fluid conversion = null;
            for (Fluid fluid : GasCentrifugeRecipes.conversionFluids()) {
                if (GasCentrifugeRecipes.conversion(fluid) == type) {
                    conversion = fluid;
                    break;
                }
            }
            rows.add(new GasCentRow(conversion, type));
        }
        return rows;
    }

    static List<FluidIoRow> refinery() {
        if (!ModFluids.HOTOIL.source.isPresent()) {
            return List.of();
        }
        Fluid hot = ModFluids.HOTOIL.source.get();
        List<FluidStack> outs = new ArrayList<>();
        addFluid(outs, ModFluids.HEAVYOIL, RefineryRecipes.HEAVY_MB);
        addFluid(outs, ModFluids.NAPHTHA, RefineryRecipes.NAPHTHA_MB);
        addFluid(outs, ModFluids.LIGHTOIL, RefineryRecipes.LIGHT_MB);
        addFluid(outs, ModFluids.PETROLEUM, RefineryRecipes.PETRO_MB);
        if (outs.size() < 4) {
            return List.of();
        }
        List<ItemStack> extras = new ArrayList<>();
        extras.add(new ItemStack(ModItems.SULFUR.get()));
        return List.of(new FluidIoRow(
                List.of(new FluidStack(hot, RefineryRecipes.INPUT_MB)),
                outs,
                extras,
                "jei.hbm.sulfur_every"));
    }

    static List<FluidIoRow> fraction() {
        List<FluidIoRow> rows = new ArrayList<>();
        for (Fluid input : FractionRecipes.inputs()) {
            FractionRecipes.Outputs out = FractionRecipes.get(input);
            if (out == null) {
                continue;
            }
            rows.add(new FluidIoRow(
                    List.of(new FluidStack(input, FractionRecipes.INPUT_MB)),
                    List.of(new FluidStack(out.left(), out.leftMb()), new FluidStack(out.right(), out.rightMb())),
                    List.of(),
                    null));
        }
        return rows;
    }

    static List<FluidIoRow> cracking() {
        List<FluidIoRow> rows = new ArrayList<>();
        for (Fluid input : CrackingRecipes.inputs()) {
            CrackingRecipes.Outputs out = CrackingRecipes.get(input);
            if (out == null) {
                continue;
            }
            List<FluidStack> ins = new ArrayList<>();
            ins.add(new FluidStack(input, CrackingRecipes.INPUT_MB));
            if (ModFluids.STEAM.source.isPresent()) {
                ins.add(new FluidStack(ModFluids.STEAM.source.get(), CrackingRecipes.STEAM_MB));
            }
            List<FluidStack> outs = new ArrayList<>();
            outs.add(new FluidStack(out.left(), out.leftMb()));
            if (out.right() != null && out.rightMb() > 0) {
                outs.add(new FluidStack(out.right(), out.rightMb()));
            }
            if (ModFluids.SPENTSTEAM.source.isPresent()) {
                outs.add(new FluidStack(ModFluids.SPENTSTEAM.source.get(), CrackingRecipes.SPENT_MB));
            }
            rows.add(new FluidIoRow(ins, outs, List.of(), null));
        }
        return rows;
    }

    static List<FluidIoRow> hydrotreater() {
        List<FluidIoRow> rows = new ArrayList<>();
        for (Fluid input : HydrotreatingRecipes.inputs()) {
            HydrotreatingRecipes.Outputs out = HydrotreatingRecipes.get(input);
            if (out == null) {
                continue;
            }
            List<FluidStack> ins = new ArrayList<>();
            ins.add(new FluidStack(input, HydrotreatingRecipes.INPUT_MB));
            if (ModFluids.HYDROGEN.source.isPresent()) {
                ins.add(new FluidStack(ModFluids.HYDROGEN.source.get(), out.hydrogenMb()));
            }
            rows.add(new FluidIoRow(
                    ins,
                    List.of(new FluidStack(out.left(), out.leftMb()), new FluidStack(out.right(), out.rightMb())),
                    List.of(),
                    null));
        }
        return rows;
    }

    static List<FluidIoRow> reformer() {
        List<FluidIoRow> rows = new ArrayList<>();
        for (Fluid input : ReformingRecipes.inputs()) {
            ReformingRecipes.Outputs out = ReformingRecipes.get(input);
            if (out == null) {
                continue;
            }
            rows.add(new FluidIoRow(
                    List.of(new FluidStack(input, ReformingRecipes.INPUT_MB)),
                    List.of(
                            new FluidStack(out.left(), out.leftMb()),
                            new FluidStack(out.mid(), out.midMb()),
                            new FluidStack(out.right(), out.rightMb())),
                    List.of(),
                    null));
        }
        return rows;
    }

    static List<FluidIoRow> vacuum() {
        List<FluidIoRow> rows = new ArrayList<>();
        for (Fluid input : VacuumRefineryRecipes.inputs()) {
            VacuumRefineryRecipes.Outputs out = VacuumRefineryRecipes.get(input);
            if (out == null) {
                continue;
            }
            rows.add(new FluidIoRow(
                    List.of(new FluidStack(input, VacuumRefineryRecipes.INPUT_MB)),
                    List.of(
                            new FluidStack(out.heavy(), out.heavyMb()),
                            new FluidStack(out.reformate(), out.reformateMb()),
                            new FluidStack(out.light(), out.lightMb()),
                            new FluidStack(out.gas(), out.gasMb())),
                    List.of(),
                    null));
        }
        return rows;
    }

    private static void addFluid(List<FluidStack> list, ModFluids.FluidEntry entry, int amount) {
        if (entry.source.isPresent() && amount > 0) {
            list.add(new FluidStack(entry.source.get(), amount));
        }
    }

    private static <T> RecipeType<T> type(String path, Class<? extends T> clazz) {
        return RecipeType.create(RefStrings.MODID, path, clazz);
    }
}
