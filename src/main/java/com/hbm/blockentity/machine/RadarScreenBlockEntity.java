package com.hbm.blockentity.machine;

import api.hbm.entity.RadarEntry;
import com.hbm.network.ModMessages;
import com.hbm.network.RadarScreenSyncPacket;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class RadarScreenBlockEntity extends BlockEntity {
    public final List<RadarEntry> entries = new ArrayList<>();
    public int refX;
    public int refY;
    public int refZ;
    public int range;
    public boolean linked;

    public RadarScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADAR_SCREEN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadarScreenBlockEntity be) {
        be.sendSync();
        be.entries.clear();
        be.linked = false;
    }

    public void receiveFromRadar(RadarNTBlockEntity radar) {
        entries.clear();
        entries.addAll(radar.entries);
        refX = radar.getBlockPos().getX();
        refY = radar.getBlockPos().getY();
        refZ = radar.getBlockPos().getZ();
        range = radar.getRange();
        linked = true;
        sendSync();
    }

    private void sendSync() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        ModMessages.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
                        100.0D, server.dimension())),
                new RadarScreenSyncPacket(worldPosition, linked, refX, refY, refZ, range, new ArrayList<>(entries)));
    }

    public void applySync(boolean linked, int refX, int refY, int refZ, int range, List<RadarEntry> entries) {
        this.linked = linked;
        this.refX = refX;
        this.refY = refY;
        this.refZ = refZ;
        this.range = range;
        this.entries.clear();
        this.entries.addAll(entries);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 1.0D, worldPosition.getY(), worldPosition.getZ() - 1.0D,
                worldPosition.getX() + 2.0D, worldPosition.getY() + 2.0D, worldPosition.getZ() + 2.0D);
    }
}
