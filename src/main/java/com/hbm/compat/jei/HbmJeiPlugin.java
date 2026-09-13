package com.hbm.compat.jei;

import com.hbm.client.screen.AnvilScreen;
import com.hbm.client.screen.ArcWelderScreen;
import com.hbm.client.screen.AssemblyMachineScreen;
import com.hbm.client.screen.BlastFurnaceScreen;
import com.hbm.client.screen.CentrifugeScreen;
import com.hbm.client.screen.ChemicalPlantScreen;
import com.hbm.client.screen.CrystallizerScreen;
import com.hbm.client.screen.DiFurnaceRtgScreen;
import com.hbm.client.screen.DiFurnaceScreen;
import com.hbm.client.screen.EPressScreen;
import com.hbm.client.screen.GasCentScreen;
import com.hbm.client.screen.HydrotreaterScreen;
import com.hbm.client.screen.MixerScreen;
import com.hbm.client.screen.PressScreen;
import com.hbm.client.screen.PurexScreen;
import com.hbm.client.screen.CatalyticReformerScreen;
import com.hbm.client.screen.RefineryScreen;
import com.hbm.client.screen.ShredderScreen;
import com.hbm.client.screen.SilexScreen;
import com.hbm.client.screen.SolderingStationScreen;
import com.hbm.client.screen.VacuumDistillScreen;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.DiFurnaceRecipes;
import com.hbm.inventory.recipes.GenericMachineRecipe;
import com.hbm.inventory.recipes.IngredientRef;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.PUREXRecipes;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public final class HbmJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(RefStrings.MODID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                cat(gui, HbmJeiRecipes.PRESS, "jei.hbm.category.press", ModBlocks.MACHINE_PRESS, 166, 72,
                        HbmJeiPlugin::press, recipe -> List.of(
                                Component.translatable("jei.hbm.stamp", recipe.stamp().name()))),
                cat(gui, HbmJeiRecipes.SHREDDER, "jei.hbm.category.shredder", ModBlocks.MACHINE_SHREDDER, 166, 54,
                        HbmJeiPlugin::shredder, null),
                cat(gui, HbmJeiRecipes.CENTRIFUGE, "jei.hbm.category.centrifuge", ModBlocks.MACHINE_CENTRIFUGE, 166, 54,
                        HbmJeiPlugin::centrifuge, null),
                cat(gui, HbmJeiRecipes.GAS_CENTRIFUGE, "jei.hbm.category.gas_centrifuge", ModBlocks.MACHINE_GASCENT, 166, 72,
                        HbmJeiPlugin::gasCent, recipe -> List.of(
                                Component.translatable("jei.hbm.pseudo", recipe.type().name()))),
                cat(gui, HbmJeiRecipes.ANVIL, "jei.hbm.category.anvil", ModBlocks.ANVIL_IRON, 166, 90,
                        HbmJeiPlugin::anvil, recipe -> List.of(
                                Component.translatable("jei.hbm.tier",
                                        recipe.tierUpper < 0
                                                ? recipe.tierLower + "+"
                                                : recipe.tierLower + "-" + recipe.tierUpper))),
                cat(gui, HbmJeiRecipes.SMITHING, "jei.hbm.category.smithing", ModBlocks.ANVIL_IRON, 166, 54,
                        HbmJeiPlugin::smithing, recipe -> List.of(
                                Component.translatable("jei.hbm.tier", recipe.tier))),
                cat(gui, HbmJeiRecipes.DI_FURNACE, "jei.hbm.category.di_furnace", ModBlocks.MACHINE_DIFURNACE, 166, 54,
                        HbmJeiPlugin::diFurnace, null),
                cat(gui, HbmJeiRecipes.BLAST, "jei.hbm.category.blast_furnace", ModBlocks.MACHINE_BLAST_FURNACE, 166, 54,
                        HbmJeiPlugin::blast, recipe -> HbmJeiIngredients.powerTime(recipe.duration(), 0)),
                cat(gui, HbmJeiRecipes.ASSEMBLER, "jei.hbm.category.assembler", ModBlocks.MACHINE_ASSEMBLY_MACHINE, 166, 90,
                        HbmJeiPlugin::generic, HbmJeiPlugin::genericExtra),
                cat(gui, HbmJeiRecipes.CHEMPLANT, "jei.hbm.category.chemplant", ModBlocks.MACHINE_CHEMICAL_PLANT, 166, 90,
                        HbmJeiPlugin::generic, HbmJeiPlugin::genericExtra),
                cat(gui, HbmJeiRecipes.PUREX, "jei.hbm.category.purex", ModBlocks.MACHINE_PUREX, 166, 90,
                        HbmJeiPlugin::generic, HbmJeiPlugin::genericExtra),
                cat(gui, HbmJeiRecipes.CRYSTALLIZER, "jei.hbm.category.crystallizer", ModBlocks.MACHINE_CRYSTALLIZER, 166, 72,
                        HbmJeiPlugin::crystallizer, recipe -> HbmJeiIngredients.powerTime(recipe.duration(), 1000)),
                cat(gui, HbmJeiRecipes.MIXER, "jei.hbm.category.mixer", ModBlocks.MACHINE_MIXER, 166, 72,
                        HbmJeiPlugin::mixer, recipe -> HbmJeiIngredients.powerTime(recipe.duration(), 50)),
                cat(gui, HbmJeiRecipes.SOLDERING, "jei.hbm.category.soldering", ModBlocks.MACHINE_SOLDERING_STATION, 166, 72,
                        HbmJeiPlugin::soldering, recipe -> HbmJeiIngredients.powerTime(recipe.duration(), recipe.consumption())),
                cat(gui, HbmJeiRecipes.ARC_WELDER, "jei.hbm.category.arc_welder", ModBlocks.MACHINE_ARC_WELDER, 166, 72,
                        HbmJeiPlugin::arcWelder, recipe -> HbmJeiIngredients.powerTime(recipe.duration(), recipe.consumption())),
                cat(gui, HbmJeiRecipes.SILEX, "jei.hbm.category.silex", ModBlocks.MACHINE_SILEX, 166, 72,
                        HbmJeiPlugin::silex, recipe -> List.of(
                                Component.translatable("jei.hbm.laser", recipe.laser().name()))),
                cat(gui, HbmJeiRecipes.REFINERY, "jei.hbm.category.refinery", ModBlocks.MACHINE_REFINERY, 166, 72,
                        HbmJeiPlugin::fluidIo, HbmJeiPlugin::fluidNote),
                cat(gui, HbmJeiRecipes.FRACTION, "jei.hbm.category.fraction", ModBlocks.MACHINE_FRACTION_TOWER, 166, 54,
                        HbmJeiPlugin::fluidIo, null),
                cat(gui, HbmJeiRecipes.CRACKING, "jei.hbm.category.cracking", ModBlocks.MACHINE_CATALYTIC_CRACKER, 166, 54,
                        HbmJeiPlugin::fluidIo, null),
                cat(gui, HbmJeiRecipes.HYDROTREATER, "jei.hbm.category.hydrotreater", ModBlocks.MACHINE_HYDROTREATER, 166, 54,
                        HbmJeiPlugin::fluidIo, recipe -> HbmJeiIngredients.powerTime(2, 20_000)),
                cat(gui, HbmJeiRecipes.REFORMER, "jei.hbm.category.reformer", ModBlocks.MACHINE_CATALYTIC_REFORMER, 166, 54,
                        HbmJeiPlugin::fluidIo, recipe -> HbmJeiIngredients.powerTime(1, 20_000)),
                cat(gui, HbmJeiRecipes.VACUUM, "jei.hbm.category.vacuum", ModBlocks.MACHINE_VACUUM_DISTILL, 166, 54,
                        HbmJeiPlugin::fluidIo, recipe -> HbmJeiIngredients.powerTime(1, 10_000))
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        HbmJeiRecipes.ensureLoaded();
        registration.addRecipes(HbmJeiRecipes.PRESS, PressRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.SHREDDER, HbmJeiRecipes.shredder());
        registration.addRecipes(HbmJeiRecipes.CENTRIFUGE, CentrifugeRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.GAS_CENTRIFUGE, HbmJeiRecipes.gasCentrifuge());
        registration.addRecipes(HbmJeiRecipes.ANVIL, AnvilRecipes.getConstruction());
        registration.addRecipes(HbmJeiRecipes.SMITHING, AnvilRecipes.getSmithing());
        registration.addRecipes(HbmJeiRecipes.DI_FURNACE, DiFurnaceRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.BLAST, BlastFurnaceRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.ASSEMBLER, AssemblyMachineRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.CHEMPLANT, ChemicalPlantRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.PUREX, PUREXRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.CRYSTALLIZER, CrystallizerRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.MIXER, MixerRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.SOLDERING, SolderingRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.ARC_WELDER, ArcWelderRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.SILEX, SILEXRecipes.recipes());
        registration.addRecipes(HbmJeiRecipes.REFINERY, HbmJeiRecipes.refinery());
        registration.addRecipes(HbmJeiRecipes.FRACTION, HbmJeiRecipes.fraction());
        registration.addRecipes(HbmJeiRecipes.CRACKING, HbmJeiRecipes.cracking());
        registration.addRecipes(HbmJeiRecipes.HYDROTREATER, HbmJeiRecipes.hydrotreater());
        registration.addRecipes(HbmJeiRecipes.REFORMER, HbmJeiRecipes.reformer());
        registration.addRecipes(HbmJeiRecipes.VACUUM, HbmJeiRecipes.vacuum());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        add(registration, ModBlocks.MACHINE_PRESS, HbmJeiRecipes.PRESS);
        add(registration, ModBlocks.MACHINE_EPRESS, HbmJeiRecipes.PRESS);
        add(registration, ModBlocks.MACHINE_SHREDDER, HbmJeiRecipes.SHREDDER);
        add(registration, ModBlocks.MACHINE_CENTRIFUGE, HbmJeiRecipes.CENTRIFUGE);
        add(registration, ModBlocks.MACHINE_GASCENT, HbmJeiRecipes.GAS_CENTRIFUGE);
        add(registration, ModBlocks.MACHINE_DIFURNACE, HbmJeiRecipes.DI_FURNACE);
        add(registration, ModBlocks.MACHINE_DIFURNACE_RTG_OFF, HbmJeiRecipes.DI_FURNACE);
        add(registration, ModBlocks.MACHINE_BLAST_FURNACE, HbmJeiRecipes.BLAST);
        add(registration, ModBlocks.MACHINE_ASSEMBLY_MACHINE, HbmJeiRecipes.ASSEMBLER);
        add(registration, ModBlocks.MACHINE_CHEMICAL_PLANT, HbmJeiRecipes.CHEMPLANT);
        add(registration, ModBlocks.MACHINE_PUREX, HbmJeiRecipes.PUREX);
        add(registration, ModBlocks.MACHINE_CRYSTALLIZER, HbmJeiRecipes.CRYSTALLIZER);
        add(registration, ModBlocks.MACHINE_MIXER, HbmJeiRecipes.MIXER);
        add(registration, ModBlocks.MACHINE_SOLDERING_STATION, HbmJeiRecipes.SOLDERING);
        add(registration, ModBlocks.MACHINE_ARC_WELDER, HbmJeiRecipes.ARC_WELDER);
        add(registration, ModBlocks.MACHINE_SILEX, HbmJeiRecipes.SILEX);
        add(registration, ModBlocks.MACHINE_FEL, HbmJeiRecipes.SILEX);
        add(registration, ModBlocks.MACHINE_REFINERY, HbmJeiRecipes.REFINERY);
        add(registration, ModBlocks.MACHINE_FRACTION_TOWER, HbmJeiRecipes.FRACTION);
        add(registration, ModBlocks.MACHINE_CATALYTIC_CRACKER, HbmJeiRecipes.CRACKING);
        add(registration, ModBlocks.MACHINE_HYDROTREATER, HbmJeiRecipes.HYDROTREATER);
        add(registration, ModBlocks.MACHINE_CATALYTIC_REFORMER, HbmJeiRecipes.REFORMER);
        add(registration, ModBlocks.MACHINE_VACUUM_DISTILL, HbmJeiRecipes.VACUUM);
        for (RegistryObject<Block> anvil : List.of(
                ModBlocks.ANVIL_IRON, ModBlocks.ANVIL_LEAD, ModBlocks.ANVIL_STEEL, ModBlocks.ANVIL_DESH,
                ModBlocks.ANVIL_FERROURANIUM, ModBlocks.ANVIL_SATURNITE, ModBlocks.ANVIL_BISMUTH_BRONZE,
                ModBlocks.ANVIL_ARSENIC_BRONZE, ModBlocks.ANVIL_SCHRABIDATE, ModBlocks.ANVIL_DNT,
                ModBlocks.ANVIL_OSMIRIDIUM, ModBlocks.ANVIL_MURKY)) {
            add(registration, anvil, HbmJeiRecipes.ANVIL);
            add(registration, anvil, HbmJeiRecipes.SMITHING);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(PressScreen.class, 80, 35, 18, 16, HbmJeiRecipes.PRESS);
        registration.addRecipeClickArea(EPressScreen.class, 80, 35, 18, 16, HbmJeiRecipes.PRESS);
        registration.addRecipeClickArea(ShredderScreen.class, 79, 34, 18, 16, HbmJeiRecipes.SHREDDER);
        registration.addRecipeClickArea(CentrifugeScreen.class, 61, 16, 54, 54, HbmJeiRecipes.CENTRIFUGE);
        registration.addRecipeClickArea(GasCentScreen.class, 61, 16, 54, 54, HbmJeiRecipes.GAS_CENTRIFUGE);
        registration.addRecipeClickArea(AnvilScreen.class, 7, 71, 162, 18, HbmJeiRecipes.ANVIL, HbmJeiRecipes.SMITHING);
        registration.addRecipeClickArea(DiFurnaceScreen.class, 79, 34, 24, 17, HbmJeiRecipes.DI_FURNACE);
        registration.addRecipeClickArea(DiFurnaceRtgScreen.class, 101, 35, 24, 17, HbmJeiRecipes.DI_FURNACE);
        registration.addRecipeClickArea(BlastFurnaceScreen.class, 79, 34, 24, 17, HbmJeiRecipes.BLAST);
        registration.addRecipeClickArea(AssemblyMachineScreen.class, 79, 34, 24, 17, HbmJeiRecipes.ASSEMBLER);
        registration.addRecipeClickArea(ChemicalPlantScreen.class, 79, 34, 24, 17, HbmJeiRecipes.CHEMPLANT);
        registration.addRecipeClickArea(PurexScreen.class, 79, 34, 24, 17, HbmJeiRecipes.PUREX);
        registration.addRecipeClickArea(CrystallizerScreen.class, 79, 34, 24, 17, HbmJeiRecipes.CRYSTALLIZER);
        registration.addRecipeClickArea(MixerScreen.class, 79, 34, 24, 17, HbmJeiRecipes.MIXER);
        registration.addRecipeClickArea(SolderingStationScreen.class, 79, 34, 24, 17, HbmJeiRecipes.SOLDERING);
        registration.addRecipeClickArea(ArcWelderScreen.class, 79, 34, 24, 17, HbmJeiRecipes.ARC_WELDER);
        registration.addRecipeClickArea(SilexScreen.class, 79, 34, 24, 17, HbmJeiRecipes.SILEX);
        registration.addRecipeClickArea(RefineryScreen.class, 79, 34, 24, 17, HbmJeiRecipes.REFINERY);
        registration.addRecipeClickArea(HydrotreaterScreen.class, 79, 34, 24, 17, HbmJeiRecipes.HYDROTREATER);
        registration.addRecipeClickArea(CatalyticReformerScreen.class, 79, 34, 24, 17, HbmJeiRecipes.REFORMER);
        registration.addRecipeClickArea(VacuumDistillScreen.class, 79, 34, 24, 17, HbmJeiRecipes.VACUUM);
    }

    private static void press(IRecipeLayoutBuilder builder, PressRecipes.PressRecipe recipe) {
        HbmJeiIngredients.items(builder, RecipeIngredientRole.INPUT, 48, 6, HbmJeiIngredients.stamps(recipe.stamp()));
        HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 48, 42, recipe.input());
        HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 111, 24, recipe.output());
    }

    private static void shredder(IRecipeLayoutBuilder builder, HbmJeiRecipes.ShredderRow recipe) {
        HbmJeiIngredients.items(builder, RecipeIngredientRole.INPUT, 39, 18, recipe.input());
        HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 111, 18, recipe.output());
    }

    private static void centrifuge(IRecipeLayoutBuilder builder, CentrifugeRecipes.CentrifugeRecipe recipe) {
        HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 6, 18, recipe.input());
        List<IngredientRef> outs = recipe.output();
        for (int i = 0; i < outs.size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 70 + (i % 4) * 18, 9 + (i / 4) * 18, outs.get(i));
        }
    }

    private static void gasCent(IRecipeLayoutBuilder builder, HbmJeiRecipes.GasCentRow recipe) {
        if (recipe.conversion() != null) {
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.INPUT, 6, 18,
                    new FluidStack(recipe.conversion(), Math.max(1, recipe.type().fluidConsumed())));
        }
        List<IngredientRef> items = recipe.type().items();
        for (int i = 0; i < items.size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 70 + (i % 3) * 18, 9 + (i / 3) * 18, items.get(i));
        }
    }

    private static void anvil(IRecipeLayoutBuilder builder, AnvilRecipes.AnvilConstructionRecipe recipe) {
        for (int i = 0; i < recipe.input.size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 2 + (i % 6) * 18, 2 + (i / 6) * 18,
                    recipe.input.get(i));
        }
        for (int i = 0; i < recipe.output.size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 116 + (i % 2) * 18, 2 + (i / 2) * 18,
                    recipe.output.get(i));
        }
    }

    private static void smithing(IRecipeLayoutBuilder builder, AnvilRecipes.AnvilSmithingRecipe recipe) {
        HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 39, 18, recipe.left);
        HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 75, 18, recipe.right);
        HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 111, 18, recipe.output);
    }

    private static void diFurnace(IRecipeLayoutBuilder builder, DiFurnaceRecipes.DiFurnaceRecipe recipe) {
        HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 30, 18, recipe.inputA());
        HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 66, 18, recipe.inputB());
        HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 120, 18, recipe.output());
    }

    private static void blast(IRecipeLayoutBuilder builder, BlastFurnaceRecipes.BlastFurnaceRecipe recipe) {
        for (int i = 0; i < recipe.inputs().size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 12 + i * 18, 18, recipe.inputs().get(i));
        }
        List<IngredientRef> outs = recipe.presentOutputs();
        for (int i = 0; i < outs.size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 100 + i * 18, 18, outs.get(i));
        }
    }

    private static void generic(IRecipeLayoutBuilder builder, GenericMachineRecipe recipe) {
        for (int i = 0; i < recipe.inputItem().size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 2 + (i % 6) * 18, 2 + (i / 6) * 18,
                    recipe.inputItem().get(i));
        }
        for (int i = 0; i < recipe.inputFluid().size(); i++) {
            GenericMachineRecipe.FluidInput in = recipe.inputFluid().get(i);
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.INPUT, 2 + i * 18, 40, in.fluid(), in.amount());
        }
        for (int i = 0; i < recipe.outputItem().size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 116 + (i % 2) * 18, 2 + (i / 2) * 18,
                    recipe.outputItem().get(i));
        }
        for (int i = 0; i < recipe.outputFluid().size(); i++) {
            GenericMachineRecipe.FluidInput out = recipe.outputFluid().get(i);
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.OUTPUT, 116 + i * 18, 40, out.fluid(), out.amount());
        }
    }

    private static List<Component> genericExtra(GenericMachineRecipe recipe) {
        List<Component> lines = new ArrayList<>(HbmJeiIngredients.powerTime(recipe.duration(), recipe.power()));
        if (!recipe.pools().isEmpty()) {
            lines.add(Component.translatable("jei.hbm.pool", String.join(", ", recipe.pools())));
        }
        return lines;
    }

    private static void crystallizer(IRecipeLayoutBuilder builder, CrystallizerRecipes.CrystallizerRecipe recipe) {
        HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 12, 18, recipe.input());
        HbmJeiIngredients.fluid(builder, RecipeIngredientRole.INPUT, 36, 18, recipe.fluid().fluid(), recipe.fluid().amount());
        HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 120, 18, recipe.output());
    }

    private static void mixer(IRecipeLayoutBuilder builder, MixerRecipes.MixerRecipe recipe) {
        int x = 6;
        if (recipe.input1() != null) {
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.INPUT, x, 18, recipe.input1().fluid(), recipe.input1().amount());
            x += 22;
        }
        if (recipe.input2() != null) {
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.INPUT, x, 18, recipe.input2().fluid(), recipe.input2().amount());
            x += 22;
        }
        if (recipe.solid() != null) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, x, 18, recipe.solid());
        }
        HbmJeiIngredients.fluid(builder, RecipeIngredientRole.OUTPUT, 120, 18, recipe.output().fluid(), recipe.output().amount());
    }

    private static void soldering(IRecipeLayoutBuilder builder, SolderingRecipes.SolderingRecipe recipe) {
        int x = 2;
        for (IngredientRef in : recipe.toppings()) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, x, 2, in);
            x += 18;
        }
        x = 2;
        for (IngredientRef in : recipe.pcb()) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, x, 22, in);
            x += 18;
        }
        x = 2;
        for (IngredientRef in : recipe.solder()) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, x, 42, in);
            x += 18;
        }
        if (recipe.fluid() != null) {
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.INPUT, 92, 22, recipe.fluid().fluid(), recipe.fluid().amount());
        }
        HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 128, 22, recipe.output());
    }

    private static void arcWelder(IRecipeLayoutBuilder builder, ArcWelderRecipes.ArcWelderRecipe recipe) {
        for (int i = 0; i < recipe.inputs().size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 6 + i * 18, 18, recipe.inputs().get(i));
        }
        if (recipe.fluid() != null) {
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.INPUT, 70, 18, recipe.fluid().fluid(), recipe.fluid().amount());
        }
        HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 128, 18, recipe.output());
    }

    private static void silex(IRecipeLayoutBuilder builder, SILEXRecipes.SILEXRecipe recipe) {
        if (recipe.fluid() != null && !recipe.fluid().isEmpty()) {
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.INPUT, 6, 18, recipe.fluid(),
                    Math.max(recipe.fluidConsumed(), 1));
        } else {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.INPUT, 6, 18, recipe.input());
        }
        int total = recipe.totalWeight();
        for (int i = 0; i < recipe.outputs().size(); i++) {
            SILEXRecipes.WeightedOut out = recipe.outputs().get(i);
            int x = 70 + (i % 4) * 18;
            int y = 6 + (i / 4) * 18;
            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStacks(HbmJeiIngredients.stacks(out.item()));
            int pct = Math.round(100.0F * out.weight() / total);
            slot.addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.hbm.chance", pct)));
        }
    }

    private static void fluidIo(IRecipeLayoutBuilder builder, HbmJeiRecipes.FluidIoRow recipe) {
        for (int i = 0; i < recipe.inputs().size(); i++) {
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.INPUT, 6 + i * 22, 12, recipe.inputs().get(i));
        }
        for (int i = 0; i < recipe.outputs().size(); i++) {
            HbmJeiIngredients.fluid(builder, RecipeIngredientRole.OUTPUT, 80 + (i % 4) * 20, 12 + (i / 4) * 22,
                    recipe.outputs().get(i));
        }
        for (int i = 0; i < recipe.extras().size(); i++) {
            HbmJeiIngredients.item(builder, RecipeIngredientRole.OUTPUT, 80 + i * 18, 36, recipe.extras().get(i));
        }
    }

    private static List<Component> fluidNote(HbmJeiRecipes.FluidIoRow recipe) {
        if (recipe.note() == null || recipe.note().isEmpty()) {
            return List.of();
        }
        return List.of(Component.translatable(recipe.note()));
    }

    private static <T> HbmJeiCategory<T> cat(IGuiHelper gui, RecipeType<T> type, String title,
                                            RegistryObject<? extends Block> icon, int width, int height,
                                            java.util.function.BiConsumer<IRecipeLayoutBuilder, T> slots,
                                            java.util.function.Function<T, List<Component>> extra) {
        return new HbmJeiCategory<>(gui, type, title, new ItemStack(icon.get()), width, height, slots, extra);
    }

    private static void add(IRecipeCatalystRegistration registration, RegistryObject<? extends Block> block,
                            RecipeType<?> type) {
        registration.addRecipeCatalyst(new ItemStack(block.get()), type);
    }
}
