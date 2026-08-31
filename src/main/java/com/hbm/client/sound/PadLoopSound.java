package com.hbm.client.sound;

import com.hbm.blockentity.machine.LaunchPadLargeBlockEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class PadLoopSound extends AbstractTickableSoundInstance {
    private final LaunchPadLargeBlockEntity pad;
    private final boolean lift;

    public PadLoopSound(LaunchPadLargeBlockEntity pad, SoundEvent sound, boolean lift, float volume) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.pad = pad;
        this.lift = lift;
        this.looping = true;
        this.volume = volume;
        this.pitch = 1.0F;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        BlockPos pos = pad.getBlockPos();
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY();
        this.z = pos.getZ() + 0.5D;
        this.delay = 0;
    }

    @Override
    public void tick() {
        if (pad.isRemoved()) {
            stop();
            return;
        }
        boolean moving = lift ? pad.liftMoving : pad.erectorMoving;
        if (!moving) {
            stop();
            return;
        }
        BlockPos pos = pad.getBlockPos();
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY();
        this.z = pos.getZ() + 0.5D;
    }
}
