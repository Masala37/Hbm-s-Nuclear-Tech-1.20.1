package com.hbm.network;

import com.hbm.blockentity.machine.LaunchTableBlockEntity;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class LaunchTablePadSizePacket {
    private final BlockPos pos;
    private final int ordinal;

    public LaunchTablePadSizePacket(BlockPos pos, int ordinal) {
        this.pos = pos;
        this.ordinal = ordinal;
    }

    public static void encode(LaunchTablePadSizePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.ordinal);
    }

    public static LaunchTablePadSizePacket decode(FriendlyByteBuf buf) {
        return new LaunchTablePadSizePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(LaunchTablePadSizePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.level() == null || !player.level().hasChunkAt(packet.pos)) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D)
                    > 64.0D * 64.0D) {
                return;
            }
            PartSize[] values = PartSize.values();
            if (packet.ordinal < 0 || packet.ordinal >= values.length) {
                return;
            }
            PartSize size = values[packet.ordinal];
            if (size != PartSize.SIZE_10 && size != PartSize.SIZE_15 && size != PartSize.SIZE_20) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof LaunchTableBlockEntity table) {
                table.setPadSize(size);
            }
        });
        ctx.setPacketHandled(true);
    }
}
