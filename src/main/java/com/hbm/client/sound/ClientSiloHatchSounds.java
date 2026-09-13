package com.hbm.client.sound;

import com.hbm.blocks.generic.SiloHatchLogic;
import com.hbm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * 1.7 silo hatch {@code AudioWrapper}: {@code door.wgh_big_start} while moving,
 * {@code door.wgh_big_stop} when the door finishes.
 */
public final class ClientSiloHatchSounds {
    private static final Map<BlockPos, Loop> LOOPS = new HashMap<>();
    private static final Map<BlockPos, Byte> LAST = new HashMap<>();

    private ClientSiloHatchSounds() {
    }

    public static void keep(BlockPos pos, byte state, float volume) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos key = pos.immutable();
        Loop current = LOOPS.get(key);
        if (current != null && !mc.getSoundManager().isActive(current)) {
            LOOPS.remove(key);
            current = null;
        }
        Byte prev = LAST.put(key, state);
        boolean moving = SiloHatchLogic.moving(state);
        if (moving) {
            if (current == null) {
                Loop next = new Loop(key, volume);
                LOOPS.put(key, next);
                mc.getSoundManager().play(next);
            }
        } else if (current != null) {
            current.endSound();
            LOOPS.remove(key);
        }
        if (prev != null && prev != state && mc.level != null) {
            if (prev == SiloHatchLogic.STATE_OPENING && state == SiloHatchLogic.STATE_OPEN
                    || prev == SiloHatchLogic.STATE_CLOSING && state == SiloHatchLogic.STATE_CLOSED) {
                mc.level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        ModSounds.require("door.wgh_big_stop"), SoundSource.BLOCKS, volume, 1.0F, false);
            }
        }
    }

    private static final class Loop extends AbstractTickableSoundInstance {
        private final BlockPos pos;

        private Loop(BlockPos pos, float volume) {
            super(ModSounds.require("door.wgh_big_start"), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            this.pos = pos.immutable();
            this.looping = true;
            this.delay = 0;
            this.pitch = 1.0F;
            this.volume = volume;
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            this.x = pos.getX() + 0.5D;
            this.y = pos.getY() + 0.5D;
            this.z = pos.getZ() + 0.5D;
        }

        private void endSound() {
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
            if (!(be instanceof com.hbm.blockentity.machine.SiloHatchBlockEntity hatch)
                    || be.isRemoved() || !SiloHatchLogic.moving(hatch.doorState())) {
                stop();
            }
        }
    }
}
