package com.hbm.network;

import com.hbm.blockentity.machine.FluidBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server 1.7 {@code AuxButtonPacket} for steel barrel I/O mode.
 */
public final class FluidBarrelControlPacket {
    private final BlockPos pos;

    public FluidBarrelControlPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(FluidBarrelControlPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static FluidBarrelControlPacket decode(FriendlyByteBuf buf) {
        return new FluidBarrelControlPacket(buf.readBlockPos());
    }

    public static void handle(FluidBarrelControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
            if (be instanceof FluidBarrelBlockEntity barrel) {
                barrel.cycleMode();
            }
        });
        ctx.setPacketHandled(true);
    }
}
