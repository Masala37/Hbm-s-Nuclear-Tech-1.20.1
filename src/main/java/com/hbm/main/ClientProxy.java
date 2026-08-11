package com.hbm.main;

import com.hbm.client.particle.ClientMissileParticles;
import com.hbm.client.sound.ClientMissileSounds;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModBulkContent;
import com.hbm.registry.ModCreativeTabs;
import com.hbm.registry.ModFluids;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

public class ClientProxy extends ServerProxy {
    @Override
    public void register(IEventBus modBus) {
        super.register(modBus);
        modBus.addListener(this::clientSetup);
    }

    @Override
    public void playMissileTakeoff(EntityMissileBaseNT missile) {
        ClientMissileSounds.playLaunch(missile);
    }

    @Override
    public void spawnMissileContrail(EntityMissileBaseNT missile) {
        ClientMissileParticles.spawnContrail(missile);
    }

    @Override
    public void tickLaunchPadSmoke(Level level, BlockPos pos) {
        if (level instanceof ClientLevel clientLevel
                && ClientMissileParticles.hasMissileNearPad(clientLevel, pos)) {
            ClientMissileParticles.spawnLaunchSmoke(pos);
        }
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModCreativeTabs.registerItemIcons();

            cutout(
                    ModBlocks.REINFORCED_GLASS.get(),
                    ModBlocks.GLASS_BORON.get(),
                    ModBlocks.GLASS_LEAD.get(),
                    ModBlocks.GLASS_TRINITITE.get(),
                    ModBlocks.GLASS_QUARTZ.get(),
                    ModBlocks.GLASS_POLARIZED.get(),
                    ModBlocks.GLASS_ASH.get(),
                    ModBlocks.REINFORCED_GLASS_PANE.get(),
                    ModBlocks.LADDER_STEEL.get(),
                    ModBlocks.LADDER_ALUMINIUM.get(),
                    ModBlocks.LADDER_GOLD.get(),
                    ModBlocks.LADDER_TITANIUM.get(),
                    ModBlocks.LADDER_COPPER.get(),
                    ModBlocks.LADDER_LEAD.get(),
                    ModBlocks.LADDER_COBALT.get(),
                    ModBlocks.LADDER_IRON.get(),
                    ModBlocks.LADDER_STURDY.get(),
                    ModBlocks.LADDER_TUNGSTEN.get(),
                    ModBlocks.STEEL_SCAFFOLD.get(),
                    ModBlocks.STEEL_BEAM.get(),
                    ModBlocks.STEEL_ROOF.get(),
                    ModBlocks.STEEL_WALL.get(),
                    ModBlocks.STEEL_WALL_ALT.get(),
                    ModBlocks.SCAFFOLD_STEEL.get(),
                    ModBlocks.SCAFFOLD_RED.get(),
                    ModBlocks.SCAFFOLD_WHITE.get(),
                    ModBlocks.SCAFFOLD_YELLOW.get(),
                    ModBlocks.SCAFFOLD_RUSTEDSTEEL.get(),
                    ModBlocks.BARBED_WIRE.get(),
                    ModBlocks.BARBED_WIRE_ACID.get(),
                    ModBlocks.BARBED_WIRE_FIRE.get(),
                    ModBlocks.BARBED_WIRE_POISON.get(),
                    ModBlocks.BARBED_WIRE_WITHER.get(),
                    ModBlocks.BARBED_WIRE_ULTRADEATH.get(),
                    ModBlocks.SPIKES.get(),
                    ModBlocks.FENCE_METAL.get(),
                    ModBlocks.BARREL_TAINT.get(),
                    ModBlocks.BARREL_IRON.get(),
                    ModBlocks.BARREL_STEEL.get(),
                    ModBlocks.BARREL_PLASTIC.get(),
                    ModBlocks.BARREL_CORRODED.get(),
                    ModBlocks.BARREL_RED.get(),
                    ModBlocks.BARREL_YELLOW.get(),
                    ModBlocks.BARREL_PINK.get(),
                    ModBlocks.BARREL_TCALLOY.get(),
                    ModBlocks.BARREL_VITRIFIED.get(),
                    ModBlocks.BARREL_LOX.get(),
                    ModBlocks.BARREL_ANTIMATTER.get(),
                    ModBlocks.TOASTER_IRON.get(),
                    ModBlocks.TOASTER_STEEL.get(),
                    ModBlocks.TOASTER_WOOD.get(),
                    ModBlocks.CRT_CLEAN.get(),
                    ModBlocks.CRT_BROKEN.get(),
                    ModBlocks.CRT_BSOD.get(),
                    ModBlocks.CRT_BLINKING.get(),
                    ModBlocks.LAMP_DEMON.get(),
                    ModBlocks.CAGE_LAMP.get(),
                    ModBlocks.CAGE_LAMP_OFF.get(),
                    ModBlocks.FLOOD_LAMP.get(),
                    ModBlocks.FLOOD_LAMP_OFF.get(),
                    ModBlocks.FLUORESCENT_LAMP.get(),
                    ModBlocks.FLUORESCENT_LAMP_OFF.get(),
                    ModBlocks.RTG.get(),
                    ModBlocks.RTG_CELL.get(),
                    ModBlocks.RTG_POLONIUM.get(),
                    ModBlocks.CHARGE_C4.get(),
                    ModBlocks.CHARGE_DYNAMITE.get(),
                    ModBlocks.CHARGE_SEMTEX.get(),
                    ModBlocks.CHARGE_MINER.get(),
                    ModBlocks.DET_CORD.get(),
                    ModBlocks.MINE_AP.get(),
                    ModBlocks.MINE_HE.get(),
                    ModBlocks.MINE_FAT.get(),
                    ModBlocks.MINE_NAVAL.get(),
                    ModBlocks.MINE_SHRAP.get(),
                    ModBlocks.BOMB_MULTI.get(),
                    ModBlocks.CRASHED_BOMB.get(),
                    ModBlocks.NUKE_PROTOTYPE.get(),
                    ModBlocks.NUKE_CUSTOM.get(),
                    ModBlocks.NUKE_BOY.get(),
                    ModBlocks.NUKE_MAN.get(),
                    ModBlocks.NUKE_GADGET.get(),
                    ModBlocks.NUKE_MIKE.get(),
                    ModBlocks.NUKE_TSAR.get(),
                    ModBlocks.NUKE_FLEIJA.get(),
                    ModBlocks.NUKE_SOLINIUM.get(),
                    ModBlocks.NUKE_FSTBMB.get(),
                    ModBlocks.NUKE_N2.get(),
                    ModBlocks.BALEFIRE.get(),
                    ModBlocks.FALLOUT.get(),
                    ModBlocks.DECO_COMPUTER.get(),
                    ModBlocks.DECO_SATELLITE_RECEIVER.get(),
                    ModBlocks.DECO_TAPE_RECORDER.get(),
                    ModBlocks.RED_CABLE.get(),
                    ModBlocks.RED_CABLE_CLASSIC.get(),
                    ModBlocks.RED_WIRE_COATED.get(),
                    ModBlocks.CABLE_SWITCH.get(),
                    ModBlocks.CABLE_DETECTOR.get(),
                    ModBlocks.CABLE_DIODE.get()
            );

            // Bulk deco OBJ / cross models (registered by id, not ModBlocks fields).
            for (RegistryObject<Block> entry : ModBulkContent.blocks()) {
                String path = entry.getId().getPath();
                if (path.startsWith("anvil_") || "geiger".equals(path)
                        || "chain".equals(path) || "chain_end".equals(path)) {
                    ItemBlockRenderTypes.setRenderLayer(entry.get(), RenderType.cutout());
                }
            }

            // Glass blocks look better translucent when fully opaque cubes.
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.REINFORCED_GLASS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.REINFORCED_LAMINATE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLASS_BORON.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLASS_LEAD.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLASS_TRINITITE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLASS_QUARTZ.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLASS_POLARIZED.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLASS_ASH.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLASS_POLONIUM.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLASS_URANIUM.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.REINFORCED_GLASS_PANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.REINFORCED_LAMINATE_PANE.get(), RenderType.translucent());

            for (ModFluids.FluidEntry entry : ModFluids.entries()) {
                RegistryObject<LiquidBlock> block = entry.block;
                if (block != null) {
                    ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.translucent());
                }
            }
        });
    }

    private static void cutout(Block... blocks) {
        for (Block block : blocks) {
            ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
        }
    }
}
