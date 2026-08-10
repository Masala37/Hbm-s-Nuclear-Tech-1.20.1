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
    private static final String PROTOCOL = "3";

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

        CHANNEL.messageBuilder(OpenBlockMenuPacket.class, nextId++)
                .encoder(OpenBlockMenuPacket::encode)
                .decoder(OpenBlockMenuPacket::decode)
                .consumerMainThread(OpenBlockMenuPacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenMenuS2CPacket.class, nextId++)
                .encoder(OpenMenuS2CPacket::encode)
                .decoder(OpenMenuS2CPacket::decode)
                .consumerMainThread(OpenMenuS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(FstbmbButtonPacket.class, nextId++)
                .encoder(FstbmbButtonPacket::encode)
                .decoder(FstbmbButtonPacket::decode)
                .consumerMainThread(FstbmbButtonPacket::handle)
                .add();

        CHANNEL.messageBuilder(LivingPropsSyncPacket.class, nextId++)
                .encoder(LivingPropsSyncPacket::encode)
                .decoder(LivingPropsSyncPacket::decode)
                .consumerMainThread(LivingPropsSyncPacket::handle)
                .add();

        HbmNuclearTechMod.LOGGER.info("Registered HBM network channel hbm:main (protocol {})", PROTOCOL);
    }
}
