package com.hbm.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C — legacy AuxParticle {@code type=muke} (wave + flash + mushroom clouds).
 */
public final class MukeEffectPacket implements PacketBase {
    private final double x;
    private final double y;
    private final double z;
    private final boolean balefire;

    public MukeEffectPacket(double x, double y, double z, boolean balefire) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.balefire = balefire;
    }

    public static MukeEffectPacket decode(FriendlyByteBuf buf) {
        return new MukeEffectPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(balefire);
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
        com.hbm.client.particle.ClientExplosionEffects.spawnMuke(x, y, z, balefire);
    }
}
