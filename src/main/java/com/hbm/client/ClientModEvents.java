package com.hbm.client;

import com.hbm.HbmNuclearTechMod;
import com.hbm.client.screen.BombMultiScreen;
import com.hbm.client.screen.CombustionGeneratorScreen;
import com.hbm.client.screen.DieselGeneratorScreen;
import com.hbm.client.screen.ElectricFurnaceScreen;
import com.hbm.client.screen.FluidBarrelScreen;
import com.hbm.client.screen.LaunchPadScreen;
import com.hbm.client.screen.MachineBatteryScreen;
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
import com.hbm.client.screen.StorageCrateScreen;
import com.hbm.client.render.blockentity.AssembledNukeRenderer;
import com.hbm.client.render.blockentity.RenderCrashedBomb;
import com.hbm.client.render.blockentity.RenderLandmine;
import com.hbm.client.render.blockentity.RenderLaunchPad;
import com.hbm.client.render.entity.PrimedBombRenderer;
import com.hbm.client.render.entity.RenderBlackHole;
import com.hbm.client.render.entity.RenderBombProjectiles;
import com.hbm.client.render.entity.RenderBomber;
import com.hbm.client.render.entity.RenderEMPBlast;
import com.hbm.client.render.entity.RenderMissile;
import com.hbm.client.render.entity.RenderFireworks;
import com.hbm.client.render.entity.RenderNukeCloud;
import com.hbm.client.render.entity.RenderRubble;
import com.hbm.client.render.entity.RenderTorex;
import com.hbm.blocks.generic.SellafieldSlakedBlock;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModEntities;
import com.hbm.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientModEvents::registerMenuScreens);
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
    }

    public static void registerMenuScreens() {
        safeRegister(ModMenus.ELECTRIC_FURNACE.get(), ElectricFurnaceScreen::new, "electric_furnace");
        safeRegister(ModMenus.MACHINE_BATTERY.get(), MachineBatteryScreen::new, "machine_battery");
        safeRegister(ModMenus.FLUID_BARREL.get(), FluidBarrelScreen::new, "fluid_barrel");
        safeRegister(ModMenus.DIESEL_GENERATOR.get(), DieselGeneratorScreen::new, "diesel_generator");
        safeRegister(ModMenus.COMBUSTION_GENERATOR.get(), CombustionGeneratorScreen::new, "combustion_generator");
        safeRegister(ModMenus.MISSILE_ASSEMBLY.get(), MissileAssemblyScreen::new, "machine_missile_assembly");
        safeRegister(ModMenus.LAUNCH_PAD.get(), LaunchPadScreen::new, "launch_pad");
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
        event.registerEntityRenderer(ModEntities.BLACK_HOLE.get(), RenderBlackHole::new);
        event.registerEntityRenderer(ModEntities.MIST.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.EMP_LOGIC.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.FALLING_NUKE.get(), RenderBombProjectiles.FallingNuke::new);
        event.registerEntityRenderer(ModEntities.BOMBER.get(), RenderBomber::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_PAD.get(), RenderLaunchPad::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRASHED_BOMB.get(), RenderCrashedBomb::new);
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
        event.register(RenderBlackHole.MODEL_SPHERE);
        event.register(new ResourceLocation(RefStrings.MODID, "block/launch_pad_silo"));
    }
}
