package com.hbm.client;

import com.hbm.client.render.entity.PrimedBombRenderer;
import com.hbm.client.render.entity.RenderTorex;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PRIMED_BOMB.get(), PrimedBombRenderer::new);
        event.registerEntityRenderer(ModEntities.NUKE_EXPLOSION_MK5.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.NUKE_TOREX.get(), RenderTorex::new);
        event.registerEntityRenderer(ModEntities.FALLOUT_RAIN.get(), NoopRenderer::new);
    }
}
