package com.hbm.network;

import com.hbm.blockentity.machine.LaunchPadRustedBlockEntity;
import com.hbm.inventory.menu.LaunchPadRustedMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class LaunchPadRustedPacket {
    private final BlockPos pos;

    public LaunchPadRustedPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(LaunchPadRustedPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static LaunchPadRustedPacket decode(FriendlyByteBuf buf) {
        return new LaunchPadRustedPacket(buf.readBlockPos());
    }

    public static void handle(LaunchPadRustedPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.level() == null || !player.level().hasChunkAt(packet.pos)) {
                return;
            }
            if (!(player.containerMenu instanceof LaunchPadRustedMenu menu) || !menu.stillValid(player)) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof LaunchPadRustedBlockEntity pad && menu.getBlockEntity() == pad && !pad.isRemoved()) {
                pad.tryRelease();
            }
        });
        ctx.setPacketHandled(true);
    }
}
