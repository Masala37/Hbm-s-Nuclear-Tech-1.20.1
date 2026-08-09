package com.hbm.main;

import com.hbm.registry.ModCreativeTabs;
import com.hbm.registry.ModFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModCreativeTabs.registerItemIcons();
            for (ModFluids.FluidEntry entry : ModFluids.entries()) {
                RegistryObject<LiquidBlock> block = entry.block;
                if (block != null) {
                    ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.translucent());
                }
            }
        });
    }
}
