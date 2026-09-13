package com.hbm.client.sound;

import com.hbm.handler.BroadcasterEffect;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

public final class ClientBroadcasterSounds {
    private static final Map<BlockPos, BroadcasterLoopSound> LOOPS = new HashMap<>();

    private ClientBroadcasterSounds() {
    }

    public static void keep(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos key = pos.immutable();
        BroadcasterLoopSound current = LOOPS.get(key);
        if (current != null && !mc.getSoundManager().isActive(current)) {
            LOOPS.remove(key);
            current = null;
        }
        boolean active = mc.level != null && mc.level.getBlockState(pos).is(ModBlocks.BROADCASTER_PC.get());
        if (active) {
            if (current == null) {
                SoundEvent event = ModSounds.require(BroadcasterEffect.trackPath(pos.getX(), pos.getY(), pos.getZ()));
                BroadcasterLoopSound next = new BroadcasterLoopSound(event, key);
                LOOPS.put(key, next);
                mc.getSoundManager().play(next);
            }
        } else if (current != null) {
            current.endSound();
            LOOPS.remove(key);
        }
    }
}
