package com.hbm.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class MachineLoopSound extends AbstractTickableSoundInstance {
    private final BlockPos pos;
    private final Block block;
    private final BooleanProperty lit;

    public MachineLoopSound(BlockPos pos, Block block, BooleanProperty lit, SoundEvent sound, float volume) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.pos = pos.immutable();
        this.block = block;
        this.lit = lit;
        this.looping = true;
        this.volume = volume;
        this.pitch = 1.0F;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY() + 0.5D;
        this.z = pos.getZ() + 0.5D;
        this.delay = 0;
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            stop();
            return;
        }
        BlockState state = mc.level.getBlockState(pos);
        if (!state.is(block) || !state.getValue(lit)) {
            stop();
        }
    }
}
