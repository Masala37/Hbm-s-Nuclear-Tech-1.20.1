package com.hbm.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C — legacy {@code ExplosionCreator} {@code explosionLarge} particle packet (small preset).
 */
public final class ExplosionLargeEffectPacket implements PacketBase {
    public enum Preset {
        SMALL,
        STANDARD,
        LARGE
    }

    private final double x;
    private final double y;
    private final double z;
    private final Preset preset;

    public ExplosionLargeEffectPacket(double x, double y, double z, Preset preset) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.preset = preset;
    }

    public static ExplosionLargeEffectPacket decode(FriendlyByteBuf buf) {
        return new ExplosionLargeEffectPacket(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                Preset.values()[buf.readByte()]);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeByte(preset.ordinal());
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
        switch (preset) {
            case SMALL -> com.hbm.client.particle.ClientExplosionEffects.composeEffectSmall(x, y, z);
            case STANDARD -> com.hbm.client.particle.ClientExplosionEffects.composeEffectStandard(x, y, z);
            case LARGE -> com.hbm.client.particle.ClientExplosionEffects.composeEffectLarge(x, y, z);
        }
    }
}
