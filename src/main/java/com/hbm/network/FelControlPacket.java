package com.hbm.network;

import com.hbm.blockentity.machine.FelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server 1.7 {@code AuxButtonPacket} meta 2: toggle FEL. */
public final class FelControlPacket {
    private final BlockPos pos;

    public FelControlPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(FelControlPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static FelControlPacket decode(FriendlyByteBuf buf) {
        return new FelControlPacket(buf.readBlockPos());
    }

    public static void handle(FelControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
            if (be instanceof FelBlockEntity fel) {
                fel.toggle();
            }
        });
        ctx.setPacketHandled(true);
    }
}
