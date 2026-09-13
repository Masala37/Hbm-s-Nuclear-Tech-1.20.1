package com.hbm.registry;

import com.hbm.blocks.HazardBlock;
import com.hbm.blocks.OutgasOreBlock;
import com.hbm.blocks.bomb.AssembledNukeBlock;
import com.hbm.blocks.bomb.BalefireBlock;
import com.hbm.blocks.bomb.BombBlock;
import com.hbm.blocks.bomb.BombMultiBlock;
import com.hbm.blocks.bomb.ChargeBlock;
import com.hbm.blocks.bomb.CrashedBombBlock;
import com.hbm.blocks.bomb.DetCordBlock;
import com.hbm.blocks.bomb.DetExplosiveBlock;
import com.hbm.blocks.bomb.ExplosiveBarrelBlock;
import com.hbm.blocks.bomb.FacingBombBlock;
import com.hbm.blocks.bomb.FireworksBlock;
import com.hbm.blocks.bomb.FissureBombBlock;
import com.hbm.blocks.bomb.LandmineBlock;
import com.hbm.blocks.bomb.SpecialtyBombBlock;
import com.hbm.blocks.bomb.NukeBoyBlock;
import com.hbm.blocks.bomb.NukeCustomBlock;
import com.hbm.blocks.bomb.NukeFleijaBlock;
import com.hbm.blocks.bomb.NukeFstbmbBlock;
import com.hbm.blocks.bomb.NukeGadgetBlock;
import com.hbm.blocks.bomb.NukeManBlock;
import com.hbm.blocks.bomb.NukeMikeBlock;
import com.hbm.blocks.bomb.NukeN2Block;
import com.hbm.blocks.bomb.NukePrototypeBlock;
import com.hbm.blocks.bomb.NukeSoliniumBlock;
import com.hbm.blocks.bomb.NukeTsarBlock;
import com.hbm.blocks.bomb.VolcanoBlock;
import com.hbm.blocks.bomb.VolcanicLavaBlock;
import com.hbm.blocks.generic.BarbedWireBlock;
import com.hbm.blocks.generic.ChainlinkFenceBlock;
import com.hbm.blocks.generic.ColoredConcreteBlock;
import com.hbm.blocks.generic.ConcreteExtBlock;
import com.hbm.blocks.generic.DeadPlantBlock;
import com.hbm.blocks.generic.PlantFlowerBlock;
import com.hbm.blocks.generic.SiloHatchBlock;
import com.hbm.blocks.generic.DecoObjBlock;
import com.hbm.blocks.generic.DecoPipeBlock;
import com.hbm.blocks.generic.DecorativeBarrelBlock;
import com.hbm.blocks.generic.FileCabinetBlock;
import com.hbm.blocks.generic.LightstoneBlock;
import com.hbm.blocks.generic.NtmDoorBlock;
import com.hbm.blocks.generic.SafeBlock;
import com.hbm.blocks.generic.SteelGrateBlock;
import com.hbm.blocks.generic.SteelTrapDoorBlock;
import com.hbm.blocks.generic.StructureMultiSlabBlock;
import com.hbm.blocks.generic.ThinLayerBlock;
import com.hbm.blocks.generic.WoodStructureBlock;
import com.hbm.blocks.generic.FalloutBlock;
import com.hbm.blocks.generic.OreVolcanoBlock;
import com.hbm.blocks.generic.SellafieldBedrockBlock;
import com.hbm.blocks.generic.SellafieldSlakedBlock;
import com.hbm.blocks.generic.SellafieldWasteBlock;
import com.hbm.blocks.generic.TaintBlock;
import com.hbm.blocks.generic.SpikesBlock;
import com.hbm.blocks.generic.SteelBeamBlock;
import com.hbm.blocks.generic.SteelRoofBlock;
import com.hbm.blocks.generic.SteelScaffoldBlock;
import com.hbm.blocks.generic.SteelWallBlock;
import com.hbm.blocks.generic.WasteEarthBlock;
import com.hbm.blocks.generic.WasteLogBlock;
import com.hbm.blocks.generic.WasteMyceliumBlock;
import com.hbm.blocks.generic.DecoLootBlock;
import com.hbm.blocks.generic.LootCrateBlock;
import com.hbm.blocks.generic.MeteorMoltenBlock;
import com.hbm.blocks.generic.MeteorSpawnerBlock;
import com.hbm.blocks.generic.MeteorTreasureBlock;
import com.hbm.blocks.generic.StructureWandBlock;
import com.hbm.blocks.gas.MonoxideGasBlock;
import com.hbm.blocks.gas.RadonGasBlock;
import com.hbm.blocks.machine.CableDetectorBlock;
import com.hbm.blocks.machine.CableDiodeBlock;
import com.hbm.blocks.machine.CableSwitchBlock;
import com.hbm.blocks.machine.CombustionGeneratorBlock;
import com.hbm.blocks.machine.DieselGeneratorBlock;
import com.hbm.blocks.machine.ElectricFurnaceBlock;
import com.hbm.blocks.machine.FluidBarrelBlock;
import com.hbm.blocks.machine.CompactLauncherBlock;
import com.hbm.blocks.machine.LaunchPadBlock;
import com.hbm.blocks.machine.LaunchPadLargeBlock;
import com.hbm.blocks.machine.LaunchPadRustedBlock;
import com.hbm.blocks.machine.LaunchTableBlock;
import com.hbm.blocks.machine.MissileAssemblyBlock;
import com.hbm.blocks.machine.RadarLargeBlock;
import com.hbm.blocks.machine.RadarNTBlock;
import com.hbm.blocks.machine.RadarScreenBlock;
import com.hbm.blocks.machine.StructLauncherCoreBlock;
import com.hbm.blocks.machine.StructPartBlock;
import com.hbm.blocks.machine.GeigerCounterBlock;
import com.hbm.blocks.machine.HeaterFireboxBlock;
import com.hbm.blocks.machine.MachineChemicalPlantBlock;
import com.hbm.blocks.machine.MachineAssemblyMachineBlock;
import com.hbm.blocks.machine.MachineBlastFurnaceBlock;
import com.hbm.blocks.machine.MachineCondenserBlock;
import com.hbm.blocks.machine.MachineDiFurnaceBlock;
import com.hbm.blocks.machine.MachineDiFurnaceExtensionBlock;
import com.hbm.blocks.machine.MachineDiFurnaceRtgBlock;
import com.hbm.blocks.machine.MachineEPressBlock;
import com.hbm.blocks.machine.MachineHeatBoilerBlock;
import com.hbm.blocks.machine.MachineWoodBurnerBlock;
import com.hbm.blocks.machine.MachineOilWellBlock;
import com.hbm.blocks.machine.MachinePumpjackBlock;
import com.hbm.blocks.machine.MachineRefineryBlock;
import com.hbm.blocks.machine.MachineFractionTowerBlock;
import com.hbm.blocks.machine.FractionSpacerBlock;
import com.hbm.blocks.machine.MachineCatalyticCrackerBlock;
import com.hbm.blocks.machine.MachineCatalyticReformerBlock;
import com.hbm.blocks.machine.MachineHydrotreaterBlock;
import com.hbm.blocks.machine.MachineVacuumDistillBlock;
import com.hbm.blocks.machine.OilPipeBlock;
import com.hbm.blocks.machine.MachineCentrifugeBlock;
import com.hbm.blocks.machine.MachineGasCentBlock;
import com.hbm.blocks.machine.MachineFelBlock;
import com.hbm.blocks.machine.MachineSilexBlock;
import com.hbm.blocks.machine.MachineCrystallizerBlock;
import com.hbm.blocks.machine.MachineMixerBlock;
import com.hbm.blocks.machine.MachineArcWelderBlock;
import com.hbm.blocks.machine.MachinePurexBlock;
import com.hbm.blocks.machine.MachineSolderingStationBlock;
import com.hbm.blocks.machine.MachinePressBlock;
import com.hbm.blocks.machine.MachineRtgBlock;
import com.hbm.blocks.machine.MachineShredderBlock;
import com.hbm.blocks.machine.MachineTurbineBlock;
import com.hbm.blocks.machine.NtmAnvilBlock;
import com.hbm.blocks.machine.InfiniteBatteryBlock;
import com.hbm.blocks.machine.MachineBatteryBlock;
import com.hbm.blocks.machine.MachineSirenBlock;
import com.hbm.blocks.machine.PinkCloudBroadcasterBlock;
import com.hbm.blocks.machine.RadioRecBlock;
import com.hbm.blocks.machine.RedCableBlock;
import com.hbm.blocks.machine.StorageCrateBlock;
import com.hbm.blocks.network.ConveyorBlock;
import com.hbm.blocks.network.CraneExtractorBlock;
import com.hbm.blocks.network.CraneInserterBlock;
import com.hbm.blocks.network.FluidCounterValveBlock;
import com.hbm.blocks.network.FluidDuctStandardBlock;
import com.hbm.blocks.network.FluidSwitchBlock;
import com.hbm.blocks.network.FluidValveBlock;
import com.hbm.blocks.network.PylonDummyableBlock;
import com.hbm.blocks.network.RedConnectorBlock;
import com.hbm.blocks.network.RedPylonBlock;
import com.hbm.blockentity.network.PylonKind;
import com.hbm.blocks.rbmk.RBMKDecoBlock;
import com.hbm.blocks.rbmk.RBMKPassiveBlock;
import com.hbm.lib.RefStrings;
import com.hbm.rbmk.RBMKColumnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RefStrings.MODID);

    public static final RegistryObject<Block> ORE_URANIUM = registerOre("ore_uranium");
    public static final RegistryObject<Block> ORE_TITANIUM = registerOre("ore_titanium");
    public static final RegistryObject<Block> ORE_TUNGSTEN = registerOre("ore_tungsten");
    public static final RegistryObject<Block> ORE_ALUMINIUM = registerOre("ore_aluminium");
    public static final RegistryObject<Block> ORE_LEAD = registerOre("ore_lead");
    public static final RegistryObject<Block> ORE_BERYLLIUM = registerOre("ore_beryllium");
    public static final RegistryObject<Block> ORE_COPPER = registerOre("ore_copper");
    public static final RegistryObject<Block> ORE_THORIUM = registerOre("ore_thorium");
    public static final RegistryObject<Block> ORE_COBALT = registerOre("ore_cobalt");
    public static final RegistryObject<Block> ORE_SULFUR = registerOre("ore_sulfur");
    public static final RegistryObject<Block> ORE_NITER = registerOre("ore_niter");
    public static final RegistryObject<Block> ORE_FLUORITE = registerOre("ore_fluorite");
    public static final RegistryObject<Block> ORE_LIGNITE = registerOre("ore_lignite");
    public static final RegistryObject<Block> ORE_ASBESTOS = registerOre("ore_asbestos");
    public static final RegistryObject<Block> ORE_RARE = registerOre("ore_rare");
    public static final RegistryObject<Block> ORE_OIL = registerOre("ore_oil");
    public static final RegistryObject<Block> ORE_OIL_EMPTY = registerOre("ore_oil_empty");
    public static final RegistryObject<Block> ORE_BEDROCK_OIL = registerOre("ore_bedrock_oil");
    public static final RegistryObject<Block> STONE_RESOURCE_HEMATITE = registerResourceStone("stone_resource_hematite");
    public static final RegistryObject<Block> STONE_RESOURCE_MALACHITE = registerResourceStone("stone_resource_malachite");
    public static final RegistryObject<Block> STONE_RESOURCE_BAUXITE = registerResourceStone("stone_resource_bauxite");
    public static final RegistryObject<Block> STONE_RESOURCE_LIMESTONE = registerResourceStone("stone_resource_limestone");
    public static final RegistryObject<Block> ORE_VOLCANO = BLOCKS.register("ore_volcano", OreVolcanoBlock::new);
    public static final RegistryObject<Block> VOLCANO_CORE = BLOCKS.register("volcano_core", () -> new VolcanoBlock(false));
    public static final RegistryObject<Block> VOLCANO_RAD_CORE = BLOCKS.register("volcano_rad_core", () -> new VolcanoBlock(true));
    public static final RegistryObject<Block> VOLCANIC_LAVA = BLOCKS.register("volcanic_lava_block",
            () -> new VolcanicLavaBlock(() -> ModFluids.VOLCANIC_LAVA.source.get(), false));
    public static final RegistryObject<Block> RAD_LAVA = BLOCKS.register("rad_lava_block",
            () -> new VolcanicLavaBlock(() -> ModFluids.RAD_LAVA.source.get(), true));
    public static final RegistryObject<Block> TOXIC_BLOCK = BLOCKS.register("toxic_block",
            () -> new com.hbm.blocks.fluid.ToxicBlock(() -> ModFluids.TOXIC.source.get()));
    public static final RegistryObject<Block> ORE_GNEISS_IRON = registerOre("ore_gneiss_iron");
    public static final RegistryObject<Block> ORE_GNEISS_GOLD = registerOre("ore_gneiss_gold");
    public static final RegistryObject<Block> ORE_GNEISS_URANIUM = registerOre("ore_gneiss_uranium");
    public static final RegistryObject<Block> ORE_GNEISS_COPPER = registerOre("ore_gneiss_copper");
    public static final RegistryObject<Block> ORE_COAL_OIL = registerOre("ore_coal_oil");
    public static final RegistryObject<Block> ORE_COLTAN = registerOre("ore_coltan");
    public static final RegistryObject<Block> ORE_CINNEBAR = registerOre("ore_cinnebar");
    public static final RegistryObject<Block> ORE_GNEISS_ASBESTOS = registerOre("ore_gneiss_asbestos");
    public static final RegistryObject<Block> ORE_GNEISS_LITHIUM = registerOre("ore_gneiss_lithium");
    public static final RegistryObject<Block> ORE_GNEISS_RARE = registerOre("ore_gneiss_rare");
    public static final RegistryObject<Block> ORE_GNEISS_SCHRABIDIUM = registerOre("ore_gneiss_schrabidium");
    public static final RegistryObject<Block> ORE_NETHER_URANIUM = registerOre("ore_nether_uranium");
    public static final RegistryObject<Block> ORE_NETHER_PLUTONIUM = registerOre("ore_nether_plutonium");
    public static final RegistryObject<Block> ORE_NETHER_TUNGSTEN = registerOre("ore_nether_tungsten");
    public static final RegistryObject<Block> ORE_NETHER_SULFUR = registerOre("ore_nether_sulfur");
    public static final RegistryObject<Block> ORE_NETHER_COAL = registerOre("ore_nether_coal");
    public static final RegistryObject<Block> ORE_NETHER_COBALT = registerOre("ore_nether_cobalt");

    public static final RegistryObject<Block> BLOCK_URANIUM = registerMetalBlock("block_uranium", true);
    public static final RegistryObject<Block> BLOCK_TITANIUM = registerMetalBlock("block_titanium", false);
    public static final RegistryObject<Block> BLOCK_TUNGSTEN = registerMetalBlock("block_tungsten", false);
    public static final RegistryObject<Block> BLOCK_ALUMINIUM = registerMetalBlock("block_aluminium", false);
    public static final RegistryObject<Block> BLOCK_LEAD = registerMetalBlock("block_lead", false);
    public static final RegistryObject<Block> BLOCK_BERYLLIUM = registerMetalBlock("block_beryllium", false);
    public static final RegistryObject<Block> BLOCK_STEEL = registerMetalBlock("block_steel", false);
    public static final RegistryObject<Block> BLOCK_COPPER = registerMetalBlock("block_copper", false);
    public static final RegistryObject<Block> BLOCK_RED_COPPER = registerMetalBlock("block_red_copper", false);
    public static final RegistryObject<Block> BLOCK_THORIUM = registerMetalBlock("block_thorium", false);
    public static final RegistryObject<Block> BLOCK_COBALT = registerMetalBlock("block_cobalt", true);
    public static final RegistryObject<Block> BLOCK_LITHIUM = registerMetalBlock("block_lithium", false);
    public static final RegistryObject<Block> BLOCK_GRAPHITE = registerMetalBlock("block_graphite", false);
    public static final RegistryObject<Block> BLOCK_DESH = registerMetalBlock("block_desh", true);
    public static final RegistryObject<Block> BLOCK_DURA_STEEL = registerMetalBlock("block_dura_steel", false);
    public static final RegistryObject<Block> BLOCK_POLYMER = registerMetalBlock("block_polymer", false);
    public static final RegistryObject<Block> BLOCK_COMBINE_STEEL = registerMetalBlock("block_combine_steel", true);
    public static final RegistryObject<Block> BLOCK_MAGNETIZED_TUNGSTEN = registerMetalBlock("block_magnetized_tungsten", false);
    public static final RegistryObject<Block> BLOCK_SCHRARANIUM = registerMetalBlock("block_schraranium", true);
    public static final RegistryObject<Block> BLOCK_SOLINIUM = registerMetalBlock("block_solinium", true);
    public static final RegistryObject<Block> BLOCK_NIOBIUM = registerMetalBlock("block_niobium", false);
    public static final RegistryObject<Block> BLOCK_TANTALIUM = registerMetalBlock("block_tantalium", false);
    public static final RegistryObject<Block> BLOCK_LANTHANIUM = registerMetalBlock("block_lanthanium", false);
    public static final RegistryObject<Block> BLOCK_ZIRCONIUM = registerMetalBlock("block_zirconium", false);
    public static final RegistryObject<Block> BLOCK_AUSTRALIUM = registerMetalBlock("block_australium", true);
    public static final RegistryObject<Block> BLOCK_STARMETAL = registerMetalBlock("block_starmetal", true);
    public static final RegistryObject<Block> BLOCK_BISMUTH = registerMetalBlock("block_bismuth", false);
    public static final RegistryObject<Block> BLOCK_ASBESTOS = registerMetalBlock("block_asbestos", false);
    public static final RegistryObject<Block> BLOCK_EUPHEMIUM = registerMetalBlock("block_euphemium", true);
    public static final RegistryObject<Block> BLOCK_DINEUTRONIUM = registerMetalBlock("block_dineutronium", true);
    public static final RegistryObject<Block> BLOCK_ACTINIUM = registerMetalBlock("block_actinium", false);
    public static final RegistryObject<Block> BLOCK_BAKELITE = registerMetalBlock("block_bakelite", false);
    public static final RegistryObject<Block> BLOCK_BORON = registerMetalBlock("block_boron", false);
    public static final RegistryObject<Block> BLOCK_CADMIUM = registerMetalBlock("block_cadmium", false);
    public static final RegistryObject<Block> BLOCK_COLTAN = registerMetalBlock("block_coltan", false);
    public static final RegistryObject<Block> BLOCK_CORIUM = registerMetalBlock("block_corium", true);
    public static final RegistryObject<Block> BLOCK_CORIUM_COBBLE = registerMetalBlock("block_corium_cobble", false);
    public static final RegistryObject<Block> BLOCK_ETERNIT = registerMetalBlock("block_eternit", false);
    public static final RegistryObject<Block> BLOCK_FERROURANIUM = registerMetalBlock("block_ferrouranium", true);
    public static final RegistryObject<Block> BLOCK_FLUORITE = registerMetalBlock("block_fluorite", false);
    public static final RegistryObject<Block> BLOCK_GUNPOWDER = registerMetalBlock("block_gunpowder", false);
    public static final RegistryObject<Block> BLOCK_NITER = registerMetalBlock("block_niter", false);
    public static final RegistryObject<Block> BLOCK_SULFUR = registerMetalBlock("block_sulfur", false);
    public static final RegistryObject<Block> BLOCK_U238 = registerMetalBlock("block_u238", true);
    public static final RegistryObject<Block> BLOCK_U235 = registerMetalBlock("block_u235", true);
    public static final RegistryObject<Block> BLOCK_PU238 = registerMetalBlock("block_pu238", true);
    public static final RegistryObject<Block> BLOCK_PU239 = registerMetalBlock("block_pu239", true);
    public static final RegistryObject<Block> BLOCK_PU240 = registerMetalBlock("block_pu240", true);
    public static final RegistryObject<Block> BLOCK_NEPTUNIUM = registerMetalBlock("block_neptunium", true);
    public static final RegistryObject<Block> BLOCK_POLONIUM = registerMetalBlock("block_polonium", true);
    public static final RegistryObject<Block> BLOCK_SEMTEX = registerMetalBlock("block_semtex", false);
    public static final RegistryObject<Block> BLOCK_WHITE_PHOSPHORUS = registerMetalBlock("block_white_phosphorus", false);
    public static final RegistryObject<Block> BLOCK_RED_PHOSPHORUS = registerMetalBlock("block_red_phosphorus", false);

    public static final RegistryObject<Block> REINFORCED_BRICK = registerBuildingBlock("reinforced_brick");
    public static final RegistryObject<Block> CONCRETE = registerBuildingBlock("concrete");
    public static final RegistryObject<Block> CONCRETE_SMOOTH = registerBuildingBlock("concrete_smooth");
    public static final RegistryObject<Block> CONCRETE_ASBESTOS = registerBuildingBlock("concrete_asbestos");
    public static final RegistryObject<Block> CONCRETE_SUPER = registerBuildingBlock("concrete_super");
    public static final RegistryObject<Block> ASPHALT = registerBuildingBlock("asphalt");
    public static final RegistryObject<Block> ASPHALT_LIGHT = registerBuildingBlock("asphalt_light");
    public static final RegistryObject<Block> BRICK_CONCRETE = registerBuildingBlock("brick_concrete");
    public static final RegistryObject<Block> BRICK_CONCRETE_MOSSY = registerBuildingBlock("brick_concrete_mossy");
    public static final RegistryObject<Block> BRICK_CONCRETE_CRACKED = registerBuildingBlock("brick_concrete_cracked");
    public static final RegistryObject<Block> BRICK_OBSIDIAN = registerBuildingBlock("brick_obsidian");
    public static final RegistryObject<Block> BRICK_LIGHT = registerBuildingBlock("brick_light");
    public static final RegistryObject<Block> BRICK_COMPOUND = registerBuildingBlock("brick_compound");
    public static final RegistryObject<Block> DUCRETE = registerBuildingBlock("ducrete");
    public static final RegistryObject<Block> BRICK_DUCRETE = registerBuildingBlock("brick_ducrete");
    public static final RegistryObject<Block> REINFORCED_STONE = registerBuildingBlock("reinforced_stone");
    public static final RegistryObject<Block> REINFORCED_SAND = registerBuildingBlock("reinforced_sand");
    public static final RegistryObject<Block> REINFORCED_LIGHT = registerBuildingBlock("reinforced_light");
    public static final RegistryObject<Block> REINFORCED_LAMP_OFF = registerBuildingBlock("reinforced_lamp_off");
    public static final RegistryObject<Block> REINFORCED_GLASS = registerGlassBlock("reinforced_glass");
    public static final RegistryObject<Block> BARBED_WIRE = BLOCKS.register("barbed_wire", BarbedWireBlock::new);
    public static final RegistryObject<Block> SPIKES = BLOCKS.register("spikes", SpikesBlock::new);
    public static final RegistryObject<Block> FENCE_METAL = BLOCKS.register("fence_metal", ChainlinkFenceBlock::new);
    public static final RegistryObject<Block> BARREL_TAINT = BLOCKS.register("barrel_taint", ExplosiveBarrelBlock::taint);
    public static final RegistryObject<Block> BRICK_CONCRETE_BROKEN = registerBuildingBlock("brick_concrete_broken");
    public static final RegistryObject<Block> BRICK_JUNGLE = registerBuildingBlock("brick_jungle");
    public static final RegistryObject<Block> BRICK_JUNGLE_CRACKED = registerBuildingBlock("brick_jungle_cracked");
    public static final RegistryObject<Block> BRICK_JUNGLE_LAVA = registerBuildingBlock("brick_jungle_lava");
    public static final RegistryObject<Block> BRICK_JUNGLE_OOZE = registerBuildingBlock("brick_jungle_ooze");
    public static final RegistryObject<Block> BRICK_RED = registerBuildingBlock("brick_red");
    public static final RegistryObject<Block> CMB_BRICK = registerBuildingBlock("cmb_brick");
    public static final RegistryObject<Block> CMB_BRICK_REINFORCED = registerBuildingBlock("cmb_brick_reinforced");
    public static final RegistryObject<Block> BRICK_ASBESTOS = registerBuildingBlock("brick_asbestos");
    public static final RegistryObject<Block> BRICK_FIRE = registerBuildingBlock("brick_fire");
    public static final RegistryObject<Block> BASALT = registerBuildingBlock("basalt");
    public static final RegistryObject<Block> BASALT_SMOOTH = registerBuildingBlock("basalt_smooth");
    public static final RegistryObject<Block> METEOR_POLISHED = registerBuildingBlock("meteor_polished");
    public static final RegistryObject<Block> METEOR_BRICK = registerBuildingBlock("meteor_brick");
    public static final RegistryObject<Block> METEOR_BRICK_MOSSY = registerBuildingBlock("meteor_brick_mossy");
    public static final RegistryObject<Block> METEOR_BRICK_CRACKED = registerBuildingBlock("meteor_brick_cracked");
    public static final RegistryObject<Block> FACTORY_TITANIUM_HULL = registerBuildingBlock("factory_titanium_hull");
    public static final RegistryObject<Block> STONE_GNEISS = registerBuildingBlock("stone_gneiss");
    public static final RegistryObject<Block> DIRT_DEAD = registerBuildingBlock("dirt_dead");
    public static final RegistryObject<Block> DIRT_OILY = registerBuildingBlock("dirt_oily");
    public static final RegistryObject<Block> SAND_DIRTY = registerBuildingBlock("sand_dirty");
    public static final RegistryObject<Block> SAND_DIRTY_RED = registerBuildingBlock("sand_dirty_red");
    public static final RegistryObject<Block> WASTE_TRINITITE = registerBuildingBlock("waste_trinitite");
    public static final RegistryObject<Block> WASTE_TRINITITE_RED = registerBuildingBlock("waste_trinitite_red");
    public static final RegistryObject<Block> GLASS_BORON = registerGlassBlock("glass_boron");
    public static final RegistryObject<Block> GLASS_LEAD = registerGlassBlock("glass_lead");
    public static final RegistryObject<Block> GLASS_TRINITITE = registerGlassBlock("glass_trinitite");
    public static final RegistryObject<Block> METEOR_COBBLE = registerBuildingBlock("meteor_cobble");
    public static final RegistryObject<Block> METEOR_TREASURE = registerBuildingBlock("meteor_treasure");
    public static final RegistryObject<Block> BASALT_POLISHED = registerBuildingBlock("basalt_polished");
    public static final RegistryObject<Block> FACTORY_ADVANCED_HULL = registerBuildingBlock("factory_advanced_hull");
    public static final RegistryObject<Block> CONCRETE_CYAN = registerBuildingBlock("concrete_cyan");
    public static final RegistryObject<Block> CONCRETE_GRAY = registerBuildingBlock("concrete_gray");
    public static final RegistryObject<Block> CONCRETE_GREEN = registerBuildingBlock("concrete_green");
    public static final RegistryObject<Block> CONCRETE_ORANGE = registerBuildingBlock("concrete_orange");
    public static final RegistryObject<Block> CONCRETE_PINK = registerBuildingBlock("concrete_pink");
    public static final RegistryObject<Block> CONCRETE_YELLOW = registerBuildingBlock("concrete_yellow");
    public static final RegistryObject<Block> TILE_LAB = registerBuildingBlock("tile_lab");
    public static final RegistryObject<Block> TILE_LAB_BROKEN = registerBuildingBlock("tile_lab_broken");
    public static final RegistryObject<Block> TILE_LAB_CRACKED = registerBuildingBlock("tile_lab_cracked");
    public static final RegistryObject<Block> BRICK_FORGOTTEN = registerBuildingBlock("brick_forgotten");
    public static final RegistryObject<Block> STONE_DEPTH = registerBuildingBlock("stone_depth");
    public static final RegistryObject<Block> STONE_DEPTH_NETHER = registerBuildingBlock("stone_depth_nether");
    public static final RegistryObject<Block> DEPTH_BRICK = registerBuildingBlock("depth_brick");
    public static final RegistryObject<Block> DEPTH_TILES = registerBuildingBlock("depth_tiles");
    public static final RegistryObject<Block> BASALT_BRICK = registerBuildingBlock("basalt_brick");
    public static final RegistryObject<Block> BASALT_TILES = registerBuildingBlock("basalt_tiles");
    public static final RegistryObject<Block> CONCRETE_BLACK = registerBuildingBlock("concrete_black");
    public static final RegistryObject<Block> CONCRETE_WHITE = registerBuildingBlock("concrete_white");
    public static final RegistryObject<Block> CONCRETE_BLUE = registerBuildingBlock("concrete_blue");
    public static final RegistryObject<Block> CONCRETE_RED = registerBuildingBlock("concrete_red");
    public static final RegistryObject<Block> CONCRETE_BROWN = registerBuildingBlock("concrete_brown");
    public static final RegistryObject<Block> CONCRETE_PURPLE = registerBuildingBlock("concrete_purple");
    public static final RegistryObject<Block> CONCRETE_SILVER = registerBuildingBlock("concrete_silver");
    public static final RegistryObject<Block> DEPTH_NETHER_TILES = registerBuildingBlock("depth_nether_tiles");
    public static final RegistryObject<Block> BRICK_LIGHT_ALT = registerBuildingBlock("brick_light_alt");
    public static final RegistryObject<Block> BRICK_CONCRETE_MARKED = registerBuildingBlock("brick_concrete_marked");
    public static final RegistryObject<Block> SELLAFIELD_SLAKED = BLOCKS.register("sellafield_slaked",
            SellafieldSlakedBlock::new);
    public static final RegistryObject<Block> SELLAFIELD_0 = BLOCKS.register("sellafield_0", () -> new SellafieldWasteBlock(0));
    public static final RegistryObject<Block> SELLAFIELD_1 = BLOCKS.register("sellafield_1", () -> new SellafieldWasteBlock(1));
    public static final RegistryObject<Block> SELLAFIELD_2 = BLOCKS.register("sellafield_2", () -> new SellafieldWasteBlock(2));
    public static final RegistryObject<Block> SELLAFIELD_3 = BLOCKS.register("sellafield_3", () -> new SellafieldWasteBlock(3));
    public static final RegistryObject<Block> SELLAFIELD_4 = BLOCKS.register("sellafield_4", () -> new SellafieldWasteBlock(4));
    public static final RegistryObject<Block> SAND_BORON = registerBuildingBlock("sand_boron");
    public static final RegistryObject<Block> SAND_LEAD = registerBuildingBlock("sand_lead");
    public static final RegistryObject<Block> SAND_POLONIUM = registerBuildingBlock("sand_polonium");
    public static final RegistryObject<Block> SAND_QUARTZ = registerBuildingBlock("sand_quartz");
    public static final RegistryObject<Block> SAND_URANIUM = registerBuildingBlock("sand_uranium");
    public static final RegistryObject<Block> SELLAFIELD_5 = BLOCKS.register("sellafield_5", () -> new SellafieldWasteBlock(5));
    public static final RegistryObject<Block> SELLAFIELD_SLAKED_1 = registerBuildingBlock("sellafield_slaked_1");
    public static final RegistryObject<Block> SELLAFIELD_SLAKED_2 = registerBuildingBlock("sellafield_slaked_2");
    public static final RegistryObject<Block> SELLAFIELD_SLAKED_3 = registerBuildingBlock("sellafield_slaked_3");
    public static final RegistryObject<Block> REINFORCED_DUCRETE = registerBuildingBlock("reinforced_ducrete");
    public static final RegistryObject<Block> REINFORCED_LAMINATE = registerGlassBlock("reinforced_laminate");
    public static final RegistryObject<Block> REINFORCED_STONE_ALT = registerBuildingBlock("reinforced_stone_alt");
    public static final RegistryObject<Block> STONE_CRACKED = registerBuildingBlock("stone_cracked");
    public static final RegistryObject<Block> STONE_GNEISS_DARK = registerBuildingBlock("stone_gneiss_dark");
    public static final RegistryObject<Block> STONE_GNEISS_VAR = registerBuildingBlock("stone_gneiss_var");
    public static final RegistryObject<Block> STONE_MAGMATIC = registerBuildingBlock("stone_magmatic");
    public static final RegistryObject<Block> STONE_POROUS = registerBuildingBlock("stone_porous");
    public static final RegistryObject<Block> STONE_KEYHOLE = registerBuildingBlock("stone_keyhole");
    public static final RegistryObject<Block> LAMP_DEMON = BLOCKS.register("lamp_demon",
            () -> DecoObjBlock.lamp(Block.box(4, 0, 4, 12, 14, 12), 10));
    public static final RegistryObject<Block> ASH = registerBuildingBlock("ash");
    public static final RegistryObject<Block> ASH_DIGAMMA = registerBuildingBlock("ash_digamma");
    public static final RegistryObject<Block> ANCIENT_SCRAP = registerBuildingBlock("ancient_scrap");
    public static final RegistryObject<Block> ABSORBER = registerBuildingBlock("absorber");
    public static final RegistryObject<Block> ABSORBER_GREEN = registerBuildingBlock("absorber_green");
    public static final RegistryObject<Block> ABSORBER_RED = registerBuildingBlock("absorber_red");
    public static final RegistryObject<Block> BARRICADE = registerBuildingBlock("barricade");
    public static final RegistryObject<Block> BLOCK_C4 = registerBuildingBlock("block_c4");
    public static final RegistryObject<Block> BLOCK_CAP_NUKA = registerBuildingBlock("block_cap_nuka");
    public static final RegistryObject<Block> BLOCK_CAP_FRITZ = registerBuildingBlock("block_cap_fritz");
    public static final RegistryObject<Block> BLOCK_CAP_KORL = registerBuildingBlock("block_cap_korl");
    public static final RegistryObject<Block> BLOCK_CAP_QUANTUM = registerBuildingBlock("block_cap_quantum");
    public static final RegistryObject<Block> BLOCK_CAP_RAD = registerBuildingBlock("block_cap_rad");
    public static final RegistryObject<Block> BLOCK_CAP_SPARKLE = registerBuildingBlock("block_cap_sparkle");
    public static final RegistryObject<Block> BLOCK_CAP_STAR = registerBuildingBlock("block_cap_star");
    public static final RegistryObject<Block> BLOCK_CAP_SUNSET = registerBuildingBlock("block_cap_sunset");
    public static final RegistryObject<Block> CHARGE_C4 = BLOCKS.register("charge_c4", ChargeBlock::c4);
    public static final RegistryObject<Block> CHARGE_DYNAMITE = BLOCKS.register("charge_dynamite", ChargeBlock::dynamite);
    public static final RegistryObject<Block> CHARGE_SEMTEX = BLOCKS.register("charge_semtex", ChargeBlock::semtex);
    public static final RegistryObject<Block> CLUSTER_COPPER = registerCluster("cluster_copper");
    public static final RegistryObject<Block> CLUSTER_IRON = registerCluster("cluster_iron");
    public static final RegistryObject<Block> CLUSTER_TITANIUM = registerCluster("cluster_titanium");
    public static final RegistryObject<Block> CLUSTER_DEPTH_IRON = registerBuildingBlock("cluster_depth_iron");
    public static final RegistryObject<Block> CLUSTER_DEPTH_TITANIUM = registerBuildingBlock("cluster_depth_titanium");
    public static final RegistryObject<Block> CLUSTER_DEPTH_TUNGSTEN = registerBuildingBlock("cluster_depth_tungsten");
    public static final RegistryObject<Block> CRT_BROKEN = BLOCKS.register("crt_broken", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> CRT_BSOD = BLOCKS.register("crt_bsod", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> CRT_CLEAN = BLOCKS.register("crt_clean", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> CRYSTAL_HARDENED = registerBuildingBlock("crystal_hardened");
    public static final RegistryObject<Block> CRYSTAL_PULSAR = registerBuildingBlock("crystal_pulsar");
    public static final RegistryObject<Block> CRYSTAL_VIRUS = registerBuildingBlock("crystal_virus");
    public static final RegistryObject<Block> DEPTH_DNT = registerBuildingBlock("depth_dnt");
    public static final RegistryObject<Block> DEPTH_NETHER_BRICK = registerBuildingBlock("depth_nether_brick");
    public static final RegistryObject<Block> CAGE_LAMP = BLOCKS.register("cage_lamp",
            () -> DecoObjBlock.lamp(Block.box(4, 4, 4, 12, 12, 12), 12));
    public static final RegistryObject<Block> CAGE_LAMP_OFF = BLOCKS.register("cage_lamp_off",
            () -> DecoObjBlock.lamp(Block.box(4, 4, 4, 12, 12, 12), 0));
    public static final RegistryObject<Block> BLOCK_GRAPHITE_FUEL = registerBuildingBlock("block_graphite_fuel");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_LITHIUM = registerBuildingBlock("block_graphite_lithium");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_PLUTONIUM = registerBuildingBlock("block_graphite_plutonium");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_SOURCE = registerBuildingBlock("block_graphite_source");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_TRITIUM = registerBuildingBlock("block_graphite_tritium");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_DETECTOR = registerBuildingBlock("block_graphite_detector");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_DRILLED = registerBuildingBlock("block_graphite_drilled");
    public static final RegistryObject<Block> ABSORBER_PINK = registerBuildingBlock("absorber_pink");
    public static final RegistryObject<Block> CHARGE_MINER = BLOCKS.register("charge_miner", ChargeBlock::miner);
    public static final RegistryObject<Block> CLUSTER_ALUMINIUM = registerCluster("cluster_aluminium");
    public static final RegistryObject<Block> CRT_BLINKING = BLOCKS.register("crt_blinking", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> DIGAMMA_MATTER = registerBuildingBlock("digamma_matter");
    public static final RegistryObject<Block> ELECTRICAL_SCRAP = registerBuildingBlock("electrical_scrap");
    public static final RegistryObject<Block> FOAM = registerBuildingBlock("foam");
    public static final RegistryObject<Block> FROZEN_DIRT = registerBuildingBlock("frozen_dirt");
    public static final RegistryObject<Block> FROZEN_LOG = registerBuildingBlock("frozen_log");
    public static final RegistryObject<Block> FROZEN_PLANKS = registerBuildingBlock("frozen_planks");
    public static final RegistryObject<Block> GAS_ASBESTOS = registerBuildingBlock("gas_asbestos");
    public static final RegistryObject<Block> GAS_COAL = registerBuildingBlock("gas_coal");
    public static final RegistryObject<Block> GAS_EXPLOSIVE = registerBuildingBlock("gas_explosive");
    public static final RegistryObject<Block> GAS_FLAMMABLE = registerBuildingBlock("gas_flammable");
    public static final RegistryObject<Block> GAS_MELTDOWN = registerBuildingBlock("gas_meltdown");
    public static final RegistryObject<Block> GAS_MONOXIDE = BLOCKS.register("gas_monoxide", MonoxideGasBlock::new);
    public static final RegistryObject<Block> GAS_RADON = BLOCKS.register("gas_radon", RadonGasBlock::normal);
    public static final RegistryObject<Block> GAS_RADON_DENSE = BLOCKS.register("gas_radon_dense", RadonGasBlock::dense);
    public static final RegistryObject<Block> GAS_RADON_TOMB = BLOCKS.register("gas_radon_tomb", RadonGasBlock::tomb);
    public static final RegistryObject<Block> GNEISS_BRICK = registerBuildingBlock("gneiss_brick");
    public static final RegistryObject<Block> GNEISS_CHISELED = registerBuildingBlock("gneiss_chiseled");
    public static final RegistryObject<Block> GNEISS_TILE = registerBuildingBlock("gneiss_tile");
    public static final RegistryObject<Block> GRAVEL_DIAMOND = registerBuildingBlock("gravel_diamond");
    public static final RegistryObject<Block> GRAVEL_OBSIDIAN = registerBuildingBlock("gravel_obsidian");
    public static final RegistryObject<Block> MOON_TURF = registerBuildingBlock("moon_turf");
    public static final RegistryObject<Block> MUSH = registerBuildingBlock("mush");
    public static final RegistryObject<Block> PINK_LOG = registerBuildingBlock("pink_log");
    public static final RegistryObject<Block> PINK_PLANKS = registerBuildingBlock("pink_planks");
    public static final RegistryObject<Block> SLAG = registerBuildingBlock("slag");
    public static final RegistryObject<Block> SANDBAGS = registerBuildingBlock("sandbags");
    public static final RegistryObject<Block> TEKTITE = registerBuildingBlock("tektite");
    public static final RegistryObject<Block> STONE_DEPTH_VAR = registerBuildingBlock("stone_depth_var");
    public static final RegistryObject<Block> STONE_GNEISS_ALT = registerBuildingBlock("stone_gneiss_alt");
    public static final RegistryObject<Block> REBAR = registerBuildingBlock("rebar");
    public static final RegistryObject<Block> WOOD_BARRIER = registerBuildingBlock("wood_barrier");
    public static final RegistryObject<Block> GLYPHID_BASE = registerBuildingBlock("glyphid_base");
    public static final RegistryObject<Block> GLYPHID_EGGS = registerBuildingBlock("glyphid_eggs");
    public static final RegistryObject<Block> GLYPHID_EGGS_BASE = registerBuildingBlock("glyphid_eggs_base");
    public static final RegistryObject<Block> PWR_CASING = registerBuildingBlock("pwr_casing");
    public static final RegistryObject<Block> PWR_CASING_PORT = registerBuildingBlock("pwr_casing_port");
    public static final RegistryObject<Block> PWR_CONCRETE_FLAT = registerBuildingBlock("pwr_concrete_flat");
    public static final RegistryObject<Block> PWR_REFLECTOR = registerBuildingBlock("pwr_reflector");
    public static final RegistryObject<Block> PWR_HEATSINK = registerBuildingBlock("pwr_heatsink");
    public static final RegistryObject<Block> PWR_HEATEX = registerBuildingBlock("pwr_heatex");
    public static final RegistryObject<Block> PWR_NEUTRON_SOURCE = registerBuildingBlock("pwr_neutron_source");
    public static final RegistryObject<Block> PWR_PORT = registerBuildingBlock("pwr_port");
    public static final RegistryObject<Block> PWR_BLOCK = registerBuildingBlock("pwr_block");
    public static final RegistryObject<Block> LAMP_TRITIUM_BLUE_OFF = registerBuildingBlock("lamp_tritium_blue_off");
    public static final RegistryObject<Block> LAMP_TRITIUM_BLUE_ON = registerBuildingBlock("lamp_tritium_blue_on");
    public static final RegistryObject<Block> LAMP_TRITIUM_GREEN_OFF = registerBuildingBlock("lamp_tritium_green_off");
    public static final RegistryObject<Block> LAMP_TRITIUM_GREEN_ON = registerBuildingBlock("lamp_tritium_green_on");
    public static final RegistryObject<Block> FLOOD_LAMP = BLOCKS.register("flood_lamp",
            () -> DecoObjBlock.lamp(Block.box(2, 0, 2, 14, 10, 14), 14));
    public static final RegistryObject<Block> FLOOD_LAMP_OFF = BLOCKS.register("flood_lamp_off",
            () -> DecoObjBlock.lamp(Block.box(2, 0, 2, 14, 10, 14), 0));
    public static final RegistryObject<Block> FLUORESCENT_LAMP = BLOCKS.register("fluorescent_lamp",
            () -> DecoObjBlock.lamp(Block.box(4, 0, 0, 12, 10, 16), 12));
    public static final RegistryObject<Block> FLUORESCENT_LAMP_OFF = BLOCKS.register("fluorescent_lamp_off",
            () -> DecoObjBlock.lamp(Block.box(4, 0, 0, 12, 10, 16), 0));
    public static final RegistryObject<Block> REINFORCED_LAMP_ON = registerBuildingBlock("reinforced_lamp_on");
    public static final RegistryObject<Block> FIELD_DISTURBER = registerBuildingBlock("field_disturber");
    public static final RegistryObject<Block> FLAME_WAR = BLOCKS.register("flame_war", SpecialtyBombBlock::flameWar);
    public static final RegistryObject<Block> TAINT = BLOCKS.register("taint", TaintBlock::new);
    public static final RegistryObject<Block> GLYPHID = registerBuildingBlock("glyphid");
    public static final RegistryObject<Block> GLYPHID_BASE_ALT = registerBuildingBlock("glyphid_base_alt");
    public static final RegistryObject<Block> GLYPHID_BASE_INFESTED = registerBuildingBlock("glyphid_base_infested");
    public static final RegistryObject<Block> GLYPHID_BASE_INFESTED_ALT = registerBuildingBlock("glyphid_base_infested_alt");
    public static final RegistryObject<Block> GLYPHID_BASE_RAD = registerBuildingBlock("glyphid_base_rad");
    public static final RegistryObject<Block> GLYPHID_BASE_RAD_ALT = registerBuildingBlock("glyphid_base_rad_alt");
    public static final RegistryObject<Block> GLYPHID_EGGS_ALT = registerBuildingBlock("glyphid_eggs_alt");
    public static final RegistryObject<Block> GLYPHID_EGGS_BASE_INFESTED = registerBuildingBlock("glyphid_eggs_base_infested");
    public static final RegistryObject<Block> GLYPHID_EGGS_BASE_RAD = registerBuildingBlock("glyphid_eggs_base_rad");
    public static final RegistryObject<Block> GLYPHID_EGGS_INFESTED = registerBuildingBlock("glyphid_eggs_infested");
    public static final RegistryObject<Block> GLYPHID_EGGS_RAD = registerBuildingBlock("glyphid_eggs_rad");
    public static final RegistryObject<Block> GRAVEL_OBSIDIAN_ALT = registerBuildingBlock("gravel_obsidian_alt");
    public static final RegistryObject<Block> METEOR = registerBuildingBlock("meteor");
    public static final RegistryObject<Block> MUSH_BLOCK_INSIDE = registerBuildingBlock("mush_block_inside");
    public static final RegistryObject<Block> MUSH_BLOCK_SKIN = registerBuildingBlock("mush_block_skin");
    public static final RegistryObject<Block> MUSH_BLOCK_STEM = registerBuildingBlock("mush_block_stem");
    public static final RegistryObject<Block> OIL_SPILL = registerBuildingBlock("oil_spill");
    public static final RegistryObject<Block> PWR_CONTROLLER = registerBuildingBlock("pwr_controller");
    public static final RegistryObject<Block> REBAR_BASE = registerBuildingBlock("rebar_base");
    public static final RegistryObject<Block> RTG = BLOCKS.register("rtg", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> RTG_CELL = BLOCKS.register("rtg_cell", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> RTG_POLONIUM = BLOCKS.register("rtg_polonium", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> SELLAFIELD_FRAMED_BASE = registerBuildingBlock("sellafield_framed_base");
    public static final RegistryObject<Block> STRUCTURE_ANCHOR = registerBuildingBlock("structure_anchor");
    public static final RegistryObject<Block> THERM_ENDO = BLOCKS.register("therm_endo", SpecialtyBombBlock::thermEndo);
    public static final RegistryObject<Block> THERM_EXO = BLOCKS.register("therm_exo", SpecialtyBombBlock::thermExo);
    public static final RegistryObject<Block> TOASTER_IRON = BLOCKS.register("toaster_iron", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> TOASTER_STEEL = BLOCKS.register("toaster_steel", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> TOASTER_WOOD = BLOCKS.register("toaster_wood", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> EMITTER = registerBuildingBlock("emitter");





    public static final RegistryObject<Block> WASTE_PLANKS = registerBuildingBlock("waste_planks");
    public static final RegistryObject<Block> WASTE_LEAVES = registerBuildingBlock("waste_leaves");
    public static final RegistryObject<Block> DECO_STEEL = registerBuildingBlock("deco_steel");
    public static final RegistryObject<Block> DECO_ALUMINIUM = registerBuildingBlock("deco_aluminium");
    public static final RegistryObject<Block> DECO_TITANIUM = registerBuildingBlock("deco_titanium");
    public static final RegistryObject<Block> DECO_TUNGSTEN = registerBuildingBlock("deco_tungsten");
    public static final RegistryObject<Block> DECO_RUSTY_STEEL = registerBuildingBlock("deco_rusty_steel");
    public static final RegistryObject<Block> BLOCK_METEOR = registerBuildingBlock("block_meteor");
    public static final RegistryObject<Block> BLOCK_METEOR_COBBLE = registerBuildingBlock("block_meteor_cobble");
    public static final RegistryObject<Block> BLOCK_METEOR_BROKEN = registerBuildingBlock("block_meteor_broken");
    public static final RegistryObject<Block> BLOCK_PLUTONIUM = registerMetalBlock("block_plutonium", false);
    public static final RegistryObject<Block> BLOCK_PU241 = registerMetalBlock("block_pu241", false);
    public static final RegistryObject<Block> BLOCK_MOX_FUEL = registerMetalBlock("block_mox_fuel", false);
    public static final RegistryObject<Block> BLOCK_NITER_REINFORCED = registerMetalBlock("block_niter_reinforced", false);
    public static final RegistryObject<Block> BLOCK_CDALLOY = registerMetalBlock("block_cdalloy", false);
    public static final RegistryObject<Block> GLASS_QUARTZ = registerGlassBlock("glass_quartz");
    public static final RegistryObject<Block> GLASS_POLARIZED = registerGlassBlock("glass_polarized");
    public static final RegistryObject<Block> GLASS_ASH = registerGlassBlock("glass_ash");
    public static final RegistryObject<Block> LADDER_STEEL = BLOCKS.register("ladder_steel", ModBlocks::metalLadder);
    public static final RegistryObject<Block> LADDER_ALUMINIUM = BLOCKS.register("ladder_aluminium", ModBlocks::metalLadder);
    public static final RegistryObject<Block> LADDER_GOLD = BLOCKS.register("ladder_gold", ModBlocks::metalLadder);
    public static final RegistryObject<Block> LADDER_TITANIUM = BLOCKS.register("ladder_titanium", ModBlocks::metalLadder);
    public static final RegistryObject<Block> LADDER_COPPER = BLOCKS.register("ladder_copper", ModBlocks::metalLadder);
    public static final RegistryObject<Block> LADDER_LEAD = BLOCKS.register("ladder_lead", ModBlocks::metalLadder);
    public static final RegistryObject<Block> LADDER_COBALT = BLOCKS.register("ladder_cobalt", ModBlocks::metalLadder);
    public static final RegistryObject<Block> LADDER_IRON = BLOCKS.register("ladder_iron", ModBlocks::metalLadder);
    public static final RegistryObject<Block> LADDER_STURDY = BLOCKS.register("ladder_sturdy", ModBlocks::metalLadder);
    public static final RegistryObject<Block> LADDER_TUNGSTEN = BLOCKS.register("ladder_tungsten", ModBlocks::metalLadder);
    public static final RegistryObject<Block> MINE_FAT = BLOCKS.register("mine_fat", LandmineBlock::fat);
    public static final RegistryObject<Block> MINE_NAVAL = BLOCKS.register("mine_naval", LandmineBlock::naval);
    public static final RegistryObject<Block> MINE_SHRAP = BLOCKS.register("mine_shrap", LandmineBlock::shrap);
    public static final RegistryObject<Block> REINFORCED_LAMINATE_PANE = BLOCKS.register("reinforced_laminate_pane",
            () -> new IronBarsBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(10.0F, 50.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistryObject<Block> STEEL_SCAFFOLD = BLOCKS.register("steel_scaffold", SteelScaffoldBlock::new);
    public static final RegistryObject<Block> STEEL_BEAM = BLOCKS.register("steel_beam", SteelBeamBlock::new);
    public static final RegistryObject<Block> STEEL_ROOF = BLOCKS.register("steel_roof", SteelRoofBlock::new);
    public static final RegistryObject<Block> STEEL_WALL = BLOCKS.register("steel_wall", SteelWallBlock::new);
    public static final RegistryObject<Block> STEEL_WALL_ALT = BLOCKS.register("steel_wall_alt", SteelWallBlock::new);
    public static final RegistryObject<Block> CONCRETE_LIGHT_BLUE = registerBuildingBlock("concrete_light_blue");
    public static final RegistryObject<Block> CONCRETE_LIME = registerBuildingBlock("concrete_lime");
    public static final RegistryObject<Block> CONCRETE_MAGENTA = registerBuildingBlock("concrete_magenta");
    public static final RegistryObject<Block> CONCRETE_REBAR = registerBuildingBlock("concrete_rebar");
    public static final RegistryObject<Block> CONCRETE_TILE = registerBuildingBlock("concrete_tile");
    public static final RegistryObject<Block> CONCRETE_SUPER_BROKEN = registerBuildingBlock("concrete_super_broken");
    public static final RegistryObject<Block> BLOCK_METEOR_MOLTEN = BLOCKS.register("block_meteor_molten", MeteorMoltenBlock::new);
    public static final RegistryObject<Block> BLOCK_WASTE = BLOCKS.register("block_waste",
            () -> new HazardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(5.0F, 10.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE), false, 15.0F));
    public static final RegistryObject<Block> BLOCK_WASTE_PAINTED = BLOCKS.register("block_waste_painted",
            () -> new HazardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(5.0F, 10.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE), false, 15.0F));
    public static final RegistryObject<Block> BLOCK_WASTE_VITRIFIED = BLOCKS.register("block_waste_vitrified",
            () -> new HazardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(5.0F, 10.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS), false, 7.5F));
    public static final RegistryObject<Block> BLOCK_SCRAP = registerBuildingBlock("block_scrap");
    public static final RegistryObject<Block> BLOCK_YELLOWCAKE = registerBuildingBlock("block_yellowcake");
    public static final RegistryObject<Block> REINFORCED_GLASS_PANE = BLOCKS.register("reinforced_glass_pane",
            () -> new IronBarsBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(10.0F, 50.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS)
                    .noOcclusion()));
    public static final RegistryObject<Block> BLOCK_SCHRABIDIUM = registerMetalBlock("block_schrabidium", false);
    public static final RegistryObject<Block> BLOCK_SATURNITE = registerMetalBlock("block_saturnite", false);
    public static final RegistryObject<Block> BLOCK_TCALLOY = registerMetalBlock("block_tcalloy", false);
    public static final RegistryObject<Block> BLOCK_RUBBER = registerMetalBlock("block_rubber", false);
    public static final RegistryObject<Block> BLOCK_PLUTONIUM_FUEL = registerMetalBlock("block_plutonium_fuel", false);
    public static final RegistryObject<Block> BLOCK_PU_MIX = registerMetalBlock("block_pu_mix", false);
    public static final RegistryObject<Block> BLOCK_RA226 = registerMetalBlock("block_ra226", false);
    public static final RegistryObject<Block> BLOCK_SCHRABIDATE = registerMetalBlock("block_schrabidate", false);
    public static final RegistryObject<Block> BLOCK_SCHRABIDIUM_FUEL = registerMetalBlock("block_schrabidium_fuel", false);
    public static final RegistryObject<Block> BLOCK_THORIUM_FUEL = registerMetalBlock("block_thorium_fuel", false);
    public static final RegistryObject<Block> BLOCK_TRINITITE = registerMetalBlock("block_trinitite", false);
    public static final RegistryObject<Block> BLOCK_U233 = registerMetalBlock("block_u233", false);
    public static final RegistryObject<Block> BLOCK_URANIUM_FUEL = registerMetalBlock("block_uranium_fuel", false);
    public static final RegistryObject<Block> BLOCK_SLAG = registerBuildingBlock("block_slag");
    public static final RegistryObject<Block> BLOCK_SLAG_BROKEN = registerBuildingBlock("block_slag_broken");
    public static final RegistryObject<Block> BLOCK_RUST = registerBuildingBlock("block_rust");
    public static final RegistryObject<Block> BLOCK_TAR = registerBuildingBlock("block_tar");
    public static final RegistryObject<Block> BLOCK_PLASTIC_BASE = registerBuildingBlock("block_plastic_base");
    public static final RegistryObject<Block> BLOCK_METEOR_TREASURE = BLOCKS.register("block_meteor_treasure", MeteorTreasureBlock::new);
    public static final RegistryObject<Block> BARREL_IRON = BLOCKS.register("barrel_iron", DecorativeBarrelBlock::new);
    public static final RegistryObject<Block> BARREL_STEEL = BLOCKS.register("barrel_steel", DecorativeBarrelBlock::new);
    public static final RegistryObject<Block> BARREL_PLASTIC = BLOCKS.register("barrel_plastic", DecorativeBarrelBlock::new);
    public static final RegistryObject<Block> BARREL_CORRODED = BLOCKS.register("barrel_corroded", DecorativeBarrelBlock::new);
    public static final RegistryObject<Block> BARREL_RED = BLOCKS.register("barrel_red", ExplosiveBarrelBlock::fire);
    public static final RegistryObject<Block> BARREL_YELLOW = BLOCKS.register("barrel_yellow", ExplosiveBarrelBlock::yellow);
    public static final RegistryObject<Block> BARREL_PINK = BLOCKS.register("barrel_pink", ExplosiveBarrelBlock::fire);
    public static final RegistryObject<Block> BARREL_TCALLOY = BLOCKS.register("barrel_tcalloy", DecorativeBarrelBlock::new);
    public static final RegistryObject<Block> BARREL_VITRIFIED = BLOCKS.register("barrel_vitrified", ExplosiveBarrelBlock::yellow);
    public static final RegistryObject<Block> BARREL_LOX = BLOCKS.register("barrel_lox", ExplosiveBarrelBlock::lox);
    public static final RegistryObject<Block> BARREL_ANTIMATTER = BLOCKS.register("barrel_antimatter", DecorativeBarrelBlock::new);
    public static final RegistryObject<Block> BARBED_WIRE_ACID = BLOCKS.register("barbed_wire_acid",
            () -> new BarbedWireBlock(BarbedWireBlock.Effect.POISON, 3.0F));
    public static final RegistryObject<Block> BARBED_WIRE_FIRE = BLOCKS.register("barbed_wire_fire",
            () -> new BarbedWireBlock(BarbedWireBlock.Effect.FIRE, 2.0F));
    public static final RegistryObject<Block> BARBED_WIRE_POISON = BLOCKS.register("barbed_wire_poison",
            () -> new BarbedWireBlock(BarbedWireBlock.Effect.POISON, 2.0F));
    public static final RegistryObject<Block> BARBED_WIRE_WITHER = BLOCKS.register("barbed_wire_wither",
            () -> new BarbedWireBlock(BarbedWireBlock.Effect.WITHER, 2.0F));
    public static final RegistryObject<Block> BARBED_WIRE_ULTRADEATH = BLOCKS.register("barbed_wire_ultradeath",
            () -> new BarbedWireBlock(BarbedWireBlock.Effect.WITHER, 5.0F));
    public static final RegistryObject<Block> SCAFFOLD_STEEL = BLOCKS.register("scaffold_steel", SteelScaffoldBlock::new);
    public static final RegistryObject<Block> SCAFFOLD_RED = BLOCKS.register("scaffold_red", SteelScaffoldBlock::new);
    public static final RegistryObject<Block> SCAFFOLD_WHITE = BLOCKS.register("scaffold_white", SteelScaffoldBlock::new);
    public static final RegistryObject<Block> SCAFFOLD_YELLOW = BLOCKS.register("scaffold_yellow", SteelScaffoldBlock::new);
    public static final RegistryObject<Block> SCAFFOLD_RUSTEDSTEEL = BLOCKS.register("scaffold_rustedsteel", SteelScaffoldBlock::new);
    public static final RegistryObject<Block> CONCRETE_REBAR_ALT = registerBuildingBlock("concrete_rebar_alt");
    public static final RegistryObject<Block> CONCRETE_TILE_TREFOIL = registerBuildingBlock("concrete_tile_trefoil");
    public static final RegistryObject<Block> CONCRETE_SUPER_M0 = registerBuildingBlock("concrete_super_m0");
    public static final RegistryObject<Block> CONCRETE_SUPER_M1 = registerBuildingBlock("concrete_super_m1");
    public static final RegistryObject<Block> CONCRETE_SUPER_M2 = registerBuildingBlock("concrete_super_m2");
    public static final RegistryObject<Block> CONCRETE_SUPER_M3 = registerBuildingBlock("concrete_super_m3");
    public static final RegistryObject<Block> BRICK_BASE = registerBuildingBlock("brick_base");
    public static final RegistryObject<Block> BRICK_JUNGLE_CIRCLE = registerBuildingBlock("brick_jungle_circle");
    public static final RegistryObject<Block> BRICK_JUNGLE_FRAGILE = registerBuildingBlock("brick_jungle_fragile");
    public static final RegistryObject<Block> BRICK_JUNGLE_MYSTIC = registerBuildingBlock("brick_jungle_mystic");
    public static final RegistryObject<Block> BRICK_FORGOTTEN_BW = registerBuildingBlock("brick_forgotten_bw");
    public static final RegistryObject<Block> BRICK_FORGOTTEN_HOLE = registerBuildingBlock("brick_forgotten_hole");
    public static final RegistryObject<Block> CM_BLOCK_STEEL = registerMetalBlock("cm_block_steel", false);
    public static final RegistryObject<Block> CM_BLOCK_DESH = registerMetalBlock("cm_block_desh", false);
    public static final RegistryObject<Block> CM_BLOCK_ALLOY = registerMetalBlock("cm_block_alloy", false);
    public static final RegistryObject<Block> CM_BLOCK_TCALLOY = registerMetalBlock("cm_block_tcalloy", false);
    public static final RegistryObject<Block> CM_BLOCK_BASE = registerMetalBlock("cm_block_base", false);
    public static final RegistryObject<Block> CM_SHEET_STEEL = registerBuildingBlock("cm_sheet_steel");
    public static final RegistryObject<Block> CM_SHEET_DESH = registerBuildingBlock("cm_sheet_desh");
    public static final RegistryObject<Block> CM_SHEET_ALLOY = registerBuildingBlock("cm_sheet_alloy");
    public static final RegistryObject<Block> CM_SHEET_TCALLOY = registerBuildingBlock("cm_sheet_tcalloy");
    public static final RegistryObject<Block> CM_SHEET_BASE = registerBuildingBlock("cm_sheet_base");
    public static final RegistryObject<Block> CM_TANK_STEEL = registerBuildingBlock("cm_tank_steel");
    public static final RegistryObject<Block> CM_TANK_DESH = registerBuildingBlock("cm_tank_desh");
    public static final RegistryObject<Block> CM_TANK_ALLOY = registerBuildingBlock("cm_tank_alloy");
    public static final RegistryObject<Block> CM_TANK_TCALLOY = registerBuildingBlock("cm_tank_tcalloy");
    public static final RegistryObject<Block> CM_PORT_STEEL = registerBuildingBlock("cm_port_steel");
    public static final RegistryObject<Block> CM_PORT_DESH = registerBuildingBlock("cm_port_desh");
    public static final RegistryObject<Block> CM_PORT_ALLOY = registerBuildingBlock("cm_port_alloy");
    public static final RegistryObject<Block> CM_PORT_TCALLOY = registerBuildingBlock("cm_port_tcalloy");
    public static final RegistryObject<Block> DECO_ASBESTOS = registerBuildingBlock("deco_asbestos");
    public static final RegistryObject<Block> DECO_BERYLLIUM = registerBuildingBlock("deco_beryllium");
    public static final RegistryObject<Block> DECO_BLANK_NEW = registerBuildingBlock("deco_blank_new");
    public static final RegistryObject<Block> DECO_LEAD = registerBuildingBlock("deco_lead");
    public static final RegistryObject<Block> DECO_RED_COPPER = registerBuildingBlock("deco_red_copper");
    public static final RegistryObject<Block> BLOCK_STEEL_MACHINE = registerMetalBlock("block_steel_machine", false);
    public static final RegistryObject<Block> ORE_ALEXANDRITE = registerOre("ore_alexandrite");
    public static final RegistryObject<Block> ORE_AUSTRALIUM = registerOre("ore_australium");
    public static final RegistryObject<Block> ORE_BASALT_ASBESTOS = registerOre("ore_basalt_asbestos");
    public static final RegistryObject<Block> ORE_BASALT_FLUORITE = registerOre("ore_basalt_fluorite");
    public static final RegistryObject<Block> ORE_BASALT_GEM = registerOre("ore_basalt_gem");
    public static final RegistryObject<Block> ORE_BASALT_MOLYSITE = registerOre("ore_basalt_molysite");
    public static final RegistryObject<Block> ORE_BASALT_SULFUR = registerOre("ore_basalt_sulfur");
    public static final RegistryObject<Block> ORE_BEDROCK_COLTAN = registerOre("ore_bedrock_coltan");
    public static final RegistryObject<Block> ORE_COAL_OIL_BURNING = registerOre("ore_coal_oil_burning");
    public static final RegistryObject<Block> ORE_DEPTH_BORAX = registerOre("ore_depth_borax");
    public static final RegistryObject<Block> ORE_DEPTH_CINNEBAR = registerOre("ore_depth_cinnebar");
    public static final RegistryObject<Block> ORE_DEPTH_NETHER_NEODYMIUM = registerOre("ore_depth_nether_neodymium");
    public static final RegistryObject<Block> ORE_DEPTH_ZIRCONIUM = registerOre("ore_depth_zirconium");
    public static final RegistryObject<Block> BRICK_CRACKED_BASE = registerBuildingBlock("brick_cracked_base");
    public static final RegistryObject<Block> BRICK_FORGOTTEN_LOCK = registerBuildingBlock("brick_forgotten_lock");
    public static final RegistryObject<Block> BRICK_FORGOTTEN_SIGN = registerBuildingBlock("brick_forgotten_sign");
    public static final RegistryObject<Block> BRICK_JUNGLE_TRAP = registerBuildingBlock("brick_jungle_trap");
    public static final RegistryObject<Block> BRICK_RED_TOP_ALT = registerBuildingBlock("brick_red_top_alt");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_0 = registerBuildingBlock("brick_jungle_glyph_0");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_1 = registerBuildingBlock("brick_jungle_glyph_1");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_2 = registerBuildingBlock("brick_jungle_glyph_2");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_3 = registerBuildingBlock("brick_jungle_glyph_3");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_4 = registerBuildingBlock("brick_jungle_glyph_4");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_5 = registerBuildingBlock("brick_jungle_glyph_5");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_6 = registerBuildingBlock("brick_jungle_glyph_6");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_7 = registerBuildingBlock("brick_jungle_glyph_7");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_8 = registerBuildingBlock("brick_jungle_glyph_8");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_9 = registerBuildingBlock("brick_jungle_glyph_9");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_10 = registerBuildingBlock("brick_jungle_glyph_10");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_11 = registerBuildingBlock("brick_jungle_glyph_11");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_12 = registerBuildingBlock("brick_jungle_glyph_12");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_13 = registerBuildingBlock("brick_jungle_glyph_13");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_14 = registerBuildingBlock("brick_jungle_glyph_14");
    public static final RegistryObject<Block> BRICK_JUNGLE_GLYPH_15 = registerBuildingBlock("brick_jungle_glyph_15");
    public static final RegistryObject<Block> CM_CIRCUIT_ALUMINIUM = registerBuildingBlock("cm_circuit_aluminium");
    public static final RegistryObject<Block> CM_CIRCUIT_COPPER = registerBuildingBlock("cm_circuit_copper");
    public static final RegistryObject<Block> CM_CIRCUIT_GOLD = registerBuildingBlock("cm_circuit_gold");
    public static final RegistryObject<Block> CM_CIRCUIT_RED_COPPER = registerBuildingBlock("cm_circuit_red_copper");
    public static final RegistryObject<Block> CM_CIRCUIT_SCHRABIDIUM = registerBuildingBlock("cm_circuit_schrabidium");
    public static final RegistryObject<Block> CM_ENGINE_BISMUTH = registerBuildingBlock("cm_engine_bismuth");
    public static final RegistryObject<Block> CM_ENGINE_DESH = registerBuildingBlock("cm_engine_desh");
    public static final RegistryObject<Block> CM_ENGINE_STANDARD = registerBuildingBlock("cm_engine_standard");
    public static final RegistryObject<Block> METEOR_BRICK_CHISELED = registerBuildingBlock("meteor_brick_chiseled");
    public static final RegistryObject<Block> METEOR_COBBLE_MOLTEN = registerBuildingBlock("meteor_cobble_molten");
    public static final RegistryObject<Block> METEOR_CRUSHED = registerBuildingBlock("meteor_crushed");
    public static final RegistryObject<Block> METEOR_PILLAR = registerBuildingBlock("meteor_pillar");
    public static final RegistryObject<Block> METEOR_POWER = registerBuildingBlock("meteor_power");
    public static final RegistryObject<Block> DUCRETE_TILE = registerBuildingBlock("ducrete_tile");
    public static final RegistryObject<Block> DIRT_OILY_ALT = registerBuildingBlock("dirt_oily_alt");
    public static final RegistryObject<Block> DECO_STEEL_POLES = registerBuildingBlock("deco_steel_poles");
    public static final RegistryObject<Block> DECO_STEEL_ORIG = registerBuildingBlock("deco_steel_orig");
    public static final RegistryObject<Block> DECO_STEEL_BLANK_OUTER = registerBuildingBlock("deco_steel_blank_outer");
    public static final RegistryObject<Block> GLASS_POLONIUM = registerGlassBlock("glass_polonium");
    public static final RegistryObject<Block> GLASS_URANIUM = registerGlassBlock("glass_uranium");
    public static final RegistryObject<Block> ORE_SCHRABIDIUM = registerOre("ore_schrabidium");
    public static final RegistryObject<Block> ORE_LITHIUM = registerOre("ore_lithium");
    public static final RegistryObject<Block> ORE_SALPETER = registerOre("ore_salpeter");
    public static final RegistryObject<Block> ORE_TIKITE = registerOre("ore_tikite");
    public static final RegistryObject<Block> ORE_TIKITE_ALT = registerOre("ore_tikite_alt");
    public static final RegistryObject<Block> ORE_TEKTITE_OSMIRIDIUM = registerOre("ore_tektite_osmiridium");
    public static final RegistryObject<Block> ORE_NETHER_SCHRABIDIUM = registerOre("ore_nether_schrabidium");
    public static final RegistryObject<Block> ORE_NETHER_FIRE = registerOre("ore_nether_fire");
    public static final RegistryObject<Block> ORE_NETHER_SMOLDERING = registerOre("ore_nether_smoldering");
    public static final RegistryObject<Block> ORE_NETHER_URANIUM_SCORCHED = registerOre("ore_nether_uranium_scorched");
    public static final RegistryObject<Block> ORE_URANIUM_SCORCHED = registerOre("ore_uranium_scorched");
    public static final RegistryObject<Block> ORE_GNEISS_URANIUM_SCORCHED = registerOre("ore_gneiss_uranium_scorched");
    public static final RegistryObject<Block> ORE_GNEISS_GAS = registerOre("ore_gneiss_gas");
    public static final RegistryObject<Block> ORE_OIL_SAND = registerOre("ore_oil_sand");
    public static final RegistryObject<Block> ORE_OIL_SAND_ALT = registerOre("ore_oil_sand_alt");
    public static final RegistryObject<Block> DECO_COMPUTER = BLOCKS.register("deco_computer", DecoObjBlock::decoModel);
    public static final RegistryObject<Block> DECO_SATELLITE_RECEIVER = BLOCKS.register("deco_satellite_receiver", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> DECO_TAPE_RECORDER = BLOCKS.register("deco_tape_recorder", DecoObjBlock::smallAppliance);







    public static final RegistryObject<Block> DECO_RBMK = BLOCKS.register("deco_rbmk", RBMKDecoBlock::new);
    public static final RegistryObject<Block> DECO_RBMK_SMOOTH = BLOCKS.register("deco_rbmk_smooth", RBMKDecoBlock::new);

    public static final RegistryObject<Block> FLUID_BARREL = BLOCKS.register("fluid_barrel", FluidBarrelBlock::new);
    public static final RegistryObject<Block> MACHINE_BATTERY = BLOCKS.register("machine_battery", MachineBatteryBlock::new);
    public static final RegistryObject<Block> MACHINE_BATTERY_INFINITE =
            BLOCKS.register("machine_battery_infinite", InfiniteBatteryBlock::new);
    public static final RegistryObject<Block> COMBUSTION_GENERATOR = BLOCKS.register("combustion_generator", CombustionGeneratorBlock::new);
    public static final RegistryObject<Block> DIESEL_GENERATOR = BLOCKS.register("diesel_generator", DieselGeneratorBlock::new);
    public static final RegistryObject<Block> ANVIL_IRON = BLOCKS.register("anvil_iron", () -> new NtmAnvilBlock(1));
    public static final RegistryObject<Block> ANVIL_LEAD = BLOCKS.register("anvil_lead", () -> new NtmAnvilBlock(1));
    public static final RegistryObject<Block> ANVIL_STEEL = BLOCKS.register("anvil_steel", () -> new NtmAnvilBlock(2));
    public static final RegistryObject<Block> ANVIL_DESH = BLOCKS.register("anvil_desh", () -> new NtmAnvilBlock(3));
    public static final RegistryObject<Block> ANVIL_FERROURANIUM = BLOCKS.register("anvil_ferrouranium", () -> new NtmAnvilBlock(4));
    public static final RegistryObject<Block> ANVIL_SATURNITE = BLOCKS.register("anvil_saturnite", () -> new NtmAnvilBlock(5));
    public static final RegistryObject<Block> ANVIL_BISMUTH_BRONZE = BLOCKS.register("anvil_bismuth_bronze", () -> new NtmAnvilBlock(5));
    public static final RegistryObject<Block> ANVIL_ARSENIC_BRONZE = BLOCKS.register("anvil_arsenic_bronze", () -> new NtmAnvilBlock(5));
    public static final RegistryObject<Block> ANVIL_SCHRABIDATE = BLOCKS.register("anvil_schrabidate", () -> new NtmAnvilBlock(6));
    public static final RegistryObject<Block> ANVIL_DNT = BLOCKS.register("anvil_dnt", () -> new NtmAnvilBlock(7));
    public static final RegistryObject<Block> ANVIL_OSMIRIDIUM = BLOCKS.register("anvil_osmiridium", () -> new NtmAnvilBlock(8));
    public static final RegistryObject<Block> ANVIL_MURKY = BLOCKS.register("anvil_murky", () -> new NtmAnvilBlock(1_916_169));
    public static final RegistryObject<Block> MACHINE_PRESS = BLOCKS.register("machine_press", MachinePressBlock::new);
    public static final RegistryObject<Block> MACHINE_EPRESS = BLOCKS.register("machine_epress", MachineEPressBlock::new);
    public static final RegistryObject<Block> MACHINE_SHREDDER = BLOCKS.register("machine_shredder", MachineShredderBlock::new);
    public static final RegistryObject<Block> MACHINE_CENTRIFUGE = BLOCKS.register("machine_centrifuge", MachineCentrifugeBlock::new);
    public static final RegistryObject<Block> MACHINE_GASCENT = BLOCKS.register("machine_gascent", MachineGasCentBlock::new);
    public static final RegistryObject<Block> MACHINE_FEL = BLOCKS.register("machine_fel", MachineFelBlock::new);
    public static final RegistryObject<Block> MACHINE_SILEX = BLOCKS.register("machine_silex", MachineSilexBlock::new);
    public static final RegistryObject<Block> MACHINE_CRYSTALLIZER = BLOCKS.register("machine_crystallizer", MachineCrystallizerBlock::new);
    public static final RegistryObject<Block> MACHINE_MIXER = BLOCKS.register("machine_mixer", MachineMixerBlock::new);
    public static final RegistryObject<Block> MACHINE_ARC_WELDER = BLOCKS.register("machine_arc_welder", MachineArcWelderBlock::new);
    public static final RegistryObject<Block> MACHINE_PUREX = BLOCKS.register("machine_purex", MachinePurexBlock::new);
    public static final RegistryObject<Block> MACHINE_SOLDERING_STATION = BLOCKS.register("machine_soldering_station", MachineSolderingStationBlock::new);
    public static final RegistryObject<Block> MACHINE_RTG = BLOCKS.register("machine_rtg", MachineRtgBlock::new);
    public static final RegistryObject<Block> MACHINE_DIFURNACE = BLOCKS.register("machine_difurnace", MachineDiFurnaceBlock::new);
    public static final RegistryObject<Block> MACHINE_DIFURNACE_EXTENSION =
            BLOCKS.register("machine_difurnace_extension", MachineDiFurnaceExtensionBlock::new);
    public static final RegistryObject<Block> MACHINE_DIFURNACE_RTG_OFF =
            BLOCKS.register("machine_difurnace_rtg_off", MachineDiFurnaceRtgBlock::new);
    public static final RegistryObject<Block> MACHINE_BLAST_FURNACE = BLOCKS.register("machine_blast_furnace", MachineBlastFurnaceBlock::new);
    public static final RegistryObject<Block> MACHINE_ASSEMBLY_MACHINE = BLOCKS.register("machine_assembly_machine", MachineAssemblyMachineBlock::new);
    public static final RegistryObject<Block> MACHINE_CHEMICAL_PLANT = BLOCKS.register("machine_chemical_plant", MachineChemicalPlantBlock::new);
    public static final RegistryObject<Block> HEATER_FIREBOX = BLOCKS.register("heater_firebox", HeaterFireboxBlock::new);
    public static final RegistryObject<Block> MACHINE_BOILER = BLOCKS.register("machine_boiler", MachineHeatBoilerBlock::new);
    public static final RegistryObject<Block> MACHINE_TURBINE = BLOCKS.register("machine_turbine", MachineTurbineBlock::new);
    public static final RegistryObject<Block> MACHINE_CONDENSER = BLOCKS.register("machine_condenser", MachineCondenserBlock::new);
    public static final RegistryObject<Block> MACHINE_WOOD_BURNER = BLOCKS.register("machine_wood_burner", MachineWoodBurnerBlock::new);
    public static final RegistryObject<Block> MACHINE_WELL = BLOCKS.register("machine_well", MachineOilWellBlock::new);
    public static final RegistryObject<Block> MACHINE_PUMPJACK = BLOCKS.register("machine_pumpjack", MachinePumpjackBlock::new);
    public static final RegistryObject<Block> MACHINE_REFINERY = BLOCKS.register("machine_refinery", MachineRefineryBlock::new);
    public static final RegistryObject<Block> MACHINE_FRACTION_TOWER =
            BLOCKS.register("machine_fraction_tower", MachineFractionTowerBlock::new);
    public static final RegistryObject<Block> FRACTION_SPACER = BLOCKS.register("fraction_spacer", FractionSpacerBlock::new);
    public static final RegistryObject<Block> MACHINE_CATALYTIC_CRACKER =
            BLOCKS.register("machine_catalytic_cracker", MachineCatalyticCrackerBlock::new);
    public static final RegistryObject<Block> MACHINE_HYDROTREATER =
            BLOCKS.register("machine_hydrotreater", MachineHydrotreaterBlock::new);
    public static final RegistryObject<Block> MACHINE_CATALYTIC_REFORMER =
            BLOCKS.register("machine_catalytic_reformer", MachineCatalyticReformerBlock::new);
    public static final RegistryObject<Block> MACHINE_VACUUM_DISTILL =
            BLOCKS.register("machine_vacuum_distill", MachineVacuumDistillBlock::new);
    public static final RegistryObject<Block> OIL_PIPE = BLOCKS.register("oil_pipe", OilPipeBlock::new);
    public static final RegistryObject<Block> ELECTRIC_FURNACE = BLOCKS.register("electric_furnace", ElectricFurnaceBlock::new);
    public static final RegistryObject<Block> MACHINE_SIREN = BLOCKS.register("machine_siren", MachineSirenBlock::new);
    public static final RegistryObject<Block> GEIGER = BLOCKS.register("geiger", GeigerCounterBlock::new);
    public static final RegistryObject<Block> BROADCASTER_PC = BLOCKS.register("broadcaster_pc", PinkCloudBroadcasterBlock::new);
    public static final RegistryObject<Block> RED_CABLE = BLOCKS.register("red_cable", RedCableBlock::new);
    public static final RegistryObject<Block> RED_CABLE_CLASSIC = BLOCKS.register("red_cable_classic", RedCableBlock::new);
    public static final RegistryObject<Block> RED_WIRE_COATED = BLOCKS.register("red_wire_coated", RedCableBlock::new);
    public static final RegistryObject<Block> RED_CONNECTOR = BLOCKS.register("red_connector", RedConnectorBlock::new);
    public static final RegistryObject<Block> RED_PYLON = BLOCKS.register("red_pylon", RedPylonBlock::new);
    public static final RegistryObject<Block> RED_PYLON_MEDIUM_WOOD =
            BLOCKS.register("red_pylon_medium_wood", () -> new PylonDummyableBlock(PylonKind.MEDIUM_WOOD));
    public static final RegistryObject<Block> RED_PYLON_MEDIUM_WOOD_TRANSFORMER =
            BLOCKS.register("red_pylon_medium_wood_transformer",
                    () -> new PylonDummyableBlock(PylonKind.MEDIUM_WOOD_TRANSFORMER));
    public static final RegistryObject<Block> RED_PYLON_MEDIUM_STEEL =
            BLOCKS.register("red_pylon_medium_steel", () -> new PylonDummyableBlock(PylonKind.MEDIUM_STEEL));
    public static final RegistryObject<Block> RED_PYLON_MEDIUM_STEEL_TRANSFORMER =
            BLOCKS.register("red_pylon_medium_steel_transformer",
                    () -> new PylonDummyableBlock(PylonKind.MEDIUM_STEEL_TRANSFORMER));
    public static final RegistryObject<Block> RED_PYLON_LARGE =
            BLOCKS.register("red_pylon_large", () -> new PylonDummyableBlock(PylonKind.LARGE));
    public static final RegistryObject<Block> SUBSTATION =
            BLOCKS.register("substation", () -> new PylonDummyableBlock(PylonKind.SUBSTATION));
    public static final RegistryObject<Block> CABLE_SWITCH = BLOCKS.register("cable_switch", CableSwitchBlock::new);
    public static final RegistryObject<Block> CABLE_DETECTOR = BLOCKS.register("cable_detector", CableDetectorBlock::new);
    public static final RegistryObject<Block> CABLE_DIODE = BLOCKS.register("cable_diode", CableDiodeBlock::new);
    public static final RegistryObject<Block> FLUID_DUCT_NEO =
            BLOCKS.register("fluid_duct_neo", FluidDuctStandardBlock::new);
    public static final RegistryObject<Block> FLUID_VALVE =
            BLOCKS.register("fluid_valve", FluidValveBlock::new);
    public static final RegistryObject<Block> FLUID_SWITCH =
            BLOCKS.register("fluid_switch", FluidSwitchBlock::new);
    public static final RegistryObject<Block> FLUID_COUNTER_VALVE =
            BLOCKS.register("fluid_counter_valve", FluidCounterValveBlock::new);
    public static final RegistryObject<Block> CONVEYOR = BLOCKS.register("conveyor", ConveyorBlock::new);
    public static final RegistryObject<Block> CRANE_INSERTER = BLOCKS.register("crane_inserter", CraneInserterBlock::new);
    public static final RegistryObject<Block> CRANE_EXTRACTOR =
            BLOCKS.register("crane_extractor", CraneExtractorBlock::new);
    public static final RegistryObject<Block> CRATE_IRON = BLOCKS.register("crate_iron",
            () -> new StorageCrateBlock(36, "container.hbm.crate_iron"));
    public static final RegistryObject<Block> CRATE_STEEL = BLOCKS.register("crate_steel",
            () -> new StorageCrateBlock(54, "container.hbm.crate_steel"));
    public static final RegistryObject<Block> FILING_CABINET = BLOCKS.register("filing_cabinet", FileCabinetBlock::new);
    public static final RegistryObject<Block> SAFE = BLOCKS.register("safe", SafeBlock::new);
    public static final RegistryObject<Block> CRATE = BLOCKS.register("crate", LootCrateBlock::supply);
    public static final RegistryObject<Block> CRATE_WEAPON = BLOCKS.register("crate_weapon", LootCrateBlock::weapon);
    public static final RegistryObject<Block> CRATE_LEAD = BLOCKS.register("crate_lead", LootCrateBlock::lead);
    public static final RegistryObject<Block> CRATE_METAL = BLOCKS.register("crate_metal", LootCrateBlock::metal);
    public static final RegistryObject<Block> CRATE_RED = BLOCKS.register("crate_red", LootCrateBlock::red);
    public static final RegistryObject<Block> CRATE_AMMO = BLOCKS.register("crate_ammo", LootCrateBlock::ammo);
    public static final RegistryObject<Block> CRATE_CAN = BLOCKS.register("crate_can", LootCrateBlock::can);
    public static final RegistryObject<Block> CRATE_SUPPLY = BLOCKS.register("crate_supply", LootCrateBlock::supply);
    public static final RegistryObject<Block> DECO_LOOT = BLOCKS.register("deco_loot", DecoLootBlock::new);
    public static final RegistryObject<Block> METEOR_SPAWNER = BLOCKS.register("meteor_spawner", MeteorSpawnerBlock::new);
    public static final RegistryObject<Block> WAND_JIGSAW = BLOCKS.register("wand_jigsaw", StructureWandBlock::new);
    public static final RegistryObject<Block> WAND_LOOT = BLOCKS.register("wand_loot", StructureWandBlock::new);
    public static final RegistryObject<Block> WAND_LOGIC = BLOCKS.register("wand_logic", StructureWandBlock::new);
    public static final RegistryObject<Block> WAND_TANDEM = BLOCKS.register("wand_tandem", StructureWandBlock::new);

    public static final RegistryObject<Block> DYNAMITE = BLOCKS.register("dynamite", () -> new BombBlock(8.0F));
    public static final RegistryObject<Block> TNT = BLOCKS.register("tnt", () -> new BombBlock(10.0F));
    public static final RegistryObject<Block> SEMTEX = BLOCKS.register("semtex", () -> new BombBlock(12.0F));
    public static final RegistryObject<Block> C4 = BLOCKS.register("c4", () -> new BombBlock(15.0F, true));
    public static final RegistryObject<Block> DET_CORD = BLOCKS.register("det_cord", DetCordBlock::new);
    public static final RegistryObject<Block> DET_CHARGE = BLOCKS.register("det_charge", DetExplosiveBlock::charge);
    public static final RegistryObject<Block> DET_NUKE = BLOCKS.register("det_nuke", DetExplosiveBlock::nuke);
    public static final RegistryObject<Block> DET_MINER = BLOCKS.register("det_miner", DetExplosiveBlock::miner);
    public static final RegistryObject<Block> BOMB_MULTI = BLOCKS.register("bomb_multi", BombMultiBlock::new);
    public static final RegistryObject<Block> LAUNCH_PAD = BLOCKS.register("launch_pad", LaunchPadBlock::new);
    public static final RegistryObject<Block> LAUNCH_PAD_LARGE =
            BLOCKS.register("launch_pad_large", LaunchPadLargeBlock::new);
    public static final RegistryObject<Block> LAUNCH_PAD_RUSTED =
            BLOCKS.register("launch_pad_rusted", LaunchPadRustedBlock::new);
    public static final RegistryObject<Block> COMPACT_LAUNCHER =
            BLOCKS.register("compact_launcher", CompactLauncherBlock::new);
    public static final RegistryObject<Block> LAUNCH_TABLE =
            BLOCKS.register("launch_table", LaunchTableBlock::new);
    public static final RegistryObject<Block> STRUCT_LAUNCHER =
            BLOCKS.register("struct_launcher", StructPartBlock::new);
    public static final RegistryObject<Block> STRUCT_SCAFFOLD =
            BLOCKS.register("struct_scaffold", StructPartBlock::new);
    public static final RegistryObject<Block> STRUCT_LAUNCHER_CORE =
            BLOCKS.register("struct_launcher_core", () -> new StructLauncherCoreBlock(StructLauncherCoreBlock.Kind.COMPACT));
    public static final RegistryObject<Block> STRUCT_LAUNCHER_CORE_LARGE =
            BLOCKS.register("struct_launcher_core_large", () -> new StructLauncherCoreBlock(StructLauncherCoreBlock.Kind.LARGE));
    public static final RegistryObject<Block> MACHINE_MISSILE_ASSEMBLY =
            BLOCKS.register("machine_missile_assembly", MissileAssemblyBlock::new);
    public static final RegistryObject<Block> MACHINE_RADAR = BLOCKS.register("machine_radar", RadarNTBlock::new);
    public static final RegistryObject<Block> MACHINE_RADAR_LARGE =
            BLOCKS.register("machine_radar_large", RadarLargeBlock::new);
    public static final RegistryObject<Block> RADAR_SCREEN = BLOCKS.register("radar_screen", RadarScreenBlock::new);
    public static final RegistryObject<Block> BOMB_FLOAT = BLOCKS.register("bomb_float", SpecialtyBombBlock::floatBomb);
    public static final RegistryObject<Block> EMP_BOMB = BLOCKS.register("emp_bomb", SpecialtyBombBlock::emp);
    public static final RegistryObject<Block> FIREWORKS = BLOCKS.register("fireworks", FireworksBlock::new);
    public static final RegistryObject<Block> FISSURE_BOMB = BLOCKS.register("fissure_bomb", FissureBombBlock::new);
    public static final RegistryObject<Block> CRASHED_BOMB = BLOCKS.register("crashed_bomb", CrashedBombBlock::new);
    /** Eternal green fire residue left by balefire digs (not a bulk stone cube). */
    public static final RegistryObject<Block> BALEFIRE = BLOCKS.register("balefire", BalefireBlock::new);
    public static final RegistryObject<Block> NUKE_PROTOTYPE = BLOCKS.register("nuke_prototype", NukePrototypeBlock::new);
    public static final RegistryObject<Block> NUKE_CUSTOM = BLOCKS.register("nuke_custom", NukeCustomBlock::new);
    public static final RegistryObject<Block> MINE_AP = BLOCKS.register("mine_ap", LandmineBlock::ap);
    public static final RegistryObject<Block> MINE_HE = BLOCKS.register("mine_he", LandmineBlock::he);
    public static final RegistryObject<Block> NUKE_BOY = BLOCKS.register("nuke_boy", NukeBoyBlock::new);
    public static final RegistryObject<Block> NUKE_MAN = BLOCKS.register("nuke_man", NukeManBlock::new);
    public static final RegistryObject<Block> NUKE_GADGET = BLOCKS.register("nuke_gadget", NukeGadgetBlock::new);
    public static final RegistryObject<Block> NUKE_MIKE = BLOCKS.register("nuke_mike", NukeMikeBlock::new);
    public static final RegistryObject<Block> NUKE_TSAR = BLOCKS.register("nuke_tsar", NukeTsarBlock::new);
    public static final RegistryObject<Block> NUKE_FLEIJA = BLOCKS.register("nuke_fleija", NukeFleijaBlock::new);
    public static final RegistryObject<Block> NUKE_SOLINIUM = BLOCKS.register("nuke_solinium", NukeSoliniumBlock::new);
    public static final RegistryObject<Block> NUKE_FSTBMB = BLOCKS.register("nuke_fstbmb", NukeFstbmbBlock::new);
    public static final RegistryObject<Block> NUKE_N2 = BLOCKS.register("nuke_n2", NukeN2Block::new);
    public static final RegistryObject<Block> WASTE_EARTH = BLOCKS.register("waste_earth", WasteEarthBlock::new);
    public static final RegistryObject<Block> WASTE_LOG = BLOCKS.register("waste_log", WasteLogBlock::new);
    public static final RegistryObject<Block> WASTE_MYCELIUM = BLOCKS.register("waste_mycelium", WasteMyceliumBlock::new);
    /** Thin radioactive ash layer (legacy BlockFallout). */
    public static final RegistryObject<Block> FALLOUT = BLOCKS.register("fallout", FalloutBlock::new);
    public static final RegistryObject<Block> SELLAFIELD_BEDROCK = BLOCKS.register("sellafield_bedrock", SellafieldBedrockBlock::new);
    public static final RegistryObject<Block> ORE_SELLAFIELD_DIAMOND = registerBuildingBlock("ore_sellafield_diamond");
    public static final RegistryObject<Block> ORE_SELLAFIELD_EMERALD = registerBuildingBlock("ore_sellafield_emerald");
    public static final RegistryObject<Block> ORE_SELLAFIELD_URANIUM_SCORCHED = registerBuildingBlock("ore_sellafield_uranium_scorched");
    public static final RegistryObject<Block> ORE_SELLAFIELD_SCHRABIDIUM = registerBuildingBlock("ore_sellafield_schrabidium");
    public static final RegistryObject<Block> ORE_SELLAFIELD_RADGEM = registerBuildingBlock("ore_sellafield_radgem");

    public static final RegistryObject<Block> RBMK_BLANK = BLOCKS.register("rbmk_blank",
            () -> new RBMKPassiveBlock(RBMKColumnType.BLANK));
    public static final RegistryObject<Block> RBMK_REFLECTOR = BLOCKS.register("rbmk_reflector",
            () -> new RBMKPassiveBlock(RBMKColumnType.REFLECTOR));
    public static final RegistryObject<Block> RBMK_ABSORBER = BLOCKS.register("rbmk_absorber",
            () -> new RBMKPassiveBlock(RBMKColumnType.ABSORBER));
    public static final RegistryObject<Block> RBMK_MODERATOR = BLOCKS.register("rbmk_moderator",
            () -> new RBMKPassiveBlock(RBMKColumnType.MODERATOR));

    public static final RegistryObject<Block> STEEL_GRATE = BLOCKS.register("steel_grate", () -> new SteelGrateBlock(false));
    public static final RegistryObject<Block> STEEL_GRATE_WIDE = BLOCKS.register("steel_grate_wide", () -> new SteelGrateBlock(true));
    public static final RegistryObject<Block> STEEL_CORNER = BLOCKS.register("steel_corner", SteelWallBlock::new);
    public static final RegistryObject<Block> STEEL_POLES = BLOCKS.register("steel_poles", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> POLE_TOP = BLOCKS.register("pole_top", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> DECO_PIPE = decoPipe("deco_pipe", 0);
    public static final RegistryObject<Block> DECO_PIPE_RUSTED = decoPipe("deco_pipe_rusted", 0);
    public static final RegistryObject<Block> DECO_PIPE_RED = decoPipe("deco_pipe_red", 0);
    public static final RegistryObject<Block> DECO_PIPE_MARKED = decoPipe("deco_pipe_marked", 0);
    public static final RegistryObject<Block> DECO_PIPE_RIM_GREEN = decoPipe("deco_pipe_rim_green", 1);
    public static final RegistryObject<Block> DECO_PIPE_RIM_MARKED = decoPipe("deco_pipe_rim_marked", 1);
    public static final RegistryObject<Block> DECO_PIPE_RIM_RUSTED = decoPipe("deco_pipe_rim_rusted", 1);
    public static final RegistryObject<Block> DECO_PIPE_RIM_GREEN_RUSTED = decoPipe("deco_pipe_rim_green_rusted", 1);
    public static final RegistryObject<Block> DECO_PIPE_FRAMED = decoPipe("deco_pipe_framed", 3);
    public static final RegistryObject<Block> DECO_PIPE_FRAMED_RUSTED = decoPipe("deco_pipe_framed_rusted", 3);
    public static final RegistryObject<Block> DECO_PIPE_FRAMED_RED = decoPipe("deco_pipe_framed_red", 3);
    public static final RegistryObject<Block> DECO_PIPE_FRAMED_GREEN_RUSTED = decoPipe("deco_pipe_framed_green_rusted", 3);
    public static final RegistryObject<Block> DECO_PIPE_QUAD = decoPipe("deco_pipe_quad", 2);
    public static final RegistryObject<Block> DECO_PIPE_QUAD_RUSTED = decoPipe("deco_pipe_quad_rusted", 2);
    public static final RegistryObject<Block> DECO_PIPE_QUAD_RED = decoPipe("deco_pipe_quad_red", 2);
    public static final RegistryObject<Block> DECO_PIPE_QUAD_MARKED = decoPipe("deco_pipe_quad_marked", 2);
    public static final RegistryObject<Block> CONCRETE_COLORED = BLOCKS.register("concrete_colored", ColoredConcreteBlock::new);
    public static final RegistryObject<Block> CONCRETE_COLORED_EXT = BLOCKS.register("concrete_colored_ext", ConcreteExtBlock::new);
    public static final RegistryObject<Block> CONCRETE_PILLAR = BLOCKS.register("concrete_pillar",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(15.0F, 180.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> CONCRETE_SLAB = BLOCKS.register("concrete_slab", StructureMultiSlabBlock::new);
    public static final RegistryObject<Block> CONCRETE_BRICK_SLAB = BLOCKS.register("concrete_brick_slab", StructureMultiSlabBlock::new);
    public static final RegistryObject<Block> BRICK_SLAB = BLOCKS.register("brick_slab", StructureMultiSlabBlock::new);
    public static final RegistryObject<Block> CONCRETE_STAIRS = registerStairs("concrete_stairs", CONCRETE);
    public static final RegistryObject<Block> CONCRETE_SMOOTH_STAIRS = registerStairs("concrete_smooth_stairs", CONCRETE_SMOOTH);
    public static final RegistryObject<Block> CONCRETE_ASBESTOS_STAIRS = registerStairs("concrete_asbestos_stairs", CONCRETE_ASBESTOS);
    public static final RegistryObject<Block> BRICK_CONCRETE_STAIRS = registerStairs("brick_concrete_stairs", BRICK_CONCRETE);
    public static final RegistryObject<Block> BRICK_CONCRETE_MOSSY_STAIRS = registerStairs("brick_concrete_mossy_stairs", BRICK_CONCRETE_MOSSY);
    public static final RegistryObject<Block> BRICK_CONCRETE_CRACKED_STAIRS = registerStairs("brick_concrete_cracked_stairs", BRICK_CONCRETE_CRACKED);
    public static final RegistryObject<Block> BRICK_CONCRETE_BROKEN_STAIRS = registerStairs("brick_concrete_broken_stairs", BRICK_CONCRETE_BROKEN);
    public static final RegistryObject<Block> BRICK_LIGHT_STAIRS = registerStairs("brick_light_stairs", BRICK_LIGHT);
    public static final RegistryObject<Block> BRICK_COMPOUND_STAIRS = registerStairs("brick_compound_stairs", BRICK_COMPOUND);
    public static final RegistryObject<Block> BRICK_OBSIDIAN_STAIRS = registerStairs("brick_obsidian_stairs", BRICK_OBSIDIAN);
    public static final RegistryObject<Block> REINFORCED_BRICK_STAIRS = registerStairs("reinforced_brick_stairs", REINFORCED_BRICK);
    public static final RegistryObject<Block> REINFORCED_STONE_STAIRS = registerStairs("reinforced_stone_stairs", REINFORCED_STONE);
    public static final RegistryObject<Block> LIGHTSTONE = BLOCKS.register("lightstone", LightstoneBlock::new);
    public static final RegistryObject<Block> LIGHTSTONE_BRICKS_STAIRS = registerStairs("lightstone_bricks_stairs", LIGHTSTONE);
    public static final RegistryObject<Block> DOOR_METAL = BLOCKS.register("door_metal", () -> new NtmDoorBlock(5.0F, 5.0F));
    public static final RegistryObject<Block> DOOR_OFFICE = BLOCKS.register("door_office", () -> new NtmDoorBlock(10.0F, 10.0F));
    public static final RegistryObject<Block> DOOR_BUNKER = BLOCKS.register("door_bunker", () -> new NtmDoorBlock(10.0F, 100.0F));
    public static final RegistryObject<Block> SILO_HATCH = BLOCKS.register("silo_hatch", () -> new SiloHatchBlock(false));
    public static final RegistryObject<Block> SILO_HATCH_LARGE = BLOCKS.register("silo_hatch_large", () -> new SiloHatchBlock(true));
    public static final RegistryObject<Block> TRAPDOOR_STEEL = BLOCKS.register("trapdoor_steel", SteelTrapDoorBlock::new);
    public static final RegistryObject<Block> PLANT_DEAD = BLOCKS.register("plant_dead", DeadPlantBlock::new);
    public static final RegistryObject<Block> PLANT_FLOWER = BLOCKS.register("plant_flower", PlantFlowerBlock::new);
    public static final RegistryObject<Block> LEAVES_LAYER = BLOCKS.register("leaves_layer",
            () -> new ThinLayerBlock(MapColor.COLOR_GREEN, SoundType.GRASS));
    public static final RegistryObject<Block> NTM_DIRT = registerBuildingBlock("ntm_dirt");
    public static final RegistryObject<Block> WOOD_STRUCTURE = BLOCKS.register("wood_structure", WoodStructureBlock::new);
    public static final RegistryObject<Block> METEOR_BATTERY = BLOCKS.register("meteor_battery",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(15.0F, 360.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryObject<Block> CHARGER = BLOCKS.register("charger", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> CAPACITOR_COPPER = BLOCKS.register("capacitor_copper",
            () -> new DecoObjBlock(Block.box(0, 0, 0, 16, 16, 16), MapColor.METAL, 5.0F, 10.0F, SoundType.METAL, 0));
    public static final RegistryObject<Block> TESLA = BLOCKS.register("tesla", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> RADIOREC = BLOCKS.register("radiorec", RadioRecBlock::new);
    public static final RegistryObject<Block> HEV_BATTERY = BLOCKS.register("hev_battery", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> MACHINE_FUNNEL = BLOCKS.register("machine_funnel", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> MACHINE_MICROWAVE = BLOCKS.register("machine_microwave", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> MACHINE_CONTROLLER = BLOCKS.register("machine_controller", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> MACHINE_FLUIDTANK = BLOCKS.register("machine_fluidtank", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> MACHINE_WEAPON_TABLE = BLOCKS.register("machine_weapon_table", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> MACHINE_ROTARY_FURNACE = BLOCKS.register("machine_rotary_furnace", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> RAIL_NARROW = BLOCKS.register("rail_narrow",
            () -> DecoObjBlock.floorFixture(Block.box(0, 0, 0, 16, 2, 16)));
    public static final RegistryObject<Block> BOBBLEHEAD = BLOCKS.register("bobblehead", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> SKELETON_HOLDER = BLOCKS.register("skeleton_holder", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> TURRET_HOWARD_DAMAGED = BLOCKS.register("turret_howard_damaged", DecoObjBlock::smallAppliance);
    public static final RegistryObject<Block> TURRET_SENTRY_DAMAGED = BLOCKS.register("turret_sentry_damaged", DecoObjBlock::smallAppliance);

    private ModBlocks() {
    }

    private static LadderBlock metalLadder() {
        return new LadderBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0F, 2.0F)
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    private static RegistryObject<Block> decoPipe(String id, int type) {
        return BLOCKS.register(id, () -> new DecoPipeBlock(type));
    }

    private static RegistryObject<Block> registerStairs(String name, RegistryObject<Block> parent) {
        return BLOCKS.register(name, () -> new StairBlock(parent.get().defaultBlockState(),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(15.0F, 100.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)));
    }

    private static RegistryObject<Block> registerOre(String name) {
        return BLOCKS.register(name, () -> new OutgasOreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> registerResourceStone(String name) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)));
    }

    private static RegistryObject<Block> registerCluster(String name) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 15.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)));
    }

    private static RegistryObject<Block> registerMetalBlock(String name, boolean beaconBase) {
        Supplier<Block> factory = () -> new HazardBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 50.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL), beaconBase);
        return BLOCKS.register(name, factory);
    }

    private static RegistryObject<Block> registerBuildingBlock(String name) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(15.0F, 100.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)));
    }

    private static RegistryObject<Block> registerGlassBlock(String name) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(10.0F, 50.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.GLASS)
                .noOcclusion()));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
