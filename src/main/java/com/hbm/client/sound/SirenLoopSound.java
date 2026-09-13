package com.hbm.client.sound;

import com.hbm.blockentity.machine.MachineSirenBlockEntity;
import com.hbm.items.machine.ItemCassette.SoundType;
import com.hbm.sound.SirenFalloff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Client siren loop. Matches 1.7.10 {@code SoundLoopSiren}: no engine attenuation,
 * linear falloff from volume 2 at the source to 0 at {@code intendedVolume} blocks.
 */
public class SirenLoopSound extends AbstractTickableSoundInstance {
    public static final List<SirenLoopSound> LIST = new ArrayList<>();

    public float intendedVolume;
    public final SoundType type;
    private final BlockPos pos;
    private final String path;

    public SirenLoopSound(SoundEvent sound, BlockPos pos, SoundType type, String path, float intendedVolume) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.pos = pos.immutable();
        this.type = type;
        this.path = path;
        this.intendedVolume = intendedVolume;
        this.looping = type == SoundType.LOOP;
        this.delay = 0;
        this.pitch = 1.0F;
        this.volume = 2.0F;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY() + 0.5D;
        this.z = pos.getZ() + 0.5D;
        LIST.add(this);
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getPath() {
        return path;
    }

    public void endSound() {
        stop();
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            drop();
            return;
        }
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof MachineSirenBlockEntity) || be.isRemoved()) {
            drop();
            return;
        }
        LocalPlayer player = mc.player;
        if (player != null) {
            float distance = (float) Math.sqrt(player.distanceToSqr(x, y, z));
            this.volume = SirenFalloff.volumeAtDistance(distance, intendedVolume);
        } else {
            this.volume = intendedVolume;
        }
        this.looping = type == SoundType.LOOP;
    }

    private void drop() {
        stop();
        LIST.remove(this);
    }
}
