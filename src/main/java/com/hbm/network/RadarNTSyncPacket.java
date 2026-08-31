package com.hbm.network;

import api.hbm.entity.RadarEntry;
import com.hbm.blockentity.machine.RadarNTBlockEntity;
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

public final class RadarNTSyncPacket {
    private final BlockPos pos;
    private final int power;
    private final boolean scanMissiles;
    private final boolean scanShells;
    private final boolean scanPlayers;
    private final boolean smartMode;
    private final boolean redMode;
    private final boolean showMap;
    private final boolean jammed;
    private final List<RadarEntry> entries;
    private final boolean clearMap;
    private final boolean mapSlice;
    private final int mapIndex;
    private final byte[] mapBytes;

    public RadarNTSyncPacket(BlockPos pos, int power, boolean scanMissiles, boolean scanShells, boolean scanPlayers,
                             boolean smartMode, boolean redMode, boolean showMap, boolean jammed,
                             List<RadarEntry> entries, boolean clearMap, boolean mapSlice, int mapIndex, byte[] mapBytes) {
        this.pos = pos;
        this.power = power;
        this.scanMissiles = scanMissiles;
        this.scanShells = scanShells;
        this.scanPlayers = scanPlayers;
        this.smartMode = smartMode;
        this.redMode = redMode;
        this.showMap = showMap;
        this.jammed = jammed;
        this.entries = entries;
        this.clearMap = clearMap;
        this.mapSlice = mapSlice;
        this.mapIndex = mapIndex;
        this.mapBytes = mapBytes;
    }

    public static void encode(RadarNTSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.power);
        buf.writeBoolean(packet.scanMissiles);
        buf.writeBoolean(packet.scanShells);
        buf.writeBoolean(packet.scanPlayers);
        buf.writeBoolean(packet.smartMode);
        buf.writeBoolean(packet.redMode);
        buf.writeBoolean(packet.showMap);
        buf.writeBoolean(packet.jammed);
        buf.writeInt(packet.entries.size());
        for (RadarEntry entry : packet.entries) {
            entry.toBytes(buf);
        }
        buf.writeBoolean(packet.clearMap);
        if (!packet.clearMap) {
            buf.writeBoolean(packet.mapSlice);
            if (packet.mapSlice) {
                buf.writeShort(packet.mapIndex);
                buf.writeBytes(packet.mapBytes);
            }
        }
    }

    public static RadarNTSyncPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int power = buf.readInt();
        boolean scanMissiles = buf.readBoolean();
        boolean scanShells = buf.readBoolean();
        boolean scanPlayers = buf.readBoolean();
        boolean smartMode = buf.readBoolean();
        boolean redMode = buf.readBoolean();
        boolean showMap = buf.readBoolean();
        boolean jammed = buf.readBoolean();
        int count = buf.readInt();
        List<RadarEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            RadarEntry entry = new RadarEntry();
            entry.fromBytes(buf);
            entries.add(entry);
        }
        boolean clearMap = buf.readBoolean();
        boolean mapSlice = false;
        int mapIndex = 0;
        byte[] mapBytes = new byte[0];
        if (!clearMap) {
            mapSlice = buf.readBoolean();
            if (mapSlice) {
                mapIndex = buf.readShort();
                mapBytes = new byte[100];
                buf.readBytes(mapBytes);
            }
        }
        return new RadarNTSyncPacket(pos, power, scanMissiles, scanShells, scanPlayers, smartMode, redMode, showMap,
                jammed, entries, clearMap, mapSlice, mapIndex, mapBytes);
    }

    public static void handle(RadarNTSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> apply(packet)));
        ctx.setPacketHandled(true);
    }

    private static void apply(RadarNTSyncPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }
        BlockEntity be = level.getBlockEntity(packet.pos);
        if (be instanceof RadarNTBlockEntity radar) {
            radar.applySync(packet.power, packet.scanMissiles, packet.scanShells, packet.scanPlayers,
                    packet.smartMode, packet.redMode, packet.showMap, packet.jammed, packet.entries,
                    packet.clearMap, packet.mapSlice, packet.mapIndex, packet.mapBytes);
        }
    }
}
