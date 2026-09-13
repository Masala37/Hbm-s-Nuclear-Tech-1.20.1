package com.hbm.network;

import com.hbm.blockentity.machine.RadioRecBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server 1.7 {@code NBTControlPacket} for the FM radio. */
public final class RadioRecControlPacket {
    private final BlockPos pos;
    private final boolean setChannel;
    private final String channel;
    private final boolean setOn;
    private final boolean on;

    public RadioRecControlPacket(BlockPos pos, String channel) {
        this.pos = pos;
        this.setChannel = true;
        this.channel = channel == null ? "" : channel;
        this.setOn = false;
        this.on = false;
    }

    public RadioRecControlPacket(BlockPos pos, boolean on) {
        this.pos = pos;
        this.setChannel = false;
        this.channel = "";
        this.setOn = true;
        this.on = on;
    }

    private RadioRecControlPacket(BlockPos pos, boolean setChannel, String channel, boolean setOn, boolean on) {
        this.pos = pos;
        this.setChannel = setChannel;
        this.channel = channel;
        this.setOn = setOn;
        this.on = on;
    }

    public static void encode(RadioRecControlPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.setChannel);
        if (packet.setChannel) {
            buf.writeUtf(packet.channel, 10);
        }
        buf.writeBoolean(packet.setOn);
        if (packet.setOn) {
            buf.writeBoolean(packet.on);
        }
    }

    public static RadioRecControlPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean setChannel = buf.readBoolean();
        String channel = setChannel ? buf.readUtf(10) : "";
        boolean setOn = buf.readBoolean();
        boolean on = setOn && buf.readBoolean();
        return new RadioRecControlPacket(pos, setChannel, channel, setOn, on);
    }

    public static void handle(RadioRecControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.level() == null || !player.level().hasChunkAt(packet.pos)) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 256.0D) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (!(be instanceof RadioRecBlockEntity radio) || !radio.hasPermission(player)) {
                return;
            }
            if (packet.setChannel) {
                radio.setChannel(packet.channel);
            }
            if (packet.setOn) {
                radio.setOn(packet.on);
            }
        });
        ctx.setPacketHandled(true);
    }
}
