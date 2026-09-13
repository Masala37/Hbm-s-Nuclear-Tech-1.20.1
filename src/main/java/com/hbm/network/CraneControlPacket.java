package com.hbm.network;

import com.hbm.blockentity.network.CraneExtractorBlockEntity;
import com.hbm.blockentity.network.CraneInserterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server 1.7 {@code NBTControlPacket} for crane GUIs.
 */
public final class CraneControlPacket {
    public static final int INSERTER_DESTROYER = 0;
    public static final int EXTRACTOR_WHITELIST = 1;
    public static final int EXTRACTOR_MAX_EJECT = 2;

    private final BlockPos pos;
    private final int action;

    public CraneControlPacket(BlockPos pos, int action) {
        this.pos = pos;
        this.action = action;
    }

    public static void encode(CraneControlPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.action);
    }

    public static CraneControlPacket decode(FriendlyByteBuf buf) {
        return new CraneControlPacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(CraneControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
            if (packet.action == INSERTER_DESTROYER && be instanceof CraneInserterBlockEntity inserter) {
                inserter.toggleDestroyer();
            } else if (packet.action == EXTRACTOR_WHITELIST && be instanceof CraneExtractorBlockEntity extractor) {
                extractor.toggleWhitelist();
            } else if (packet.action == EXTRACTOR_MAX_EJECT && be instanceof CraneExtractorBlockEntity extractor) {
                extractor.toggleMaxEject();
            }
        });
        ctx.setPacketHandled(true);
    }
}
