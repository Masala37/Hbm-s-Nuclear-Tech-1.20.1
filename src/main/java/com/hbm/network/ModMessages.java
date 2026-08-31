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
    private static final String PROTOCOL = "5";

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

        CHANNEL.messageBuilder(AssembleMissilePacket.class, nextId++)
                .encoder(AssembleMissilePacket::encode)
                .decoder(AssembleMissilePacket::decode)
                .consumerMainThread(AssembleMissilePacket::handle)
                .add();

        CHANNEL.messageBuilder(LaunchPadPacket.class, nextId++)
                .encoder(LaunchPadPacket::encode)
                .decoder(LaunchPadPacket::decode)
                .consumerMainThread(LaunchPadPacket::handle)
                .add();

        CHANNEL.messageBuilder(LaunchPadRustedPacket.class, nextId++)
                .encoder(LaunchPadRustedPacket::encode)
                .decoder(LaunchPadRustedPacket::decode)
                .consumerMainThread(LaunchPadRustedPacket::handle)
                .add();

        CHANNEL.messageBuilder(ExplosionLargeEffectPacket.class, nextId++)
                .encoder(ExplosionLargeEffectPacket::encode)
                .decoder(ExplosionLargeEffectPacket::decode)
                .consumerMainThread(ExplosionLargeEffectPacket::handle)
                .add();

        CHANNEL.messageBuilder(ExplosionSmallEffectPacket.class, nextId++)
                .encoder(ExplosionSmallEffectPacket::encode)
                .decoder(ExplosionSmallEffectPacket::decode)
                .consumerMainThread(ExplosionSmallEffectPacket::handle)
                .add();

        CHANNEL.messageBuilder(SmokeCloudEffectPacket.class, nextId++)
                .encoder(SmokeCloudEffectPacket::encode)
                .decoder(SmokeCloudEffectPacket::decode)
                .consumerMainThread(SmokeCloudEffectPacket::handle)
                .add();

        CHANNEL.messageBuilder(MukeEffectPacket.class, nextId++)
                .encoder(MukeEffectPacket::encode)
                .decoder(MukeEffectPacket::decode)
                .consumerMainThread(MukeEffectPacket::handle)
                .add();

        CHANNEL.messageBuilder(RbmkMushEffectPacket.class, nextId++)
                .encoder(RbmkMushEffectPacket::encode)
                .decoder(RbmkMushEffectPacket::decode)
                .consumerMainThread(RbmkMushEffectPacket::handle)
                .add();

        CHANNEL.messageBuilder(LivingPropsSyncPacket.class, nextId++)
                .encoder(LivingPropsSyncPacket::encode)
                .decoder(LivingPropsSyncPacket::decode)
                .consumerMainThread(LivingPropsSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(RadarNTSyncPacket.class, nextId++)
                .encoder(RadarNTSyncPacket::encode)
                .decoder(RadarNTSyncPacket::decode)
                .consumerMainThread(RadarNTSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(RadarScreenSyncPacket.class, nextId++)
                .encoder(RadarScreenSyncPacket::encode)
                .decoder(RadarScreenSyncPacket::decode)
                .consumerMainThread(RadarScreenSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(RadarControlPacket.class, nextId++)
                .encoder(RadarControlPacket::encode)
                .decoder(RadarControlPacket::decode)
                .consumerMainThread(RadarControlPacket::handle)
                .add();

        CHANNEL.messageBuilder(LaunchTablePadSizePacket.class, nextId++)
                .encoder(LaunchTablePadSizePacket::encode)
                .decoder(LaunchTablePadSizePacket::decode)
                .consumerMainThread(LaunchTablePadSizePacket::handle)
                .add();

        CHANNEL.messageBuilder(ItemDesignatorPacket.class, nextId++)
                .encoder(ItemDesignatorPacket::encode)
                .decoder(ItemDesignatorPacket::decode)
                .consumerMainThread(ItemDesignatorPacket::handle)
                .add();

        HbmNuclearTechMod.LOGGER.info("Registered HBM network channel hbm:main (protocol {})", PROTOCOL);
    }
}
