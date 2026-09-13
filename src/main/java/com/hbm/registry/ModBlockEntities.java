package com.hbm.registry;

import com.hbm.blockentity.bomb.BombMultiBlockEntity;
import com.hbm.blockentity.bomb.ChargeBlockEntity;
import com.hbm.blockentity.bomb.CrashedBombBlockEntity;
import com.hbm.blockentity.bomb.VolcanoCoreBlockEntity;
import com.hbm.blockentity.bomb.FireworksBlockEntity;
import com.hbm.blockentity.bomb.LandmineBlockEntity;
import com.hbm.blockentity.bomb.NukeBoyBlockEntity;
import com.hbm.blockentity.bomb.NukeCustomBlockEntity;
import com.hbm.blockentity.bomb.NukeFleijaBlockEntity;
import com.hbm.blockentity.bomb.NukeFstbmbBlockEntity;
import com.hbm.blockentity.bomb.NukeGadgetBlockEntity;
import com.hbm.blockentity.bomb.NukeManBlockEntity;
import com.hbm.blockentity.bomb.NukeMikeBlockEntity;
import com.hbm.blockentity.bomb.NukeN2BlockEntity;
import com.hbm.blockentity.bomb.NukePrototypeBlockEntity;
import com.hbm.blockentity.bomb.NukeSoliniumBlockEntity;
import com.hbm.blockentity.bomb.NukeTsarBlockEntity;
import com.hbm.blockentity.machine.CableDiodeBlockEntity;
import com.hbm.blockentity.machine.CableSwitchBlockEntity;
import com.hbm.blockentity.machine.CombustionGeneratorBlockEntity;
import com.hbm.blockentity.machine.DieselGeneratorBlockEntity;
import com.hbm.blockentity.machine.ElectricFurnaceBlockEntity;
import com.hbm.blockentity.machine.FluidBarrelBlockEntity;
import com.hbm.blockentity.machine.InfiniteBatteryBlockEntity;
import com.hbm.blockentity.machine.CompactLauncherBlockEntity;
import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.blockentity.machine.LaunchPadLargeBlockEntity;
import com.hbm.blockentity.machine.LaunchPadProxyBlockEntity;
import com.hbm.blockentity.machine.LaunchPadRustedBlockEntity;
import com.hbm.blockentity.machine.LaunchTableBlockEntity;
import com.hbm.blockentity.machine.MachineBatteryBlockEntity;
import com.hbm.blockentity.machine.MachineSirenBlockEntity;
import com.hbm.blockentity.machine.GeigerBlockEntity;
import com.hbm.blockentity.machine.BroadcasterBlockEntity;
import com.hbm.blockentity.machine.RadioRecBlockEntity;
import com.hbm.blockentity.machine.MissileAssemblyBlockEntity;
import com.hbm.blockentity.machine.RadarLargeBlockEntity;
import com.hbm.blockentity.machine.RadarNTBlockEntity;
import com.hbm.blockentity.machine.RadarProxyBlockEntity;
import com.hbm.blockentity.machine.RadarScreenBlockEntity;
import com.hbm.blockentity.machine.RedCableBlockEntity;
import com.hbm.blockentity.machine.AssemblyMachineBlockEntity;
import com.hbm.blockentity.machine.ChemicalPlantBlockEntity;
import com.hbm.blockentity.machine.CondenserBlockEntity;
import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.FireboxBlockEntity;
import com.hbm.blockentity.machine.HeatBoilerBlockEntity;
import com.hbm.blockentity.machine.BlastFurnaceBlockEntity;
import com.hbm.blockentity.machine.DiFurnaceBlockEntity;
import com.hbm.blockentity.machine.DiFurnaceExtensionBlockEntity;
import com.hbm.blockentity.machine.DiFurnaceRtgBlockEntity;
import com.hbm.blockentity.machine.EPressBlockEntity;
import com.hbm.blockentity.machine.CentrifugeBlockEntity;
import com.hbm.blockentity.machine.GasCentBlockEntity;
import com.hbm.blockentity.machine.FelBlockEntity;
import com.hbm.blockentity.machine.SilexBlockEntity;
import com.hbm.blockentity.machine.CrystallizerBlockEntity;
import com.hbm.blockentity.machine.MixerBlockEntity;
import com.hbm.blockentity.machine.ArcWelderBlockEntity;
import com.hbm.blockentity.machine.PurexBlockEntity;
import com.hbm.blockentity.machine.SolderingStationBlockEntity;
import com.hbm.blockentity.machine.StructLauncherCoreBlockEntity;
import com.hbm.blockentity.machine.PressBlockEntity;
import com.hbm.blockentity.machine.RtgBlockEntity;
import com.hbm.blockentity.machine.ShredderBlockEntity;
import com.hbm.blockentity.machine.FileCabinetBlockEntity;
import com.hbm.blockentity.machine.SafeBlockEntity;
import com.hbm.blockentity.machine.StorageCrateBlockEntity;
import com.hbm.blockentity.machine.TurbineBlockEntity;
import com.hbm.blockentity.machine.WoodBurnerBlockEntity;
import com.hbm.blockentity.machine.OilWellBlockEntity;
import com.hbm.blockentity.machine.PumpjackBlockEntity;
import com.hbm.blockentity.machine.RefineryBlockEntity;
import com.hbm.blockentity.machine.FractionTowerBlockEntity;
import com.hbm.blockentity.machine.FractionSpacerBlockEntity;
import com.hbm.blockentity.machine.SiloHatchBlockEntity;
import com.hbm.blockentity.machine.CatalyticCrackerBlockEntity;
import com.hbm.blockentity.machine.HydrotreaterBlockEntity;
import com.hbm.blockentity.machine.CatalyticReformerBlockEntity;
import com.hbm.blockentity.machine.VacuumDistillBlockEntity;
import com.hbm.blockentity.network.CraneExtractorBlockEntity;
import com.hbm.blockentity.network.CraneInserterBlockEntity;
import com.hbm.blockentity.network.FluidCounterValveBlockEntity;
import com.hbm.blockentity.network.FluidPipeBlockEntity;
import com.hbm.blockentity.network.FluidValveBlockEntity;
import com.hbm.blockentity.network.PylonBlockEntity;
import com.hbm.blockentity.rbmk.RBMKPassiveBlockEntity;
import com.hbm.lib.RefStrings;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RefStrings.MODID);

    public static final RegistryObject<BlockEntityType<RBMKPassiveBlockEntity>> RBMK_PASSIVE =
            BLOCK_ENTITIES.register("rbmk_passive", () -> BlockEntityType.Builder.of(
                    RBMKPassiveBlockEntity::new,
                    ModBlocks.RBMK_BLANK.get(),
                    ModBlocks.RBMK_REFLECTOR.get(),
                    ModBlocks.RBMK_ABSORBER.get(),
                    ModBlocks.RBMK_MODERATOR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FluidBarrelBlockEntity>> FLUID_BARREL =
            BLOCK_ENTITIES.register("fluid_barrel", () -> BlockEntityType.Builder.of(
                    FluidBarrelBlockEntity::new,
                    ModBlocks.FLUID_BARREL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MachineBatteryBlockEntity>> MACHINE_BATTERY =
            BLOCK_ENTITIES.register("machine_battery", () -> BlockEntityType.Builder.of(
                    MachineBatteryBlockEntity::new,
                    ModBlocks.MACHINE_BATTERY.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<InfiniteBatteryBlockEntity>> MACHINE_BATTERY_INFINITE =
            BLOCK_ENTITIES.register("machine_battery_infinite", () -> BlockEntityType.Builder.of(
                    InfiniteBatteryBlockEntity::new,
                    ModBlocks.MACHINE_BATTERY_INFINITE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CombustionGeneratorBlockEntity>> COMBUSTION_GENERATOR =
            BLOCK_ENTITIES.register("combustion_generator", () -> BlockEntityType.Builder.of(
                    CombustionGeneratorBlockEntity::new,
                    ModBlocks.COMBUSTION_GENERATOR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<DieselGeneratorBlockEntity>> DIESEL_GENERATOR =
            BLOCK_ENTITIES.register("diesel_generator", () -> BlockEntityType.Builder.of(
                    DieselGeneratorBlockEntity::new,
                    ModBlocks.DIESEL_GENERATOR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE =
            BLOCK_ENTITIES.register("electric_furnace", () -> BlockEntityType.Builder.of(
                    ElectricFurnaceBlockEntity::new,
                    ModBlocks.ELECTRIC_FURNACE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<PressBlockEntity>> PRESS =
            BLOCK_ENTITIES.register("machine_press", () -> BlockEntityType.Builder.of(
                    PressBlockEntity::new,
                    ModBlocks.MACHINE_PRESS.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<EPressBlockEntity>> EPRESS =
            BLOCK_ENTITIES.register("machine_epress", () -> BlockEntityType.Builder.of(
                    EPressBlockEntity::new,
                    ModBlocks.MACHINE_EPRESS.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ShredderBlockEntity>> SHREDDER =
            BLOCK_ENTITIES.register("machine_shredder", () -> BlockEntityType.Builder.of(
                    ShredderBlockEntity::new,
                    ModBlocks.MACHINE_SHREDDER.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<CentrifugeBlockEntity>> MACHINE_CENTRIFUGE =
            BLOCK_ENTITIES.register("machine_centrifuge", () -> BlockEntityType.Builder.of(
                    CentrifugeBlockEntity::new,
                    ModBlocks.MACHINE_CENTRIFUGE.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<GasCentBlockEntity>> MACHINE_GASCENT =
            BLOCK_ENTITIES.register("machine_gascent", () -> BlockEntityType.Builder.of(
                    GasCentBlockEntity::new,
                    ModBlocks.MACHINE_GASCENT.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<FelBlockEntity>> MACHINE_FEL =
            BLOCK_ENTITIES.register("machine_fel", () -> BlockEntityType.Builder.of(
                    FelBlockEntity::new,
                    ModBlocks.MACHINE_FEL.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<SilexBlockEntity>> MACHINE_SILEX =
            BLOCK_ENTITIES.register("machine_silex", () -> BlockEntityType.Builder.of(
                    SilexBlockEntity::new,
                    ModBlocks.MACHINE_SILEX.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<CrystallizerBlockEntity>> MACHINE_CRYSTALLIZER =
            BLOCK_ENTITIES.register("machine_crystallizer", () -> BlockEntityType.Builder.of(
                    CrystallizerBlockEntity::new,
                    ModBlocks.MACHINE_CRYSTALLIZER.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<MixerBlockEntity>> MACHINE_MIXER =
            BLOCK_ENTITIES.register("machine_mixer", () -> BlockEntityType.Builder.of(
                    MixerBlockEntity::new,
                    ModBlocks.MACHINE_MIXER.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<ArcWelderBlockEntity>> MACHINE_ARC_WELDER =
            BLOCK_ENTITIES.register("machine_arc_welder", () -> BlockEntityType.Builder.of(
                    ArcWelderBlockEntity::new,
                    ModBlocks.MACHINE_ARC_WELDER.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<PurexBlockEntity>> MACHINE_PUREX =
            BLOCK_ENTITIES.register("machine_purex", () -> BlockEntityType.Builder.of(
                    PurexBlockEntity::new,
                    ModBlocks.MACHINE_PUREX.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<SolderingStationBlockEntity>> MACHINE_SOLDERING_STATION =
            BLOCK_ENTITIES.register("machine_soldering_station", () -> BlockEntityType.Builder.of(
                    SolderingStationBlockEntity::new,
                    ModBlocks.MACHINE_SOLDERING_STATION.get()
            ).build(null));
    public static final RegistryObject<BlockEntityType<RtgBlockEntity>> MACHINE_RTG =
            BLOCK_ENTITIES.register("machine_rtg", () -> BlockEntityType.Builder.of(
                    RtgBlockEntity::new,
                    ModBlocks.MACHINE_RTG.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<DiFurnaceBlockEntity>> DI_FURNACE =
            BLOCK_ENTITIES.register("machine_difurnace", () -> BlockEntityType.Builder.of(
                    DiFurnaceBlockEntity::new,
                    ModBlocks.MACHINE_DIFURNACE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<DiFurnaceExtensionBlockEntity>> DI_FURNACE_EXTENSION =
            BLOCK_ENTITIES.register("machine_difurnace_extension", () -> BlockEntityType.Builder.of(
                    DiFurnaceExtensionBlockEntity::new,
                    ModBlocks.MACHINE_DIFURNACE_EXTENSION.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<DiFurnaceRtgBlockEntity>> DI_FURNACE_RTG =
            BLOCK_ENTITIES.register("machine_difurnace_rtg_off", () -> BlockEntityType.Builder.of(
                    DiFurnaceRtgBlockEntity::new,
                    ModBlocks.MACHINE_DIFURNACE_RTG_OFF.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<BlastFurnaceBlockEntity>> BLAST_FURNACE =
            BLOCK_ENTITIES.register("machine_blast_furnace", () -> BlockEntityType.Builder.of(
                    BlastFurnaceBlockEntity::new,
                    ModBlocks.MACHINE_BLAST_FURNACE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<AssemblyMachineBlockEntity>> ASSEMBLY_MACHINE =
            BLOCK_ENTITIES.register("machine_assembly_machine", () -> BlockEntityType.Builder.of(
                    AssemblyMachineBlockEntity::new,
                    ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ChemicalPlantBlockEntity>> CHEMICAL_PLANT =
            BLOCK_ENTITIES.register("machine_chemical_plant", () -> BlockEntityType.Builder.of(
                    ChemicalPlantBlockEntity::new,
                    ModBlocks.MACHINE_CHEMICAL_PLANT.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FireboxBlockEntity>> HEATER_FIREBOX =
            BLOCK_ENTITIES.register("heater_firebox", () -> BlockEntityType.Builder.of(
                    FireboxBlockEntity::new,
                    ModBlocks.HEATER_FIREBOX.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<HeatBoilerBlockEntity>> MACHINE_BOILER =
            BLOCK_ENTITIES.register("machine_boiler", () -> BlockEntityType.Builder.of(
                    HeatBoilerBlockEntity::new,
                    ModBlocks.MACHINE_BOILER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<TurbineBlockEntity>> MACHINE_TURBINE =
            BLOCK_ENTITIES.register("machine_turbine", () -> BlockEntityType.Builder.of(
                    TurbineBlockEntity::new,
                    ModBlocks.MACHINE_TURBINE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CondenserBlockEntity>> MACHINE_CONDENSER =
            BLOCK_ENTITIES.register("machine_condenser", () -> BlockEntityType.Builder.of(
                    CondenserBlockEntity::new,
                    ModBlocks.MACHINE_CONDENSER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<WoodBurnerBlockEntity>> MACHINE_WOOD_BURNER =
            BLOCK_ENTITIES.register("machine_wood_burner", () -> BlockEntityType.Builder.of(
                    WoodBurnerBlockEntity::new,
                    ModBlocks.MACHINE_WOOD_BURNER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<OilWellBlockEntity>> MACHINE_WELL =
            BLOCK_ENTITIES.register("machine_well", () -> BlockEntityType.Builder.of(
                    OilWellBlockEntity::new,
                    ModBlocks.MACHINE_WELL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<PumpjackBlockEntity>> MACHINE_PUMPJACK =
            BLOCK_ENTITIES.register("machine_pumpjack", () -> BlockEntityType.Builder.of(
                    PumpjackBlockEntity::new,
                    ModBlocks.MACHINE_PUMPJACK.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<RefineryBlockEntity>> MACHINE_REFINERY =
            BLOCK_ENTITIES.register("machine_refinery", () -> BlockEntityType.Builder.of(
                    RefineryBlockEntity::new,
                    ModBlocks.MACHINE_REFINERY.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FractionTowerBlockEntity>> MACHINE_FRACTION_TOWER =
            BLOCK_ENTITIES.register("machine_fraction_tower", () -> BlockEntityType.Builder.of(
                    FractionTowerBlockEntity::new,
                    ModBlocks.MACHINE_FRACTION_TOWER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FractionSpacerBlockEntity>> FRACTION_SPACER =
            BLOCK_ENTITIES.register("fraction_spacer", () -> BlockEntityType.Builder.of(
                    FractionSpacerBlockEntity::new,
                    ModBlocks.FRACTION_SPACER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<SiloHatchBlockEntity>> SILO_HATCH =
            BLOCK_ENTITIES.register("silo_hatch", () -> BlockEntityType.Builder.of(
                    SiloHatchBlockEntity::new,
                    ModBlocks.SILO_HATCH.get(),
                    ModBlocks.SILO_HATCH_LARGE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CatalyticCrackerBlockEntity>> MACHINE_CATALYTIC_CRACKER =
            BLOCK_ENTITIES.register("machine_catalytic_cracker", () -> BlockEntityType.Builder.of(
                    CatalyticCrackerBlockEntity::new,
                    ModBlocks.MACHINE_CATALYTIC_CRACKER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<HydrotreaterBlockEntity>> MACHINE_HYDROTREATER =
            BLOCK_ENTITIES.register("machine_hydrotreater", () -> BlockEntityType.Builder.of(
                    HydrotreaterBlockEntity::new,
                    ModBlocks.MACHINE_HYDROTREATER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CatalyticReformerBlockEntity>> MACHINE_CATALYTIC_REFORMER =
            BLOCK_ENTITIES.register("machine_catalytic_reformer", () -> BlockEntityType.Builder.of(
                    CatalyticReformerBlockEntity::new,
                    ModBlocks.MACHINE_CATALYTIC_REFORMER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<VacuumDistillBlockEntity>> MACHINE_VACUUM_DISTILL =
            BLOCK_ENTITIES.register("machine_vacuum_distill", () -> BlockEntityType.Builder.of(
                    VacuumDistillBlockEntity::new,
                    ModBlocks.MACHINE_VACUUM_DISTILL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<DummyableProxyBlockEntity>> DUMMYABLE_PROXY =
            BLOCK_ENTITIES.register("dummyable_proxy", () -> BlockEntityType.Builder.of(
                    DummyableProxyBlockEntity::new,
                    ModBlocks.MACHINE_ASSEMBLY_MACHINE.get(),
                    ModBlocks.MACHINE_CRYSTALLIZER.get(),
                    ModBlocks.MACHINE_ARC_WELDER.get(),
                    ModBlocks.MACHINE_PUREX.get(),
                    ModBlocks.MACHINE_SOLDERING_STATION.get(),
                    ModBlocks.MACHINE_CHEMICAL_PLANT.get(),
                    ModBlocks.HEATER_FIREBOX.get(),
                    ModBlocks.MACHINE_BOILER.get(),
                    ModBlocks.MACHINE_WOOD_BURNER.get(),
                    ModBlocks.MACHINE_WELL.get(),
                    ModBlocks.MACHINE_PUMPJACK.get(),
                    ModBlocks.MACHINE_REFINERY.get(),
                    ModBlocks.MACHINE_FRACTION_TOWER.get(),
                    ModBlocks.FRACTION_SPACER.get(),
                    ModBlocks.MACHINE_CATALYTIC_CRACKER.get(),
                    ModBlocks.MACHINE_HYDROTREATER.get(),
                    ModBlocks.MACHINE_CATALYTIC_REFORMER.get(),
                    ModBlocks.MACHINE_VACUUM_DISTILL.get(),
                    ModBlocks.MACHINE_FEL.get(),
                    ModBlocks.MACHINE_SILEX.get(),
                    ModBlocks.SUBSTATION.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MachineSirenBlockEntity>> MACHINE_SIREN =
            BLOCK_ENTITIES.register("machine_siren", () -> BlockEntityType.Builder.of(
                    MachineSirenBlockEntity::new,
                    ModBlocks.MACHINE_SIREN.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<GeigerBlockEntity>> GEIGER =
            BLOCK_ENTITIES.register("geiger", () -> BlockEntityType.Builder.of(
                    GeigerBlockEntity::new,
                    ModBlocks.GEIGER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<BroadcasterBlockEntity>> BROADCASTER_PC =
            BLOCK_ENTITIES.register("broadcaster_pc", () -> BlockEntityType.Builder.of(
                    BroadcasterBlockEntity::new,
                    ModBlocks.BROADCASTER_PC.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<RadioRecBlockEntity>> RADIOREC =
            BLOCK_ENTITIES.register("radiorec", () -> BlockEntityType.Builder.of(
                    RadioRecBlockEntity::new,
                    ModBlocks.RADIOREC.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<RedCableBlockEntity>> RED_CABLE =
            BLOCK_ENTITIES.register("red_cable", () -> BlockEntityType.Builder.of(
                    RedCableBlockEntity::new,
                    ModBlocks.RED_CABLE.get(),
                    ModBlocks.RED_CABLE_CLASSIC.get(),
                    ModBlocks.RED_WIRE_COATED.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<PylonBlockEntity>> PYLON =
            BLOCK_ENTITIES.register("pylon", () -> BlockEntityType.Builder.of(
                    PylonBlockEntity::new,
                    ModBlocks.RED_CONNECTOR.get(),
                    ModBlocks.RED_PYLON.get(),
                    ModBlocks.RED_PYLON_MEDIUM_WOOD.get(),
                    ModBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER.get(),
                    ModBlocks.RED_PYLON_MEDIUM_STEEL.get(),
                    ModBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER.get(),
                    ModBlocks.RED_PYLON_LARGE.get(),
                    ModBlocks.SUBSTATION.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CableSwitchBlockEntity>> CABLE_SWITCH =
            BLOCK_ENTITIES.register("cable_switch", () -> BlockEntityType.Builder.of(
                    CableSwitchBlockEntity::new,
                    ModBlocks.CABLE_SWITCH.get(),
                    ModBlocks.CABLE_DETECTOR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CableDiodeBlockEntity>> CABLE_DIODE =
            BLOCK_ENTITIES.register("cable_diode", () -> BlockEntityType.Builder.of(
                    CableDiodeBlockEntity::new,
                    ModBlocks.CABLE_DIODE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_DUCT =
            BLOCK_ENTITIES.register("fluid_duct", () -> BlockEntityType.Builder.of(
                    FluidPipeBlockEntity::new,
                    ModBlocks.FLUID_DUCT_NEO.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FluidValveBlockEntity>> FLUID_VALVE =
            BLOCK_ENTITIES.register("fluid_valve", () -> BlockEntityType.Builder.of(
                    FluidValveBlockEntity::new,
                    ModBlocks.FLUID_VALVE.get(),
                    ModBlocks.FLUID_SWITCH.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FluidCounterValveBlockEntity>> FLUID_COUNTER_VALVE =
            BLOCK_ENTITIES.register("fluid_counter_valve", () -> BlockEntityType.Builder.of(
                    FluidCounterValveBlockEntity::new,
                    ModBlocks.FLUID_COUNTER_VALVE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CraneInserterBlockEntity>> CRANE_INSERTER =
            BLOCK_ENTITIES.register("crane_inserter", () -> BlockEntityType.Builder.of(
                    CraneInserterBlockEntity::new,
                    ModBlocks.CRANE_INSERTER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CraneExtractorBlockEntity>> CRANE_EXTRACTOR =
            BLOCK_ENTITIES.register("crane_extractor", () -> BlockEntityType.Builder.of(
                    CraneExtractorBlockEntity::new,
                    ModBlocks.CRANE_EXTRACTOR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<StorageCrateBlockEntity>> STORAGE_CRATE =
            BLOCK_ENTITIES.register("storage_crate", () -> BlockEntityType.Builder.of(
                    StorageCrateBlockEntity::new,
                    ModBlocks.CRATE_IRON.get(),
                    ModBlocks.CRATE_STEEL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FileCabinetBlockEntity>> FILE_CABINET =
            BLOCK_ENTITIES.register("file_cabinet", () -> BlockEntityType.Builder.of(
                    FileCabinetBlockEntity::new,
                    ModBlocks.FILING_CABINET.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<SafeBlockEntity>> SAFE =
            BLOCK_ENTITIES.register("safe", () -> BlockEntityType.Builder.of(
                    SafeBlockEntity::new,
                    ModBlocks.SAFE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeBoyBlockEntity>> NUKE_BOY =
            BLOCK_ENTITIES.register("nuke_boy", () -> BlockEntityType.Builder.of(
                    NukeBoyBlockEntity::new,
                    ModBlocks.NUKE_BOY.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeManBlockEntity>> NUKE_MAN =
            BLOCK_ENTITIES.register("nuke_man", () -> BlockEntityType.Builder.of(
                    NukeManBlockEntity::new,
                    ModBlocks.NUKE_MAN.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeGadgetBlockEntity>> NUKE_GADGET =
            BLOCK_ENTITIES.register("nuke_gadget", () -> BlockEntityType.Builder.of(
                    NukeGadgetBlockEntity::new,
                    ModBlocks.NUKE_GADGET.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeMikeBlockEntity>> NUKE_MIKE =
            BLOCK_ENTITIES.register("nuke_mike", () -> BlockEntityType.Builder.of(
                    NukeMikeBlockEntity::new,
                    ModBlocks.NUKE_MIKE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeTsarBlockEntity>> NUKE_TSAR =
            BLOCK_ENTITIES.register("nuke_tsar", () -> BlockEntityType.Builder.of(
                    NukeTsarBlockEntity::new,
                    ModBlocks.NUKE_TSAR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeFleijaBlockEntity>> NUKE_FLEIJA =
            BLOCK_ENTITIES.register("nuke_fleija", () -> BlockEntityType.Builder.of(
                    NukeFleijaBlockEntity::new,
                    ModBlocks.NUKE_FLEIJA.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeSoliniumBlockEntity>> NUKE_SOLINIUM =
            BLOCK_ENTITIES.register("nuke_solinium", () -> BlockEntityType.Builder.of(
                    NukeSoliniumBlockEntity::new,
                    ModBlocks.NUKE_SOLINIUM.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeFstbmbBlockEntity>> NUKE_FSTBMB =
            BLOCK_ENTITIES.register("nuke_fstbmb", () -> BlockEntityType.Builder.of(
                    NukeFstbmbBlockEntity::new,
                    ModBlocks.NUKE_FSTBMB.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeN2BlockEntity>> NUKE_N2 =
            BLOCK_ENTITIES.register("nuke_n2", () -> BlockEntityType.Builder.of(
                    NukeN2BlockEntity::new,
                    ModBlocks.NUKE_N2.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukePrototypeBlockEntity>> NUKE_PROTOTYPE =
            BLOCK_ENTITIES.register("nuke_prototype", () -> BlockEntityType.Builder.of(
                    NukePrototypeBlockEntity::new,
                    ModBlocks.NUKE_PROTOTYPE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<NukeCustomBlockEntity>> NUKE_CUSTOM =
            BLOCK_ENTITIES.register("nuke_custom", () -> BlockEntityType.Builder.of(
                    NukeCustomBlockEntity::new,
                    ModBlocks.NUKE_CUSTOM.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<BombMultiBlockEntity>> BOMB_MULTI =
            BLOCK_ENTITIES.register("bomb_multi", () -> BlockEntityType.Builder.of(
                    BombMultiBlockEntity::new,
                    ModBlocks.BOMB_MULTI.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LaunchPadBlockEntity>> LAUNCH_PAD =
            BLOCK_ENTITIES.register("launch_pad", () -> BlockEntityType.Builder.of(
                    LaunchPadBlockEntity::new,
                    ModBlocks.LAUNCH_PAD.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LaunchPadLargeBlockEntity>> LAUNCH_PAD_LARGE =
            BLOCK_ENTITIES.register("launch_pad_large", () -> BlockEntityType.Builder.of(
                    LaunchPadLargeBlockEntity::new,
                    ModBlocks.LAUNCH_PAD_LARGE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LaunchPadRustedBlockEntity>> LAUNCH_PAD_RUSTED =
            BLOCK_ENTITIES.register("launch_pad_rusted", () -> BlockEntityType.Builder.of(
                    LaunchPadRustedBlockEntity::new,
                    ModBlocks.LAUNCH_PAD_RUSTED.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LaunchPadProxyBlockEntity>> LAUNCH_PAD_PROXY =
            BLOCK_ENTITIES.register("launch_pad_proxy", () -> BlockEntityType.Builder.of(
                    LaunchPadProxyBlockEntity::new,
                    ModBlocks.LAUNCH_PAD.get(),
                    ModBlocks.LAUNCH_PAD_LARGE.get(),
                    ModBlocks.COMPACT_LAUNCHER.get(),
                    ModBlocks.LAUNCH_TABLE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CompactLauncherBlockEntity>> COMPACT_LAUNCHER =
            BLOCK_ENTITIES.register("compact_launcher", () -> BlockEntityType.Builder.of(
                    CompactLauncherBlockEntity::new,
                    ModBlocks.COMPACT_LAUNCHER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LaunchTableBlockEntity>> LAUNCH_TABLE =
            BLOCK_ENTITIES.register("launch_table", () -> BlockEntityType.Builder.of(
                    LaunchTableBlockEntity::new,
                    ModBlocks.LAUNCH_TABLE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<StructLauncherCoreBlockEntity>> STRUCT_LAUNCHER_CORE =
            BLOCK_ENTITIES.register("struct_launcher_core", () -> BlockEntityType.Builder.of(
                    StructLauncherCoreBlockEntity::new,
                    ModBlocks.STRUCT_LAUNCHER_CORE.get(),
                    ModBlocks.STRUCT_LAUNCHER_CORE_LARGE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<MissileAssemblyBlockEntity>> MISSILE_ASSEMBLY =
            BLOCK_ENTITIES.register("machine_missile_assembly", () -> BlockEntityType.Builder.of(
                    MissileAssemblyBlockEntity::new,
                    ModBlocks.MACHINE_MISSILE_ASSEMBLY.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<RadarNTBlockEntity>> RADAR =
            BLOCK_ENTITIES.register("machine_radar", () -> BlockEntityType.Builder.of(
                    RadarNTBlockEntity::new,
                    ModBlocks.MACHINE_RADAR.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<RadarLargeBlockEntity>> RADAR_LARGE =
            BLOCK_ENTITIES.register("machine_radar_large", () -> BlockEntityType.Builder.of(
                    RadarLargeBlockEntity::new,
                    ModBlocks.MACHINE_RADAR_LARGE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<RadarScreenBlockEntity>> RADAR_SCREEN =
            BLOCK_ENTITIES.register("radar_screen", () -> BlockEntityType.Builder.of(
                    RadarScreenBlockEntity::new,
                    ModBlocks.RADAR_SCREEN.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<RadarProxyBlockEntity>> RADAR_PROXY =
            BLOCK_ENTITIES.register("radar_proxy", () -> BlockEntityType.Builder.of(
                    RadarProxyBlockEntity::new,
                    ModBlocks.MACHINE_RADAR_LARGE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<CrashedBombBlockEntity>> CRASHED_BOMB =
            BLOCK_ENTITIES.register("crashed_bomb", () -> BlockEntityType.Builder.of(
                    CrashedBombBlockEntity::new,
                    ModBlocks.CRASHED_BOMB.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<LandmineBlockEntity>> LANDMINE =
            BLOCK_ENTITIES.register("landmine", () -> BlockEntityType.Builder.of(
                    LandmineBlockEntity::new,
                    ModBlocks.MINE_AP.get(),
                    ModBlocks.MINE_HE.get(),
                    ModBlocks.MINE_SHRAP.get(),
                    ModBlocks.MINE_FAT.get(),
                    ModBlocks.MINE_NAVAL.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ChargeBlockEntity>> CHARGE =
            BLOCK_ENTITIES.register("charge", () -> BlockEntityType.Builder.of(
                    ChargeBlockEntity::new,
                    ModBlocks.CHARGE_DYNAMITE.get(),
                    ModBlocks.CHARGE_C4.get(),
                    ModBlocks.CHARGE_SEMTEX.get(),
                    ModBlocks.CHARGE_MINER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<FireworksBlockEntity>> FIREWORKS =
            BLOCK_ENTITIES.register("fireworks", () -> BlockEntityType.Builder.of(
                    FireworksBlockEntity::new,
                    ModBlocks.FIREWORKS.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<VolcanoCoreBlockEntity>> VOLCANO_CORE =
            BLOCK_ENTITIES.register("volcano_core", () -> BlockEntityType.Builder.of(
                    VolcanoCoreBlockEntity::new,
                    ModBlocks.VOLCANO_CORE.get(),
                    ModBlocks.VOLCANO_RAD_CORE.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<com.hbm.blockentity.machine.DecoLootBlockEntity>> DECO_LOOT =
            BLOCK_ENTITIES.register("deco_loot", () -> BlockEntityType.Builder.of(
                    com.hbm.blockentity.machine.DecoLootBlockEntity::new,
                    ModBlocks.DECO_LOOT.get()
            ).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
