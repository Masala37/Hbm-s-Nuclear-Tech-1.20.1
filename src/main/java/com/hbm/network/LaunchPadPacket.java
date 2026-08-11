package com.hbm.network;

import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server: launch from pad GUI.
 */
public final class LaunchPadPacket {
    private final BlockPos pos;

    public LaunchPadPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(LaunchPadPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static LaunchPadPacket decode(FriendlyByteBuf buf) {
        return new LaunchPadPacket(buf.readBlockPos());
    }

    public static void handle(LaunchPadPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof LaunchPadBlockEntity pad) {
                if (pad.launch()) {
                    player.displayClientMessage(Component.literal("Missile launched"), true);
                } else {
                    player.displayClientMessage(pad.statusMessage(), true);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
