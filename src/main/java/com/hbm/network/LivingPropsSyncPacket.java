package com.hbm.network;

import com.hbm.capability.HbmLivingProps;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Syncs living radiation / digamma dose to the owning client.
 */
public final class LivingPropsSyncPacket implements PacketBase {
    private final int entityId;
    private final float radiation;
    private final float digamma;
    private final float radBuf;

    public LivingPropsSyncPacket(int entityId, float radiation, float digamma, float radBuf) {
        this.entityId = entityId;
        this.radiation = radiation;
        this.digamma = digamma;
        this.radBuf = radBuf;
    }

    public static LivingPropsSyncPacket decode(FriendlyByteBuf buf) {
        return new LivingPropsSyncPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(radiation);
        buf.writeFloat(digamma);
        buf.writeFloat(radBuf);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Entity entity = mc.level.getEntity(entityId);
        if (entity instanceof LivingEntity living) {
            HbmLivingProps.get(living).applyClientSync(radiation, digamma, radBuf);
            HbmLivingProps.get(living).applyDigammaHealth(living);
        }
    }
}
