package com.hbm.client.sound;

import com.hbm.blockentity.machine.MachineSirenBlockEntity;
import com.hbm.items.machine.ItemCassette.TrackType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ClientSirenSounds {
    private ClientSirenSounds() {
    }

    public static void apply(BlockPos pos, int id, boolean active) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockEntity te = mc.level.getBlockEntity(pos);
        if (!(te instanceof MachineSirenBlockEntity)) {
            return;
        }

        SirenLoopSound.LIST.removeIf(SirenLoopSound::isStopped);
        SirenLoopSound sound = find(pos);
        if (active) {
            TrackType track = TrackType.getEnum(id);
            SoundEvent event = track.getSoundEvent();
            if (sound == null) {
                if (id > 0 && event != null) {
                    SirenLoopSound next = new SirenLoopSound(
                            event, pos, track.getType(), track.getSoundPath(), track.getVolume());
                    mc.getSoundManager().play(next);
                }
                return;
            }
            String path = track.getSoundPath();
            if (path != null && !path.equals(sound.getPath())) {
                sound.endSound();
                SirenLoopSound.LIST.remove(sound);
                if (id > 0 && event != null) {
                    SirenLoopSound next = new SirenLoopSound(
                            event, pos, track.getType(), path, track.getVolume());
                    mc.getSoundManager().play(next);
                }
                return;
            }
            sound.intendedVolume = track.getVolume();
        } else if (sound != null) {
            sound.endSound();
            SirenLoopSound.LIST.remove(sound);
        }
    }

    private static SirenLoopSound find(BlockPos pos) {
        for (SirenLoopSound sound : SirenLoopSound.LIST) {
            if (sound.getPos().equals(pos)) {
                return sound;
            }
        }
        return null;
    }
}
