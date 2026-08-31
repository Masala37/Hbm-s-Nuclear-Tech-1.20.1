package com.hbm.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C — legacy AuxParticle {@code type=smoke} packet (cloud / radial modes).
 */
public final class SmokeCloudEffectPacket implements PacketBase {
    public enum Mode {
        CLOUD,
        RADIAL
    }

    private final double x;
    private final double y;
    private final double z;
    private final int count;
    private final Mode mode;
    private final boolean playBang;

    public SmokeCloudEffectPacket(double x, double y, double z, int count, Mode mode) {
        this(x, y, z, count, mode, false);
    }

    public SmokeCloudEffectPacket(double x, double y, double z, int count, Mode mode, boolean playBang) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.count = count;
        this.mode = mode;
        this.playBang = playBang;
    }

    public static SmokeCloudEffectPacket cloud(double x, double y, double z, int count) {
        return new SmokeCloudEffectPacket(x, y, z, count, Mode.CLOUD, false);
    }

    public static SmokeCloudEffectPacket cloudBang(double x, double y, double z, int count) {
        return new SmokeCloudEffectPacket(x, y, z, count, Mode.CLOUD, true);
    }

    public static SmokeCloudEffectPacket decode(FriendlyByteBuf buf) {
        return new SmokeCloudEffectPacket(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readVarInt(), Mode.values()[buf.readByte()], buf.readBoolean());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeVarInt(count);
        buf.writeByte(mode.ordinal());
        buf.writeBoolean(playBang);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        com.hbm.client.particle.ClientExplosionEffects.spawnExSmoke(
                x, y, z, count, mode == Mode.RADIAL, playBang);
    }
}
