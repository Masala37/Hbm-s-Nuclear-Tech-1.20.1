package com.hbm.client.sound;

import com.hbm.blockentity.machine.BroadcasterBlockEntity;
import com.hbm.sound.BroadcasterFalloff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Client broadcast loop. Matches 1.7.10 {@code AudioDynamic}: no engine attenuation,
 * linear falloff from volume 25 at the source to 0 at 25 blocks.
 */
public class BroadcasterLoopSound extends AbstractTickableSoundInstance {
    private final BlockPos pos;

    public BroadcasterLoopSound(SoundEvent sound, BlockPos pos) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.pos = pos.immutable();
        this.looping = true;
        this.delay = 0;
        this.pitch = 1.0F;
        this.volume = BroadcasterFalloff.VOLUME;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY() + 0.5D;
        this.z = pos.getZ() + 0.5D;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void endSound() {
        stop();
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            stop();
            return;
        }
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof BroadcasterBlockEntity) || be.isRemoved()) {
            stop();
            return;
        }
        LocalPlayer player = mc.player;
        if (player != null) {
            float distance = (float) Math.sqrt(player.distanceToSqr(x, y, z));
            this.volume = BroadcasterFalloff.volumeAtDistance(distance);
        } else {
            this.volume = BroadcasterFalloff.VOLUME;
        }
    }
}
