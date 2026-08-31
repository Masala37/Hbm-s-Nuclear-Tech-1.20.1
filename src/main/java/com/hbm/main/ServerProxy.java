package com.hbm.main;

import com.hbm.config.FalloutConfigJSON;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.hazard.HazardRegistry;
import com.hbm.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
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

    /** Client-only: pad takeoff bang at a position. */
    public void playMissileTakeoffAt(double x, double y, double z) {
    }

    /** Client-only: flight contrail particles. */
    public void spawnMissileContrail(EntityMissileBaseNT missile) {
    }

    /** Client-only: black ABM exhaust. */
    public void spawnAbmContrail(com.hbm.entity.missile.EntityMissileAntiBallistic missile) {
    }

    /** Client-only: looping rumble that follows a live singularity. */
    public void playBlackHole(com.hbm.entity.effect.EntityBlackHole hole) {
    }

    /** Client-only: Torex shock-front nuclear boom. */
    public boolean tryPlayNuclearExplosion(double x, double y, double z, double hearRange) {
        return false;
    }

    /** Client-only: pad launch smoke while a missile is nearby. */
    public void tickLaunchPadSmoke(Level level, BlockPos pos) {
    }

    /** Client-only: compact launcher / launch table ground plumes. */
    public void tickCustomLauncherSmoke(Level level, BlockPos pos, float spread) {
    }

    /** Client-only: large pad erector loops + tower vapor. */
    public void tickLaunchPadLarge(Level level, BlockPos pos, Object pad) {
    }

    /** Client-only: looping machine rumble while a LIT block is running. */
    public void tickMachineLoop(Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                net.minecraft.world.level.block.state.properties.BooleanProperty lit,
                                net.minecraft.sounds.SoundEvent sound, float volume) {
    }

    public void openDesignatorScreen(Player player) {
    }
}
