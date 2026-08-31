package com.hbm.client.sound;

import com.hbm.blockentity.machine.LaunchPadLargeBlockEntity;
import com.hbm.client.particle.ClientMissileParticles;
import com.hbm.client.particle.ParticleCoolingTower;
import com.hbm.handler.LaunchPadFormFactor;
import com.hbm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class ClientLaunchPadSounds {
    private static final Map<BlockPos, PadLoopSound> LIFT = new HashMap<>();
    private static final Map<BlockPos, PadLoopSound> ERECTOR = new HashMap<>();

    private ClientLaunchPadSounds() {
    }

    public static void tickLarge(Level level, BlockPos pos, LaunchPadLargeBlockEntity pad) {
        if (!(level instanceof ClientLevel client)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (pad.isRemoved()) {
            LIFT.remove(pos);
            ERECTOR.remove(pos);
            return;
        }
        keep(mc, LIFT, pos, pad, true, pad.liftMoving, ModSounds.DOOR_WGH_START.get(), 0.75F);
        keep(mc, ERECTOR, pos, pad, false, pad.erectorMoving, ModSounds.DOOR_GARAGE_MOVE.get(), 1.5F);

        if (pad.erected && pad.formFactor >= 0 && pad.formFactor < LaunchPadFormFactor.values().length) {
            LaunchPadFormFactor form = LaunchPadFormFactor.values()[pad.formFactor];
            if ((form == LaunchPadFormFactor.HUGE || form == LaunchPadFormFactor.ATLAS)
                    && pad.getOxidizerTank().getFluidAmount() > 0) {
                for (int i = 0; i < 3; i++) {
                    ParticleCoolingTower fx = new ParticleCoolingTower(
                            client,
                            pos.getX() + 0.5D + client.random.nextGaussian() * 0.5D,
                            pos.getY() + 2.0D,
                            pos.getZ() + 0.5D + client.random.nextGaussian() * 0.5D);
                    fx.setLift(0.0F);
                    fx.setBaseScale(0.5F);
                    fx.setMaxScale(2.0F);
                    fx.setLife(70 + client.random.nextInt(30));
                    fx.noWind();
                    fx.setStrafe(0.05F);
                    fx.alphaMod(2.0F);
                    mc.particleEngine.add(fx);
                }
            }
        }

        if (ClientMissileParticles.hasMissileNearPad(client, pos)) {
            ClientMissileParticles.spawnLaunchSmoke(pos);
        }
    }

    private static void keep(Minecraft mc, Map<BlockPos, PadLoopSound> map, BlockPos pos,
                               LaunchPadLargeBlockEntity pad, boolean lift, boolean moving,
                               net.minecraft.sounds.SoundEvent sound, float volume) {
        PadLoopSound current = map.get(pos);
        if (current != null && !mc.getSoundManager().isActive(current)) {
            map.remove(pos);
            current = null;
        }
        if (moving) {
            if (current == null) {
                PadLoopSound next = new PadLoopSound(pad, sound, lift, volume);
                map.put(pos.immutable(), next);
                mc.getSoundManager().play(next);
            }
        } else if (current != null) {
            map.remove(pos);
        }
    }
}
