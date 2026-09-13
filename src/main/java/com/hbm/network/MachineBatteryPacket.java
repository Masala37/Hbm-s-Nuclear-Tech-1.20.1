package com.hbm.network;

import com.hbm.blockentity.machine.MachineBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server 1.7 {@code AuxButtonPacket} ids 0–2 for {@code TileEntityMachineBattery}.
 */
public final class MachineBatteryPacket {
    private final BlockPos pos;
    private final int button;

    public MachineBatteryPacket(BlockPos pos, int button) {
        this.pos = pos;
        this.button = button;
    }

    public static void encode(MachineBatteryPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.button);
    }

    public static MachineBatteryPacket decode(FriendlyByteBuf buf) {
        return new MachineBatteryPacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(MachineBatteryPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
            if (be instanceof MachineBatteryBlockEntity battery) {
                battery.cycleButton(packet.button);
            }
        });
        ctx.setPacketHandled(true);
    }
}
