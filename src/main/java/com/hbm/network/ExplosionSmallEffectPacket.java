package com.hbm.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C — legacy {@code ExplosionSmallCreator} / {@code ExplosionEffectWeapon} packet.
 */
public final class ExplosionSmallEffectPacket implements PacketBase {
    private final double x;
    private final double y;
    private final double z;
    private final int cloudCount;
    private final float cloudScale;
    private final float cloudSpeedMult;

    public ExplosionSmallEffectPacket(double x, double y, double z,
                                      int cloudCount, float cloudScale, float cloudSpeedMult) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cloudCount = cloudCount;
        this.cloudScale = cloudScale;
        this.cloudSpeedMult = cloudSpeedMult;
    }

    /** Legacy weapon preset used by cluster submunitions: {@code (10, 2.5F, 1F)}. */
    public static ExplosionSmallEffectPacket weapon(double x, double y, double z) {
        return new ExplosionSmallEffectPacket(x, y, z, 10, 2.5F, 1.0F);
    }

    public static ExplosionSmallEffectPacket decode(FriendlyByteBuf buf) {
        return new ExplosionSmallEffectPacket(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readVarInt(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeVarInt(cloudCount);
        buf.writeFloat(cloudScale);
        buf.writeFloat(cloudSpeedMult);
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
        com.hbm.client.particle.ClientExplosionEffects.composeEffectSmallWeapon(
                x, y, z, cloudCount, cloudScale, cloudSpeedMult);
    }
}
