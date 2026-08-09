package com.hbm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Minimal packet contract for the 1.20.1 SimpleChannel scaffold.
 */
public interface PacketBase {
    void encode(FriendlyByteBuf buf);

    void handle(Supplier<NetworkEvent.Context> context);
}
