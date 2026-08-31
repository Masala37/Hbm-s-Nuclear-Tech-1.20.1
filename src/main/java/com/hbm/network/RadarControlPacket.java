package com.hbm.network;

import com.hbm.blockentity.machine.RadarNTBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class RadarControlPacket {
    private final BlockPos pos;
    private final CompoundTag data;

    public RadarControlPacket(BlockPos pos, CompoundTag data) {
        this.pos = pos;
        this.data = data;
    }

    public static void encode(RadarControlPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeNbt(packet.data);
    }

    public static RadarControlPacket decode(FriendlyByteBuf buf) {
        return new RadarControlPacket(buf.readBlockPos(), buf.readNbt());
    }

    public static void handle(RadarControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.level() == null || packet.data == null) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D)
                    > 64.0D * 64.0D) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof RadarNTBlockEntity radar) {
                radar.receiveControl(player, packet.data);
            }
        });
        ctx.setPacketHandled(true);
    }
}
