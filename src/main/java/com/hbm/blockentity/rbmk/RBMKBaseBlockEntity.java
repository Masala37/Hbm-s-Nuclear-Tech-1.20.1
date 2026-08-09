package com.hbm.blockentity.rbmk;

import com.hbm.blocks.rbmk.RBMKBaseBlock;
import com.hbm.config.RBMKConfig;
import com.hbm.rbmk.RBMKColumnType;
import com.hbm.rbmk.RBMKLidType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RBMKBaseBlockEntity extends BlockEntity {
    protected double heat;
    protected int craneIndicator;

    public RBMKBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public double getHeat() {
        return heat;
    }

    public void setHeat(double heat) {
        this.heat = heat;
        setChanged();
    }

    public RBMKColumnType getColumnType() {
        return RBMKColumnType.BLANK;
    }

    public RBMKLidType getLidType() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof RBMKBaseBlock rbmk) {
            return rbmk.getLidType(state);
        }
        return RBMKLidType.NONE;
    }

    public boolean hasLid() {
        return getLidType().hasLid();
    }

    public double maxHeat() {
        return RBMKConfig.maxHeat;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKBaseBlockEntity be) {
        if (be.craneIndicator > 0) {
            be.craneIndicator--;
            be.setChanged();
        }
        // Heat diffusion, neutron flux, and meltdown logic will be ported here.
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("heat", heat);
        tag.putInt("craneIndicator", craneIndicator);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heat = tag.getDouble("heat");
        craneIndicator = tag.getInt("craneIndicator");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
