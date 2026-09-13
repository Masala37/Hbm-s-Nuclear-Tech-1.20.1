package com.hbm.inventory.recipes;

import com.hbm.HbmNuclearTechMod;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;

import java.io.IOException;

public final class MachineRecipeBootstrap {
    private static boolean loaded;

    private MachineRecipeBootstrap() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            AnvilRecipes.loadAndFilterMissing();
            PressRecipes.loadAndFilterMissing();
            ShredderRecipes.loadAndFilterMissing();
            CentrifugeRecipes.loadAndFilterMissing();
            SolderingRecipes.loadAndFilterMissing();
            CrystallizerRecipes.loadAndFilterMissing();
            MixerRecipes.loadAndFilterMissing();
            ArcWelderRecipes.loadAndFilterMissing();
            DiFurnaceRecipes.loadAndFilterMissing();
            BlastFurnaceRecipes.loadAndFilterMissing();
            AssemblyMachineRecipes.loadAndFilterMissing();
            ChemicalPlantRecipes.loadAndFilterMissing();
            PUREXRecipes.loadAndFilterMissing();
            GasCentrifugeRecipes.loadAndFilterMissing();
            SILEXRecipes.loadAndFilterMissing();
            HbmNuclearTechMod.LOGGER.info(
                    "Early machines: anvil smithing {} construction {} press {} shredder {} centrifuge {} soldering {} crystallizer {} mixer {} arc welder {} di-furnace {} blast {} assembler {} chemplant {} purex {} gascent {} silex {}",
                    AnvilRecipes.getSmithing().size(),
                    AnvilRecipes.getConstruction().size(),
                    PressRecipes.recipes().size(),
                    ShredderRecipes.recipes().size(),
                    CentrifugeRecipes.recipes().size(),
                    SolderingRecipes.recipes().size(),
                    CrystallizerRecipes.recipes().size(),
                    MixerRecipes.recipes().size(),
                    ArcWelderRecipes.recipes().size(),
                    DiFurnaceRecipes.recipes().size(),
                    BlastFurnaceRecipes.recipes().size(),
                    AssemblyMachineRecipes.recipes().size(),
                    ChemicalPlantRecipes.recipes().size(),
                    PUREXRecipes.recipes().size(),
                    GasCentrifugeRecipes.types().size(),
                    SILEXRecipes.recipes().size());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load 1.7.10 machine recipe dumps", e);
        }
    }
}
