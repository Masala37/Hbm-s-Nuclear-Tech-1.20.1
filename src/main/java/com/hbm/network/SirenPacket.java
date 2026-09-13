package com.hbm.network;

import com.hbm.client.sound.ClientSirenSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C — 1.7.10 {@code TESirenPacket}: track id plus whether the siren should be sounding.
 */
public final class SirenPacket {
    private final BlockPos pos;
    private final int id;
    private final boolean active;

    public SirenPacket(BlockPos pos, int id, boolean active) {
        this.pos = pos;
        this.id = id;
        this.active = active;
    }

    public static void encode(SirenPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.id);
        buf.writeBoolean(packet.active);
    }

    public static SirenPacket decode(FriendlyByteBuf buf) {
        return new SirenPacket(buf.readBlockPos(), buf.readInt(), buf.readBoolean());
    }

    public static void handle(SirenPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientSirenSounds.apply(packet.pos, packet.id, packet.active)));
        ctx.setPacketHandled(true);
    }
}
