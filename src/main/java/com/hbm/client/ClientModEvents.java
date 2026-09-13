package com.hbm.client;

import com.hbm.HbmNuclearTechMod;
import com.hbm.client.screen.AnvilScreen;
import com.hbm.client.screen.AssemblyMachineScreen;
import com.hbm.client.screen.ChemicalPlantScreen;
import com.hbm.client.screen.CraneExtractorScreen;
import com.hbm.client.screen.CraneInserterScreen;
import com.hbm.client.screen.FireboxScreen;
import com.hbm.client.screen.BlastFurnaceScreen;
import com.hbm.client.screen.BombMultiScreen;
import com.hbm.client.screen.CombustionGeneratorScreen;
import com.hbm.client.screen.DieselGeneratorScreen;
import com.hbm.client.screen.DiFurnaceScreen;
import com.hbm.client.screen.DiFurnaceRtgScreen;
import com.hbm.client.screen.ElectricFurnaceScreen;
import com.hbm.client.screen.EPressScreen;
import com.hbm.client.screen.PressScreen;
import com.hbm.client.screen.RtgScreen;
import com.hbm.client.screen.CentrifugeScreen;
import com.hbm.client.screen.GasCentScreen;
import com.hbm.client.screen.FelScreen;
import com.hbm.client.screen.SilexScreen;
import com.hbm.client.screen.CrystallizerScreen;
import com.hbm.client.screen.MixerScreen;
import com.hbm.client.screen.ArcWelderScreen;
import com.hbm.client.screen.PurexScreen;
import com.hbm.client.screen.SolderingStationScreen;
import com.hbm.client.screen.ShredderScreen;
import com.hbm.client.screen.FluidBarrelScreen;
import com.hbm.client.screen.LaunchPadScreen;
import com.hbm.client.screen.LaunchPadRustedScreen;
import com.hbm.client.screen.CompactLauncherScreen;
import com.hbm.client.screen.LaunchTableScreen;
import com.hbm.client.screen.RadarNTSlotsScreen;
import com.hbm.client.screen.MachineBatteryScreen;
import com.hbm.client.screen.MachineSirenScreen;
import com.hbm.client.screen.MissileAssemblyScreen;
import com.hbm.client.screen.NukeBoyScreen;
import com.hbm.client.screen.NukeCustomScreen;
import com.hbm.client.screen.NukeFleijaScreen;
import com.hbm.client.screen.NukeFstbmbScreen;
import com.hbm.client.screen.NukeGadgetScreen;
import com.hbm.client.screen.NukeManScreen;
import com.hbm.client.screen.NukeMikeScreen;
import com.hbm.client.screen.NukeN2Screen;
import com.hbm.client.screen.NukePrototypeScreen;
import com.hbm.client.screen.NukeSoliniumScreen;
import com.hbm.client.screen.NukeTsarScreen;
import com.hbm.client.screen.FileCabinetScreen;
import com.hbm.client.screen.SafeScreen;
import com.hbm.client.screen.StorageCrateScreen;
import com.hbm.client.screen.TurbineScreen;
import com.hbm.client.screen.WoodBurnerScreen;
import com.hbm.client.screen.OilWellScreen;
import com.hbm.client.screen.PumpjackScreen;
import com.hbm.client.screen.RefineryScreen;
import com.hbm.client.screen.HydrotreaterScreen;
import com.hbm.client.screen.CatalyticReformerScreen;
import com.hbm.client.screen.VacuumDistillScreen;
import com.hbm.client.render.blockentity.AssembledNukeRenderer;
import com.hbm.client.render.blockentity.RenderAssemblyMachine;
import com.hbm.client.render.blockentity.RenderChemicalPlant;
import com.hbm.client.render.blockentity.RenderFileCabinet;
import com.hbm.client.render.blockentity.RenderFirebox;
import com.hbm.client.render.blockentity.RenderBoiler;
import com.hbm.client.render.blockentity.RenderWoodBurner;
import com.hbm.client.render.blockentity.RenderDerrick;
import com.hbm.client.render.blockentity.RenderPumpjack;
import com.hbm.client.render.blockentity.RenderRefinery;
import com.hbm.client.render.blockentity.RenderFractionTower;
import com.hbm.client.render.blockentity.RenderFractionSpacer;
import com.hbm.client.render.blockentity.RenderSiloHatch;
import com.hbm.client.render.blockentity.RenderCatalyticCracker;
import com.hbm.client.render.blockentity.RenderHydrotreater;
import com.hbm.client.render.blockentity.RenderCatalyticReformer;
import com.hbm.client.render.blockentity.RenderVacuumDistill;
import com.hbm.client.render.blockentity.RenderBlastFurnace;
import com.hbm.client.render.blockentity.RenderBroadcaster;
import com.hbm.client.render.blockentity.RenderRadioRec;
import com.hbm.client.render.blockentity.RenderLoot;
import com.hbm.client.render.blockentity.RenderEPress;
import com.hbm.client.render.blockentity.RenderRtg;
import com.hbm.client.render.blockentity.RenderCentrifuge;
import com.hbm.client.render.blockentity.RenderGasCent;
import com.hbm.client.render.blockentity.RenderFel;
import com.hbm.client.render.blockentity.RenderSilex;
import com.hbm.client.render.blockentity.RenderCrystallizer;
import com.hbm.client.render.blockentity.RenderMixer;
import com.hbm.client.render.blockentity.RenderArcWelder;
import com.hbm.client.render.blockentity.RenderPurex;
import com.hbm.client.render.blockentity.RenderSolderingStation;
import com.hbm.client.render.blockentity.RenderPress;
import com.hbm.client.render.blockentity.RenderDieselGen;
import com.hbm.client.render.blockentity.RenderPylon;
import com.hbm.client.render.blockentity.RenderCrashedBomb;
import com.hbm.client.render.blockentity.RenderLandmine;
import com.hbm.client.render.blockentity.RenderLaunchPad;
import com.hbm.client.render.blockentity.RenderLaunchPadLarge;
import com.hbm.client.render.blockentity.RenderLaunchPadRusted;
import com.hbm.client.render.blockentity.RenderCompactLauncher;
import com.hbm.client.render.blockentity.RenderLaunchTable;
import com.hbm.client.render.blockentity.RenderMissileAssembly;
import com.hbm.client.render.blockentity.RenderRadar;
import com.hbm.client.render.blockentity.RenderRadarLarge;
import com.hbm.client.render.blockentity.RenderRadarScreen;
import com.hbm.client.render.entity.PrimedBombRenderer;
import com.hbm.client.render.entity.RenderBlackHole;
import com.hbm.client.render.entity.RenderBombProjectiles;
import com.hbm.client.render.entity.RenderBomber;
import com.hbm.client.render.entity.RenderEMPBlast;
import com.hbm.client.render.entity.RenderMissile;
import com.hbm.client.render.entity.RenderMissileAntiBallistic;
import com.hbm.client.render.entity.RenderMissileCustom;
import com.hbm.client.render.entity.RenderFireworks;
import com.hbm.client.render.entity.RenderMovingItem;
import com.hbm.client.render.entity.RenderNukeCloud;
import com.hbm.client.render.entity.RenderRubble;
import com.hbm.client.render.entity.RenderTorex;
import com.hbm.client.render.missile.MissilePartModels;
import com.hbm.blocks.generic.SellafieldSlakedBlock;
import com.hbm.items.machine.ItemCassette;
import com.hbm.items.machine.ItemRTGPelletDepleted;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModFluids;
import com.hbm.registry.ModItems;
import com.hbm.registry.ModEntities;
import com.hbm.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerMenuScreens();
            ItemProperties.register(ModItems.PELLET_RTG_DEPLETED.get(),
                    new ResourceLocation(RefStrings.MODID, "mat"),
                    (stack, level, entity, seed) -> ItemRTGPelletDepleted.modelIndex(stack));
        });
    }

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            int intensity = state.hasProperty(SellafieldSlakedBlock.INTENSITY)
                    ? state.getValue(SellafieldSlakedBlock.INTENSITY)
                    : 0;
            return SellafieldSlakedBlock.tintColor(intensity);
        }, ModBlocks.SELLAFIELD_SLAKED.get());
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> SellafieldSlakedBlock.tintColor(0),
                ModBlocks.SELLAFIELD_SLAKED.get());
        event.register(ItemCassette::overlayColor, ModItems.SIREN_TRACK.get());
        Item[] buckets = ModFluids.entries().stream()
                .map(entry -> entry.bucket.get())
                .toArray(Item[]::new);
        event.register(new DynamicFluidContainerModel.Colors(), buckets);
    }

    public static void registerMenuScreens() {
        safeRegister(ModMenus.ELECTRIC_FURNACE.get(), ElectricFurnaceScreen::new, "electric_furnace");
        safeRegister(ModMenus.ANVIL.get(), AnvilScreen::new, "anvil");
        safeRegister(ModMenus.PRESS.get(), PressScreen::new, "press");
        safeRegister(ModMenus.EPRESS.get(), EPressScreen::new, "epress");
        safeRegister(ModMenus.SHREDDER.get(), ShredderScreen::new, "shredder");
        safeRegister(ModMenus.MACHINE_CENTRIFUGE.get(), CentrifugeScreen::new, "machine_centrifuge");
        safeRegister(ModMenus.MACHINE_GASCENT.get(), GasCentScreen::new, "machine_gascent");
        safeRegister(ModMenus.MACHINE_FEL.get(), FelScreen::new, "machine_fel");
        safeRegister(ModMenus.MACHINE_SILEX.get(), SilexScreen::new, "machine_silex");
        safeRegister(ModMenus.MACHINE_CRYSTALLIZER.get(), CrystallizerScreen::new, "machine_crystallizer");
        safeRegister(ModMenus.MACHINE_MIXER.get(), MixerScreen::new, "machine_mixer");
        safeRegister(ModMenus.MACHINE_ARC_WELDER.get(), ArcWelderScreen::new, "machine_arc_welder");
        safeRegister(ModMenus.MACHINE_PUREX.get(), PurexScreen::new, "machine_purex");
        safeRegister(ModMenus.MACHINE_SOLDERING_STATION.get(), SolderingStationScreen::new, "machine_soldering_station");
        safeRegister(ModMenus.MACHINE_RTG.get(), RtgScreen::new, "machine_rtg");
        safeRegister(ModMenus.DI_FURNACE.get(), DiFurnaceScreen::new, "di_furnace");
        safeRegister(ModMenus.DI_FURNACE_RTG.get(), DiFurnaceRtgScreen::new, "di_furnace_rtg");
        safeRegister(ModMenus.BLAST_FURNACE.get(), BlastFurnaceScreen::new, "blast_furnace");
        safeRegister(ModMenus.ASSEMBLY_MACHINE.get(), AssemblyMachineScreen::new, "assembly_machine");
        safeRegister(ModMenus.CHEMICAL_PLANT.get(), ChemicalPlantScreen::new, "chemical_plant");
        safeRegister(ModMenus.HEATER_FIREBOX.get(), FireboxScreen::new, "heater_firebox");
        safeRegister(ModMenus.MACHINE_WOOD_BURNER.get(), WoodBurnerScreen::new, "machine_wood_burner");
        safeRegister(ModMenus.MACHINE_WELL.get(), OilWellScreen::new, "machine_well");
        safeRegister(ModMenus.MACHINE_PUMPJACK.get(), PumpjackScreen::new, "machine_pumpjack");
        safeRegister(ModMenus.MACHINE_REFINERY.get(), RefineryScreen::new, "machine_refinery");
        safeRegister(ModMenus.MACHINE_HYDROTREATER.get(), HydrotreaterScreen::new, "machine_hydrotreater");
        safeRegister(ModMenus.MACHINE_CATALYTIC_REFORMER.get(), CatalyticReformerScreen::new, "machine_catalytic_reformer");
        safeRegister(ModMenus.MACHINE_VACUUM_DISTILL.get(), VacuumDistillScreen::new, "machine_vacuum_distill");
        safeRegister(ModMenus.MACHINE_TURBINE.get(), TurbineScreen::new, "machine_turbine");
        safeRegister(ModMenus.MACHINE_SIREN.get(), MachineSirenScreen::new, "machine_siren");
        safeRegister(ModMenus.CRANE_INSERTER.get(), CraneInserterScreen::new, "crane_inserter");
        safeRegister(ModMenus.CRANE_EXTRACTOR.get(), CraneExtractorScreen::new, "crane_extractor");
        safeRegister(ModMenus.MACHINE_BATTERY.get(), MachineBatteryScreen::new, "machine_battery");
        safeRegister(ModMenus.FLUID_BARREL.get(), FluidBarrelScreen::new, "fluid_barrel");
        safeRegister(ModMenus.DIESEL_GENERATOR.get(), DieselGeneratorScreen::new, "diesel_generator");
        safeRegister(ModMenus.COMBUSTION_GENERATOR.get(), CombustionGeneratorScreen::new, "combustion_generator");
        safeRegister(ModMenus.MISSILE_ASSEMBLY.get(), MissileAssemblyScreen::new, "machine_missile_assembly");
        safeRegister(ModMenus.LAUNCH_PAD.get(), LaunchPadScreen::new, "launch_pad");
        safeRegister(ModMenus.LAUNCH_PAD_RUSTED.get(), LaunchPadRustedScreen::new, "launch_pad_rusted");
        safeRegister(ModMenus.COMPACT_LAUNCHER.get(), CompactLauncherScreen::new, "compact_launcher");
        safeRegister(ModMenus.LAUNCH_TABLE.get(), LaunchTableScreen::new, "launch_table");
        safeRegister(ModMenus.RADAR_NT.get(), RadarNTSlotsScreen::new, "machine_radar");
        safeRegister(ModMenus.NUKE_BOY.get(), NukeBoyScreen::new, "nuke_boy");
        safeRegister(ModMenus.NUKE_MAN.get(), NukeManScreen::new, "nuke_man");
        safeRegister(ModMenus.NUKE_GADGET.get(), NukeGadgetScreen::new, "nuke_gadget");
        safeRegister(ModMenus.NUKE_MIKE.get(), NukeMikeScreen::new, "nuke_mike");
        safeRegister(ModMenus.NUKE_TSAR.get(), NukeTsarScreen::new, "nuke_tsar");
        safeRegister(ModMenus.NUKE_FLEIJA.get(), NukeFleijaScreen::new, "nuke_fleija");
        safeRegister(ModMenus.NUKE_SOLINIUM.get(), NukeSoliniumScreen::new, "nuke_solinium");
        safeRegister(ModMenus.NUKE_FSTBMB.get(), NukeFstbmbScreen::new, "nuke_fstbmb");
        safeRegister(ModMenus.NUKE_N2.get(), NukeN2Screen::new, "nuke_n2");
        safeRegister(ModMenus.NUKE_PROTOTYPE.get(), NukePrototypeScreen::new, "nuke_prototype");
        safeRegister(ModMenus.NUKE_CUSTOM.get(), NukeCustomScreen::new, "nuke_custom");
        safeRegister(ModMenus.BOMB_MULTI.get(), BombMultiScreen::new, "bomb_multi");
        safeRegister(ModMenus.STORAGE_CRATE.get(), StorageCrateScreen::new, "storage_crate");
        safeRegister(ModMenus.STORAGE_CRATE_LARGE.get(), StorageCrateScreen::new, "storage_crate_large");
        safeRegister(ModMenus.FILE_CABINET.get(), FileCabinetScreen::new, "file_cabinet");
        safeRegister(ModMenus.SAFE.get(), SafeScreen::new, "safe");
        HbmNuclearTechMod.LOGGER.info("All HBM MenuScreens registered");
    }

    private static <M extends AbstractContainerMenu, U extends AbstractContainerScreen<M>> void safeRegister(
            MenuType<? extends M> type,
            MenuScreens.ScreenConstructor<M, U> ctor,
            String name) {
        try {
            MenuScreens.register(type, ctor);
            HbmNuclearTechMod.LOGGER.info("MenuScreen OK: {}", name);
        } catch (IllegalArgumentException | IllegalStateException e) {
            HbmNuclearTechMod.LOGGER.info("MenuScreen already present: {}", name);
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PRIMED_BOMB.get(), PrimedBombRenderer::new);
        event.registerEntityRenderer(ModEntities.NUKE_EXPLOSION_MK5.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.NUKE_EXPLOSION_MK3.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.BALEFIRE_BLAST.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.CLOUD_FLEIJA.get(), RenderNukeCloud.Fleija::new);
        event.registerEntityRenderer(ModEntities.CLOUD_FLEIJA_RAINBOW.get(), RenderNukeCloud.Rainbow::new);
        event.registerEntityRenderer(ModEntities.CLOUD_SOLINIUM.get(), RenderNukeCloud.Solinium::new);
        event.registerEntityRenderer(ModEntities.NUKE_TOREX.get(), RenderTorex::new);
        event.registerEntityRenderer(ModEntities.FALLOUT_RAIN.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.EMP_BLAST.get(), RenderEMPBlast::new);
        event.registerEntityRenderer(ModEntities.FIREWORKS.get(), RenderFireworks::new);
        event.registerEntityRenderer(ModEntities.MOVING_ITEM.get(), RenderMovingItem::new);
        event.registerEntityRenderer(ModEntities.SHRAPNEL.get(), RenderBombProjectiles.Shrapnel::new);
        event.registerEntityRenderer(ModEntities.RUBBLE.get(), RenderRubble::new);
        event.registerEntityRenderer(ModEntities.CLUSTER_BOMBLET.get(), RenderBombProjectiles.ClusterBomblet::new);
        event.registerEntityRenderer(ModEntities.BOMBLET_ZETA.get(), RenderBombProjectiles.BombletZeta::new);
        event.registerEntityRenderer(ModEntities.MISSILE_GENERIC.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_STRONG.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_INCENDIARY.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_CLUSTER.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_BUSTER.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_TAINT.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_MICRO.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_BHOLE.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_SCHRABIDIUM.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_EMP.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_EMP_STRONG.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_DECOY.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_STEALTH.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_BURST.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_INFERNO.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_RAIN.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_DRILL.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_SHUTTLE.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_NUCLEAR.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_NUCLEAR_CLUSTER.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_VOLCANO.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_DOOMSDAY.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_DOOMSDAY_RUSTED.get(), RenderMissile::new);
        event.registerEntityRenderer(ModEntities.MISSILE_CUSTOM.get(), RenderMissileCustom::new);
        event.registerEntityRenderer(ModEntities.MISSILE_ANTI_BALLISTIC.get(), RenderMissileAntiBallistic::new);
        event.registerEntityRenderer(ModEntities.BLACK_HOLE.get(), RenderBlackHole::new);
        event.registerEntityRenderer(ModEntities.MIST.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.EMP_LOGIC.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.FALLING_NUKE.get(), RenderBombProjectiles.FallingNuke::new);
        event.registerEntityRenderer(ModEntities.BOMBER.get(), RenderBomber::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESS.get(), RenderPress::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_CENTRIFUGE.get(), RenderCentrifuge::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_GASCENT.get(), RenderGasCent::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_FEL.get(), RenderFel::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_SILEX.get(), RenderSilex::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_CRYSTALLIZER.get(), RenderCrystallizer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_MIXER.get(), RenderMixer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_ARC_WELDER.get(), RenderArcWelder::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_PUREX.get(), RenderPurex::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_SOLDERING_STATION.get(), RenderSolderingStation::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_RTG.get(), RenderRtg::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DIESEL_GENERATOR.get(), RenderDieselGen::new);
        event.registerBlockEntityRenderer(ModBlockEntities.EPRESS.get(), RenderEPress::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BLAST_FURNACE.get(), RenderBlastFurnace::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_MACHINE.get(), RenderAssemblyMachine::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHEMICAL_PLANT.get(), RenderChemicalPlant::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEATER_FIREBOX.get(), RenderFirebox::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_BOILER.get(), RenderBoiler::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_WOOD_BURNER.get(), RenderWoodBurner::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_WELL.get(), RenderDerrick::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_PUMPJACK.get(), RenderPumpjack::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_REFINERY.get(), RenderRefinery::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_FRACTION_TOWER.get(), RenderFractionTower::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FRACTION_SPACER.get(), RenderFractionSpacer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SILO_HATCH.get(), RenderSiloHatch::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_CATALYTIC_CRACKER.get(), RenderCatalyticCracker::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_HYDROTREATER.get(), RenderHydrotreater::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_CATALYTIC_REFORMER.get(), RenderCatalyticReformer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_VACUUM_DISTILL.get(), RenderVacuumDistill::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PYLON.get(), RenderPylon::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_PAD.get(), RenderLaunchPad::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_PAD_LARGE.get(), RenderLaunchPadLarge::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_PAD_RUSTED.get(), RenderLaunchPadRusted::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COMPACT_LAUNCHER.get(), RenderCompactLauncher::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_TABLE.get(), RenderLaunchTable::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MISSILE_ASSEMBLY.get(), RenderMissileAssembly::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADAR.get(), RenderRadar::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADAR_LARGE.get(), RenderRadarLarge::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADAR_SCREEN.get(), RenderRadarScreen::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRASHED_BOMB.get(), RenderCrashedBomb::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BROADCASTER_PC.get(), RenderBroadcaster::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIOREC.get(), RenderRadioRec::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DECO_LOOT.get(), RenderLoot::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LANDMINE.get(), RenderLandmine::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_BOY.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_MAN.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_GADGET.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_MIKE.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_TSAR.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_FLEIJA.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_SOLINIUM.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_FSTBMB.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_N2.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_PROTOTYPE.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_CUSTOM.get(), AssembledNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FILE_CABINET.get(), RenderFileCabinet::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // AP biome skins (same mesh, different MTL) for RenderLandmine.
        event.register(new ResourceLocation(RefStrings.MODID, "block/mine_ap_desert"));
        event.register(new ResourceLocation(RefStrings.MODID, "block/mine_ap_snow"));
        event.register(new ResourceLocation(RefStrings.MODID, "block/mine_ap_stone"));
        // Missile skins + silo pad (standalone bake for entity/BER render).
        for (ResourceLocation model : RenderMissile.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : MissilePartModels.allModels()) {
            event.register(model);
        }
        event.register(RenderBlackHole.MODEL_SPHERE);
        event.register(new ResourceLocation(RefStrings.MODID, "block/launch_pad_silo"));
        event.register(RenderLaunchPad.PAD_RUSTED_MODEL);
        event.register(RenderMissileAssembly.BENCH_MODEL);
        event.register(RenderMissileAssembly.STRUT_MODEL);
        event.register(RenderPress.BODY);
        event.register(RenderPress.HEAD);
        for (ResourceLocation model : RenderCentrifuge.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderGasCent.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderFel.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderSilex.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderCrystallizer.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderMixer.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderArcWelder.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderPurex.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderSolderingStation.allModels()) {
            event.register(model);
        }
        event.register(RenderRtg.GEN);
        event.register(RenderRtg.CONNECTOR);
        for (ResourceLocation model : RenderDieselGen.allModels()) {
            event.register(model);
        }
        event.register(RenderEPress.BODY);
        event.register(RenderEPress.HEAD);
        event.register(RenderBlastFurnace.MODEL);
        for (ResourceLocation model : RenderAssemblyMachine.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderChemicalPlant.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderFirebox.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderBoiler.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderWoodBurner.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderDerrick.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderPumpjack.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderRefinery.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderFractionTower.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderFractionSpacer.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderSiloHatch.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderCatalyticCracker.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderHydrotreater.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderCatalyticReformer.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderVacuumDistill.allModels()) {
            event.register(model);
        }
        for (ResourceLocation model : RenderPylon.allModels()) {
            event.register(model);
        }
    }
}
