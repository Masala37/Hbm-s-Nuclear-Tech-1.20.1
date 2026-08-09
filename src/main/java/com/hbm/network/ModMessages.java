package com.hbm.network;

import com.hbm.HbmNuclearTechMod;
import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Thin networking scaffold. Legacy threaded custom codecs are not ported yet.
 */
public final class ModMessages {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RefStrings.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private static int nextId = 0;
    private static boolean registered;

    private ModMessages() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        CHANNEL.messageBuilder(PingPacket.class, nextId++)
                .encoder(PingPacket::encode)
                .decoder(PingPacket::decode)
                .consumerMainThread(PingPacket::handle)
                .add();

        HbmNuclearTechMod.LOGGER.info("Registered HBM network channel hbm:main (protocol {})", PROTOCOL);
    }
}
