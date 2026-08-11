package com.hbm.main;

import com.hbm.config.FalloutConfigJSON;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.hazard.HazardRegistry;
import com.hbm.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerProxy {
    public void register(IEventBus modBus) {
        modBus.addListener(this::commonSetup);
    }

    protected void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModMessages::register);
        event.enqueueWork(FalloutConfigJSON::initialize);
        event.enqueueWork(HazardRegistry::registerItems);
        event.enqueueWork(com.hbm.entity.missile.MissileLaunchRegistry::bootstrap);
    }

    /** Client-only: takeoff + engine loop. */
    public void playMissileTakeoff(EntityMissileBaseNT missile) {
    }

    /** Client-only: flight contrail particles. */
    public void spawnMissileContrail(EntityMissileBaseNT missile) {
    }

    /** Client-only: pad launch smoke while a missile is nearby. */
    public void tickLaunchPadSmoke(Level level, BlockPos pos) {
    }
}
