package com.hbm.network;

import com.hbm.blockentity.machine.SolderingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server 1.7 {@code NBTControlPacket} collision-prevention toggle.
 */
public final class SolderingControlPacket {
    private final BlockPos pos;

    public SolderingControlPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(SolderingControlPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static SolderingControlPacket decode(FriendlyByteBuf buf) {
        return new SolderingControlPacket(buf.readBlockPos());
    }

    public static void handle(SolderingControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.level() == null || !player.level().hasChunkAt(packet.pos)) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof SolderingStationBlockEntity station) {
                station.toggleCollisionPrevention();
            }
        });
        ctx.setPacketHandled(true);
    }
}
