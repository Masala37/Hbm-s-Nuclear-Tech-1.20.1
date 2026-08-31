package com.hbm.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class RbmkMushEffectPacket implements PacketBase {
    private final double x;
    private final double y;
    private final double z;
    private final float scale;

    public RbmkMushEffectPacket(double x, double y, double z, float scale) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.scale = scale;
    }

    public static RbmkMushEffectPacket decode(FriendlyByteBuf buf) {
        return new RbmkMushEffectPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(scale);
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
        com.hbm.client.particle.ClientExplosionEffects.spawnRbmkMush(x, y, z, scale);
    }
}
