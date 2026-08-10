package com.hbm.blockentity.bomb;

import com.hbm.blocks.bomb.ChargeBlock;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Sticky charge timer / arm state (legacy {@code TileEntityCharge}).
 */
public class ChargeBlockEntity extends BlockEntity {
    private static final int[] TIMER_STEPS = {0, 100, 200, 300, 600, 1200, 3600, 6000};

    private boolean started;
    private int timer;

    public ChargeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHARGE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChargeBlockEntity be) {
        if (!(state.getBlock() instanceof ChargeBlock chargeBlock)) {
            return;
        }
        if (!be.started) {
            return;
        }

        be.timer--;
        if (be.timer % 20 == 0 && be.timer > 0) {
            level.playSound(null, pos, ModSounds.FSTBMB_PING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (be.timer <= 0) {
            be.started = false;
            be.timer = 0;
            be.setChanged();
            chargeBlock.explode(level, pos);
            return;
        }
        be.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getTimer() {
        return timer;
    }

    public void cycleTimer() {
        int next = TIMER_STEPS[0];
        for (int i = 0; i < TIMER_STEPS.length; i++) {
            if (timer == TIMER_STEPS[i]) {
                next = TIMER_STEPS[(i + 1) % TIMER_STEPS.length];
                break;
            }
        }
        // If current value is somehow off-cycle, reset to first armed step.
        boolean found = false;
        for (int step : TIMER_STEPS) {
            if (step == timer) {
                found = true;
                break;
            }
        }
        timer = found ? next : TIMER_STEPS[1];
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public String getMinutes() {
        String mins = Integer.toString(timer / 1200);
        return mins.length() == 1 ? "0" + mins : mins;
    }

    public String getSeconds() {
        String secs = Integer.toString((timer / 20) % 60);
        return secs.length() == 1 ? "0" + secs : secs;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("started", started);
        tag.putInt("timer", timer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        started = tag.getBoolean("started");
        timer = tag.getInt("timer");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
