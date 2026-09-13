package com.hbm.network;

import com.hbm.blockentity.machine.SilexBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server 1.7 {@code AuxButtonPacket}: void SILEX contents. */
public final class SilexControlPacket {
    private final BlockPos pos;

    public SilexControlPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(SilexControlPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static SilexControlPacket decode(FriendlyByteBuf buf) {
        return new SilexControlPacket(buf.readBlockPos());
    }

    public static void handle(SilexControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
            if (be instanceof SilexBlockEntity silex) {
                silex.voidContents();
            }
        });
        ctx.setPacketHandled(true);
    }
}
