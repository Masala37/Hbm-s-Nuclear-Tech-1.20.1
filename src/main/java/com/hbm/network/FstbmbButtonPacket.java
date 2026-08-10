package com.hbm.network;

import com.hbm.blockentity.bomb.NukeFstbmbBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server Balefire bomb GUI actions.
 * meta 0 = start countdown; meta 1 = set timer seconds (value).
 */
public final class FstbmbButtonPacket {
    private final BlockPos pos;
    private final int value;
    private final int meta;

    public FstbmbButtonPacket(BlockPos pos, int value, int meta) {
        this.pos = pos;
        this.value = value;
        this.meta = meta;
    }

    public static void encode(FstbmbButtonPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.value);
        buf.writeVarInt(packet.meta);
    }

    public static FstbmbButtonPacket decode(FriendlyByteBuf buf) {
        return new FstbmbButtonPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(FstbmbButtonPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.level() == null) {
                return;
            }
            if (!player.level().hasChunkAt(packet.pos)) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D * 64.0D) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (!(be instanceof NukeFstbmbBlockEntity bomb)) {
                return;
            }
            if (packet.meta == 0) {
                bomb.startCountdown();
            } else if (packet.meta == 1) {
                bomb.setTimerSeconds(packet.value);
            }
        });
        ctx.setPacketHandled(true);
    }
}
