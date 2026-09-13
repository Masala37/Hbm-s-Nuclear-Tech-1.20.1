package com.hbm.client.sound;

import com.hbm.blockentity.machine.ChemicalPlantBlockEntity;
import com.hbm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * 1.7.10 {@code NTMSounds.CHEMPLANT_LOOP} while the plant is processing and the player is nearby.
 */
public final class ClientChemplantSounds {
    private static final Map<BlockPos, Loop> LOOPS = new HashMap<>();
    private static final double HEAR_RANGE = 30.0D;

    private ClientChemplantSounds() {
    }

    public static void keep(BlockPos pos, boolean processing) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos key = pos.immutable();
        Loop current = LOOPS.get(key);
        if (current != null && !mc.getSoundManager().isActive(current)) {
            LOOPS.remove(key);
            current = null;
        }
        LocalPlayer player = mc.player;
        boolean hear = processing && player != null
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
                < HEAR_RANGE * HEAR_RANGE;
        if (hear) {
            if (current == null) {
                Loop next = new Loop(key);
                LOOPS.put(key, next);
                mc.getSoundManager().play(next);
            }
        } else if (current != null) {
            current.endSound();
            LOOPS.remove(key);
        }
    }

    private static final class Loop extends AbstractTickableSoundInstance {
        private final BlockPos pos;

        private Loop(BlockPos pos) {
            super(ModSounds.require("block.chemical_plant"), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            this.pos = pos.immutable();
            this.looping = true;
            this.delay = 0;
            this.pitch = 1.0F;
            this.volume = 1.0F;
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
            if (!(be instanceof ChemicalPlantBlockEntity plant) || be.isRemoved() || !plant.didProcess()) {
                stop();
            }
        }
    }
}
