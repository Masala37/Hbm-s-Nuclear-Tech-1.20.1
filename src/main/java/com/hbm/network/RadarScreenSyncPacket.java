package com.hbm.network;

import api.hbm.entity.RadarEntry;
import com.hbm.blockentity.machine.RadarScreenBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class RadarScreenSyncPacket {
    private final BlockPos pos;
    private final boolean linked;
    private final int refX;
    private final int refY;
    private final int refZ;
    private final int range;
    private final List<RadarEntry> entries;

    public RadarScreenSyncPacket(BlockPos pos, boolean linked, int refX, int refY, int refZ, int range,
                                 List<RadarEntry> entries) {
        this.pos = pos;
        this.linked = linked;
        this.refX = refX;
        this.refY = refY;
        this.refZ = refZ;
        this.range = range;
        this.entries = entries;
    }

    public static void encode(RadarScreenSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.linked);
        buf.writeInt(packet.refX);
        buf.writeInt(packet.refY);
        buf.writeInt(packet.refZ);
        buf.writeInt(packet.range);
        buf.writeInt(packet.entries.size());
        for (RadarEntry entry : packet.entries) {
            entry.toBytes(buf);
        }
    }

    public static RadarScreenSyncPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean linked = buf.readBoolean();
        int refX = buf.readInt();
        int refY = buf.readInt();
        int refZ = buf.readInt();
        int range = buf.readInt();
        int count = buf.readInt();
        List<RadarEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            RadarEntry entry = new RadarEntry();
            entry.fromBytes(buf);
            entries.add(entry);
        }
        return new RadarScreenSyncPacket(pos, linked, refX, refY, refZ, range, entries);
    }

    public static void handle(RadarScreenSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> apply(packet)));
        ctx.setPacketHandled(true);
    }

    private static void apply(RadarScreenSyncPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }
        BlockEntity be = level.getBlockEntity(packet.pos);
        if (be instanceof RadarScreenBlockEntity screen) {
            screen.applySync(packet.linked, packet.refX, packet.refY, packet.refZ, packet.range, packet.entries);
        }
    }
}
