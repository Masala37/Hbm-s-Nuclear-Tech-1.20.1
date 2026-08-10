package com.hbm.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Forge 1.20.1 {@code PlayMessages.OpenContainer} runs on the network thread and can
 * swallow menu/screen construction failures with no log. This opens on the main thread instead.
 * <p>
 * Client UI is loaded reflectively so this class never hard-depends on Minecraft client types.
 */
public final class OpenMenuS2CPacket {
    private final int containerId;
    private final ResourceLocation menuType;
    private final Component title;
    private final BlockPos pos;

    public OpenMenuS2CPacket(int containerId, ResourceLocation menuType, Component title, BlockPos pos) {
        this.containerId = containerId;
        this.menuType = menuType;
        this.title = title;
        this.pos = pos.immutable();
    }

    public int containerId() {
        return containerId;
    }

    public ResourceLocation menuType() {
        return menuType;
    }

    public Component title() {
        return title;
    }

    public BlockPos pos() {
        return pos;
    }

    public static void encode(OpenMenuS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.containerId);
        buf.writeResourceLocation(packet.menuType);
        buf.writeComponent(packet.title);
        buf.writeBlockPos(packet.pos);
    }

    public static OpenMenuS2CPacket decode(FriendlyByteBuf buf) {
        return new OpenMenuS2CPacket(
                buf.readVarInt(),
                buf.readResourceLocation(),
                buf.readComponent(),
                buf.readBlockPos());
    }

    public static void handle(OpenMenuS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if (!ctx.getDirection().getReceptionSide().isClient()) {
                return;
            }
            try {
                Class.forName("com.hbm.client.OpenMenuClient")
                        .getMethod("open", OpenMenuS2CPacket.class)
                        .invoke(null, packet);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to open menu on client", e);
            }
        });
        ctx.setPacketHandled(true);
    }
}
