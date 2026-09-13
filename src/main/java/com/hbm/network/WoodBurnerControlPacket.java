package com.hbm.network;

import com.hbm.blockentity.machine.WoodBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server 1.7 {@code NBTControlPacket} ({@code toggle} / {@code switch}) for the wood burner.
 */
public final class WoodBurnerControlPacket {
    public static final int TOGGLE = 0;
    public static final int SWITCH_MODE = 1;

    private final BlockPos pos;
    private final int action;

    public WoodBurnerControlPacket(BlockPos pos, int action) {
        this.pos = pos;
        this.action = action;
    }

    public static void encode(WoodBurnerControlPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.action);
    }

    public static WoodBurnerControlPacket decode(FriendlyByteBuf buf) {
        return new WoodBurnerControlPacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(WoodBurnerControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.level() == null || !player.level().hasChunkAt(packet.pos)) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (!(be instanceof WoodBurnerBlockEntity burner)) {
                return;
            }
            if (packet.action == TOGGLE) {
                burner.toggleOn();
            } else if (packet.action == SWITCH_MODE) {
                burner.toggleLiquidBurn();
            }
        });
        ctx.setPacketHandled(true);
    }
}
