package com.hbm.blockentity.machine;

import com.hbm.registry.ModBlockEntities;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.tileentity.network.RTTYSystem.RTTYChannel;
import com.hbm.util.NoteBuilder;
import com.hbm.util.NoteBuilder.Hit;
import com.hbm.util.NoteBuilder.Instrument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/** 1.7 {@code TileEntityRadioRec}. */
public class RadioRecBlockEntity extends BlockEntity {
    private String channel = "";
    private boolean on;

    public RadioRecBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIOREC.get(), pos, state);
    }

    public String channel() {
        return channel;
    }

    public boolean isOn() {
        return on;
    }

    public void setChannel(String channel) {
        this.channel = channel == null ? "" : channel;
        sync();
    }

    public void setOn(boolean on) {
        this.on = on;
        sync();
    }

    public boolean hasPermission(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) < 256.0D;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioRecBlockEntity be) {
        if (!be.on || be.channel.isEmpty()) {
            return;
        }
        RTTYChannel chan = RTTYSystem.listen(level, be.channel);
        if (chan == null || chan.timeStamp != level.getGameTime() - 1L || chan.signal == null) {
            return;
        }
        Hit[] notes = NoteBuilder.translate(String.valueOf(chan.signal));
        for (Hit hit : notes) {
            int noteId = hit.note().ordinal() + hit.octave().ordinal() * 12;
            float pitch = (float) Math.pow(2.0D, (noteId - 12) / 12.0D);
            level.playSound(null, pos, instrumentSound(hit.instrument()), SoundSource.RECORDS, 3.0F, pitch);
        }
    }

    private static SoundEvent instrumentSound(Instrument instrument) {
        return switch (instrument) {
            case BASSDRUM -> SoundEvents.NOTE_BLOCK_BASEDRUM.value();
            case SNARE -> SoundEvents.NOTE_BLOCK_SNARE.value();
            case CLICKS -> SoundEvents.NOTE_BLOCK_HAT.value();
            case BASSGUITAR -> SoundEvents.NOTE_BLOCK_BASS.value();
            default -> SoundEvents.NOTE_BLOCK_HARP.value();
        };
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = worldPosition;
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 2.0D, pos.getZ() + 1.0D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("channel", channel);
        tag.putBoolean("isOn", on);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        channel = tag.getString("channel");
        on = tag.getBoolean("isOn");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }
}
