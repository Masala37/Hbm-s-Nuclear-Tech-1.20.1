package com.hbm.registry;

import com.hbm.inventory.menu.BombMultiMenu;
import com.hbm.inventory.menu.CombustionGeneratorMenu;
import com.hbm.inventory.menu.DieselGeneratorMenu;
import com.hbm.inventory.menu.AssemblyMachineMenu;
import com.hbm.inventory.menu.ChemicalPlantMenu;
import com.hbm.inventory.menu.CraneExtractorMenu;
import com.hbm.inventory.menu.CraneInserterMenu;
import com.hbm.inventory.menu.FireboxMenu;
import com.hbm.inventory.menu.AnvilMenu;
import com.hbm.inventory.menu.BlastFurnaceMenu;
import com.hbm.inventory.menu.DiFurnaceMenu;
import com.hbm.inventory.menu.DiFurnaceRtgMenu;
import com.hbm.inventory.menu.ElectricFurnaceMenu;
import com.hbm.inventory.menu.EPressMenu;
import com.hbm.inventory.menu.PressMenu;
import com.hbm.inventory.menu.RtgMenu;
import com.hbm.inventory.menu.CentrifugeMenu;
import com.hbm.inventory.menu.GasCentMenu;
import com.hbm.inventory.menu.FelMenu;
import com.hbm.inventory.menu.SilexMenu;
import com.hbm.inventory.menu.CrystallizerMenu;
import com.hbm.inventory.menu.MixerMenu;
import com.hbm.inventory.menu.ArcWelderMenu;
import com.hbm.inventory.menu.PurexMenu;
import com.hbm.inventory.menu.SolderingStationMenu;
import com.hbm.inventory.menu.ShredderMenu;
import com.hbm.inventory.menu.FluidBarrelMenu;
import com.hbm.inventory.menu.LaunchPadMenu;
import com.hbm.inventory.menu.LaunchPadRustedMenu;
import com.hbm.inventory.menu.CompactLauncherMenu;
import com.hbm.inventory.menu.LaunchTableMenu;
import com.hbm.inventory.menu.MachineBatteryMenu;
import com.hbm.inventory.menu.MachineSirenMenu;
import com.hbm.inventory.menu.MissileAssemblyMenu;
import com.hbm.inventory.menu.RadarNTMenu;
import com.hbm.inventory.menu.NukeBoyMenu;
import com.hbm.inventory.menu.NukeBoyMenu;
import com.hbm.inventory.menu.NukeCustomMenu;
import com.hbm.inventory.menu.NukeFleijaMenu;
import com.hbm.inventory.menu.NukeFstbmbMenu;
import com.hbm.inventory.menu.NukeGadgetMenu;
import com.hbm.inventory.menu.NukeManMenu;
import com.hbm.inventory.menu.NukeMikeMenu;
import com.hbm.inventory.menu.NukeN2Menu;
import com.hbm.inventory.menu.NukePrototypeMenu;
import com.hbm.inventory.menu.NukeSoliniumMenu;
import com.hbm.inventory.menu.NukeTsarMenu;
import com.hbm.inventory.menu.FileCabinetMenu;
import com.hbm.inventory.menu.SafeMenu;
import com.hbm.inventory.menu.StorageCrateMenu;
import com.hbm.inventory.menu.TurbineMenu;
import com.hbm.inventory.menu.WoodBurnerMenu;
import com.hbm.inventory.menu.OilWellMenu;
import com.hbm.inventory.menu.PumpjackMenu;
import com.hbm.inventory.menu.RefineryMenu;
import com.hbm.inventory.menu.HydrotreaterMenu;
import com.hbm.inventory.menu.CatalyticReformerMenu;
import com.hbm.inventory.menu.VacuumDistillMenu;
import com.hbm.lib.RefStrings;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RefStrings.MODID);

    public static final RegistryObject<MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE =
            MENUS.register("electric_furnace", () -> IForgeMenuType.create(ElectricFurnaceMenu::new));

    public static final RegistryObject<MenuType<AnvilMenu>> ANVIL =
            MENUS.register("anvil", () -> IForgeMenuType.create(AnvilMenu::new));

    public static final RegistryObject<MenuType<PressMenu>> PRESS =
            MENUS.register("press", () -> IForgeMenuType.create(PressMenu::new));

    public static final RegistryObject<MenuType<EPressMenu>> EPRESS =
            MENUS.register("epress", () -> IForgeMenuType.create(EPressMenu::new));

    public static final RegistryObject<MenuType<ShredderMenu>> SHREDDER =
            MENUS.register("shredder", () -> IForgeMenuType.create(ShredderMenu::new));
    public static final RegistryObject<MenuType<CentrifugeMenu>> MACHINE_CENTRIFUGE =
            MENUS.register("machine_centrifuge", () -> IForgeMenuType.create(CentrifugeMenu::new));
    public static final RegistryObject<MenuType<GasCentMenu>> MACHINE_GASCENT =
            MENUS.register("machine_gascent", () -> IForgeMenuType.create(GasCentMenu::new));
    public static final RegistryObject<MenuType<FelMenu>> MACHINE_FEL =
            MENUS.register("machine_fel", () -> IForgeMenuType.create(FelMenu::new));
    public static final RegistryObject<MenuType<SilexMenu>> MACHINE_SILEX =
            MENUS.register("machine_silex", () -> IForgeMenuType.create(SilexMenu::new));
    public static final RegistryObject<MenuType<CrystallizerMenu>> MACHINE_CRYSTALLIZER =
            MENUS.register("machine_crystallizer", () -> IForgeMenuType.create(CrystallizerMenu::new));
    public static final RegistryObject<MenuType<MixerMenu>> MACHINE_MIXER =
            MENUS.register("machine_mixer", () -> IForgeMenuType.create(MixerMenu::new));
    public static final RegistryObject<MenuType<ArcWelderMenu>> MACHINE_ARC_WELDER =
            MENUS.register("machine_arc_welder", () -> IForgeMenuType.create(ArcWelderMenu::new));
    public static final RegistryObject<MenuType<PurexMenu>> MACHINE_PUREX =
            MENUS.register("machine_purex", () -> IForgeMenuType.create(PurexMenu::new));
    public static final RegistryObject<MenuType<SolderingStationMenu>> MACHINE_SOLDERING_STATION =
            MENUS.register("machine_soldering_station", () -> IForgeMenuType.create(SolderingStationMenu::new));
    public static final RegistryObject<MenuType<RtgMenu>> MACHINE_RTG =
            MENUS.register("machine_rtg", () -> IForgeMenuType.create(RtgMenu::new));

    public static final RegistryObject<MenuType<DiFurnaceMenu>> DI_FURNACE =
            MENUS.register("di_furnace", () -> IForgeMenuType.create(DiFurnaceMenu::new));

    public static final RegistryObject<MenuType<DiFurnaceRtgMenu>> DI_FURNACE_RTG =
            MENUS.register("di_furnace_rtg", () -> IForgeMenuType.create(DiFurnaceRtgMenu::new));

    public static final RegistryObject<MenuType<BlastFurnaceMenu>> BLAST_FURNACE =
            MENUS.register("blast_furnace", () -> IForgeMenuType.create(BlastFurnaceMenu::new));

    public static final RegistryObject<MenuType<AssemblyMachineMenu>> ASSEMBLY_MACHINE =
            MENUS.register("assembly_machine", () -> IForgeMenuType.create(AssemblyMachineMenu::new));

    public static final RegistryObject<MenuType<ChemicalPlantMenu>> CHEMICAL_PLANT =
            MENUS.register("chemical_plant", () -> IForgeMenuType.create(ChemicalPlantMenu::new));

    public static final RegistryObject<MenuType<FireboxMenu>> HEATER_FIREBOX =
            MENUS.register("heater_firebox", () -> IForgeMenuType.create(FireboxMenu::new));

    public static final RegistryObject<MenuType<TurbineMenu>> MACHINE_TURBINE =
            MENUS.register("machine_turbine", () -> IForgeMenuType.create(TurbineMenu::new));

    public static final RegistryObject<MenuType<WoodBurnerMenu>> MACHINE_WOOD_BURNER =
            MENUS.register("machine_wood_burner", () -> IForgeMenuType.create(WoodBurnerMenu::new));

    public static final RegistryObject<MenuType<OilWellMenu>> MACHINE_WELL =
            MENUS.register("machine_well", () -> IForgeMenuType.create(OilWellMenu::new));

    public static final RegistryObject<MenuType<PumpjackMenu>> MACHINE_PUMPJACK =
            MENUS.register("machine_pumpjack", () -> IForgeMenuType.create(PumpjackMenu::new));

    public static final RegistryObject<MenuType<RefineryMenu>> MACHINE_REFINERY =
            MENUS.register("machine_refinery", () -> IForgeMenuType.create(RefineryMenu::new));

    public static final RegistryObject<MenuType<HydrotreaterMenu>> MACHINE_HYDROTREATER =
            MENUS.register("machine_hydrotreater", () -> IForgeMenuType.create(HydrotreaterMenu::new));

    public static final RegistryObject<MenuType<CatalyticReformerMenu>> MACHINE_CATALYTIC_REFORMER =
            MENUS.register("machine_catalytic_reformer", () -> IForgeMenuType.create(CatalyticReformerMenu::new));

    public static final RegistryObject<MenuType<VacuumDistillMenu>> MACHINE_VACUUM_DISTILL =
            MENUS.register("machine_vacuum_distill", () -> IForgeMenuType.create(VacuumDistillMenu::new));

    public static final RegistryObject<MenuType<MachineSirenMenu>> MACHINE_SIREN =
            MENUS.register("machine_siren", () -> IForgeMenuType.create(MachineSirenMenu::new));

    public static final RegistryObject<MenuType<CraneInserterMenu>> CRANE_INSERTER =
            MENUS.register("crane_inserter", () -> IForgeMenuType.create(CraneInserterMenu::new));

    public static final RegistryObject<MenuType<CraneExtractorMenu>> CRANE_EXTRACTOR =
            MENUS.register("crane_extractor", () -> IForgeMenuType.create(CraneExtractorMenu::new));

    public static final RegistryObject<MenuType<MachineBatteryMenu>> MACHINE_BATTERY =
            MENUS.register("machine_battery", () -> IForgeMenuType.create(MachineBatteryMenu::new));

    public static final RegistryObject<MenuType<FluidBarrelMenu>> FLUID_BARREL =
            MENUS.register("fluid_barrel", () -> IForgeMenuType.create(FluidBarrelMenu::new));

    public static final RegistryObject<MenuType<DieselGeneratorMenu>> DIESEL_GENERATOR =
            MENUS.register("diesel_generator", () -> IForgeMenuType.create(DieselGeneratorMenu::new));

    public static final RegistryObject<MenuType<CombustionGeneratorMenu>> COMBUSTION_GENERATOR =
            MENUS.register("combustion_generator", () -> IForgeMenuType.create(CombustionGeneratorMenu::new));

    public static final RegistryObject<MenuType<MissileAssemblyMenu>> MISSILE_ASSEMBLY =
            MENUS.register("machine_missile_assembly", () -> IForgeMenuType.create(MissileAssemblyMenu::new));

    public static final RegistryObject<MenuType<LaunchPadMenu>> LAUNCH_PAD =
            MENUS.register("launch_pad", () -> IForgeMenuType.create(LaunchPadMenu::new));

    public static final RegistryObject<MenuType<LaunchPadRustedMenu>> LAUNCH_PAD_RUSTED =
            MENUS.register("launch_pad_rusted", () -> IForgeMenuType.create(LaunchPadRustedMenu::new));

    public static final RegistryObject<MenuType<CompactLauncherMenu>> COMPACT_LAUNCHER =
            MENUS.register("compact_launcher", () -> IForgeMenuType.create(CompactLauncherMenu::new));

    public static final RegistryObject<MenuType<LaunchTableMenu>> LAUNCH_TABLE =
            MENUS.register("launch_table", () -> IForgeMenuType.create(LaunchTableMenu::new));

    public static final RegistryObject<MenuType<RadarNTMenu>> RADAR_NT =
            MENUS.register("machine_radar", () -> IForgeMenuType.create(RadarNTMenu::new));

    public static final RegistryObject<MenuType<NukeBoyMenu>> NUKE_BOY =
            MENUS.register("nuke_boy", () -> IForgeMenuType.create(NukeBoyMenu::new));

    public static final RegistryObject<MenuType<NukeManMenu>> NUKE_MAN =
            MENUS.register("nuke_man", () -> IForgeMenuType.create(NukeManMenu::new));

    public static final RegistryObject<MenuType<NukeGadgetMenu>> NUKE_GADGET =
            MENUS.register("nuke_gadget", () -> IForgeMenuType.create(NukeGadgetMenu::new));

    public static final RegistryObject<MenuType<NukeMikeMenu>> NUKE_MIKE =
            MENUS.register("nuke_mike", () -> IForgeMenuType.create(NukeMikeMenu::new));

    public static final RegistryObject<MenuType<NukeTsarMenu>> NUKE_TSAR =
            MENUS.register("nuke_tsar", () -> IForgeMenuType.create(NukeTsarMenu::new));

    public static final RegistryObject<MenuType<NukeFleijaMenu>> NUKE_FLEIJA =
            MENUS.register("nuke_fleija", () -> IForgeMenuType.create(NukeFleijaMenu::new));

    public static final RegistryObject<MenuType<NukeSoliniumMenu>> NUKE_SOLINIUM =
            MENUS.register("nuke_solinium", () -> IForgeMenuType.create(NukeSoliniumMenu::new));

    public static final RegistryObject<MenuType<NukeFstbmbMenu>> NUKE_FSTBMB =
            MENUS.register("nuke_fstbmb", () -> IForgeMenuType.create(NukeFstbmbMenu::new));

    public static final RegistryObject<MenuType<NukeN2Menu>> NUKE_N2 =
            MENUS.register("nuke_n2", () -> IForgeMenuType.create(NukeN2Menu::new));

    public static final RegistryObject<MenuType<NukePrototypeMenu>> NUKE_PROTOTYPE =
            MENUS.register("nuke_prototype", () -> IForgeMenuType.create(NukePrototypeMenu::new));

    public static final RegistryObject<MenuType<NukeCustomMenu>> NUKE_CUSTOM =
            MENUS.register("nuke_custom", () -> IForgeMenuType.create(NukeCustomMenu::new));

    public static final RegistryObject<MenuType<BombMultiMenu>> BOMB_MULTI =
            MENUS.register("bomb_multi", () -> IForgeMenuType.create(BombMultiMenu::new));

    public static final RegistryObject<MenuType<StorageCrateMenu>> STORAGE_CRATE =
            MENUS.register("storage_crate", () -> IForgeMenuType.create(StorageCrateMenu::new));

    public static final RegistryObject<MenuType<StorageCrateMenu>> STORAGE_CRATE_LARGE =
            MENUS.register("storage_crate_large", () -> IForgeMenuType.create(StorageCrateMenu::new));

    public static final RegistryObject<MenuType<FileCabinetMenu>> FILE_CABINET =
            MENUS.register("file_cabinet", () -> IForgeMenuType.create(FileCabinetMenu::new));

    public static final RegistryObject<MenuType<SafeMenu>> SAFE =
            MENUS.register("safe", () -> IForgeMenuType.create(SafeMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
