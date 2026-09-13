package com.hbm.network;

import com.hbm.blockentity.machine.MixerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server 1.7 {@code NBTControlPacket} recipe-index toggle.
 */
public final class MixerControlPacket {
    private final BlockPos pos;

    public MixerControlPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(MixerControlPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static MixerControlPacket decode(FriendlyByteBuf buf) {
        return new MixerControlPacket(buf.readBlockPos());
    }

    public static void handle(MixerControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
            if (be instanceof MixerBlockEntity mixer) {
                mixer.cycleRecipe();
            }
        });
        ctx.setPacketHandled(true);
    }
}
