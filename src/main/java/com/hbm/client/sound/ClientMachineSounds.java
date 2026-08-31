package com.hbm.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.HashMap;
import java.util.Map;

public final class ClientMachineSounds {
    private static final Map<BlockPos, MachineLoopSound> LOOPS = new HashMap<>();

    private ClientMachineSounds() {
    }

    public static void keep(BlockPos pos, Block block, BooleanProperty lit, SoundEvent sound, float volume) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos key = pos.immutable();
        MachineLoopSound current = LOOPS.get(key);
        if (current != null && !mc.getSoundManager().isActive(current)) {
            LOOPS.remove(key);
            current = null;
        }
        boolean active = mc.level != null
                && mc.level.getBlockState(pos).is(block)
                && mc.level.getBlockState(pos).getValue(lit);
        if (active) {
            if (current == null) {
                MachineLoopSound next = new MachineLoopSound(key, block, lit, sound, volume);
                LOOPS.put(key, next);
                mc.getSoundManager().play(next);
            }
        } else if (current != null) {
            LOOPS.remove(key);
        }
    }
}
