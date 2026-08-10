package com.hbm.blockentity.bomb;

import com.hbm.entity.item.EntityFireworks;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Fireworks launcher state (legacy {@code TileEntityFireworks}).
 */
public class FireworksBlockEntity extends BlockEntity {
    private int color = 0xFF0000;
    private String message = "NUCLEAR TECH";
    private int charges;

    private int index;
    private int delay;

    public FireworksBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIREWORKS.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FireworksBlockEntity be) {
        if (!level.hasNeighborSignal(pos) || be.message.isEmpty() || be.charges <= 0) {
            be.delay = 0;
            be.index = 0;
            return;
        }

        be.delay--;
        if (be.delay > 0) {
            return;
        }

        be.delay = 30;
        int character = be.message.charAt(be.index);
        int mod = be.index % 9;
        double offX = (mod / 3 - 1) * 0.3125D;
        double offZ = (mod % 3 - 1) * 0.3125D;

        double x = pos.getX() + 0.5D + offX;
        double y = pos.getY() + 1.5D;
        double z = pos.getZ() + 0.5D + offZ;

        EntityFireworks rocket = new EntityFireworks(level, x, y, z, be.color, character);
        level.addFreshEntity(rocket);
        level.playSound(null, x, y, z, ModSounds.ROCKET_FLAME.get(), SoundSource.BLOCKS, 3.0F, 1.0F);

        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLAME, x, pos.getY() + 1.125D, z, 8, 0.05D, 0.05D, 0.05D, 0.01D);
        }

        be.charges--;
        be.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);

        be.index++;
        if (be.index >= be.message.length()) {
            be.index = 0;
            be.delay = 100;
        }
    }

    public void addCharges(int amount) {
        charges += Math.max(0, amount);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getCharges() {
        return charges;
    }

    public void setColor(int color) {
        this.color = color & 0xFFFFFF;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getColor() {
        return color;
    }

    public void setMessage(String message) {
        this.message = message == null || message.isEmpty() ? "NUCLEAR TECH" : message;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public String getMessage() {
        return message;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("charges", charges);
        tag.putInt("color", color);
        tag.putString("message", message);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        charges = tag.getInt("charges");
        color = tag.getInt("color");
        message = tag.getString("message");
        if (message.isEmpty()) {
            message = "NUCLEAR TECH";
        }
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
