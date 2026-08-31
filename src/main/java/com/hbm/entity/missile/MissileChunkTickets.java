package com.hbm.entity.missile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

/** Region tickets so missiles keep ticking after the pad chunk unloads. */
public final class MissileChunkTickets {
    private static final TicketType<ChunkPos> TICKET =
            TicketType.create("hbm_missile", Comparator.comparingLong(ChunkPos::toLong), 5);
    /** {@code 33 - 2 = 31}: entity-ticking at the ticket chunk. */
    private static final int ENTITY_TICKING = 2;

    private MissileChunkTickets() {
    }

    /** Current chunk plus the chunk this tick's motion lands in. */
    public static void keepFlight(Entity entity, double motionScale) {
        if (!(entity.level() instanceof ServerLevel server) || entity.isRemoved()) {
            return;
        }
        Vec3 pos = entity.position();
        Vec3 mot = entity.getDeltaMovement();
        force(server, chunkAt(pos.x, pos.z));
        force(server, chunkAt(pos.x + mot.x * motionScale, pos.z + mot.z * motionScale));
    }

    /** 3×3 around the interceptor. */
    public static void keepNeighbors(Entity entity) {
        if (!(entity.level() instanceof ServerLevel server) || entity.isRemoved()) {
            return;
        }
        ChunkPos center = entity.chunkPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                force(server, new ChunkPos(center.x + dx, center.z + dz));
            }
        }
    }

    private static void force(ServerLevel server, ChunkPos cp) {
        server.getChunkSource().addRegionTicket(TICKET, cp, ENTITY_TICKING, cp);
    }

    private static ChunkPos chunkAt(double x, double z) {
        return new ChunkPos(Mth.floor(x) >> 4, Mth.floor(z) >> 4);
    }
}
