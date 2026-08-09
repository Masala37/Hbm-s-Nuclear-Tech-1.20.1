package com.hbm.network;

import com.hbm.HbmNuclearTechMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * No-op packet used to exercise channel registration during common setup.
 */
public final class PingPacket implements PacketBase {
    public PingPacket() {
    }

    public static PingPacket decode(FriendlyByteBuf buf) {
        return new PingPacket();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        // No payload.
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> HbmNuclearTechMod.LOGGER.debug("Received HBM ping packet on {}", ctx.getDirection()));
        ctx.setPacketHandled(true);
    }
}
