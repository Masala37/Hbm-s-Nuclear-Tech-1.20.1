package com.hbm.handler;

import api.hbm.entity.IRadarDetectable;
import api.hbm.entity.IRadarDetectableNT;
import api.hbm.entity.IRadarDetectableNT.RadarScanParams;
import api.hbm.entity.RadarEntry;
import com.hbm.lib.RefStrings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class RadarScanSystem {
    public static final List<Entity> MATCHING = new ArrayList<>();

    private static final List<Function<ScanQuery, RadarEntry>> CONVERTERS = new ArrayList<>();

    static {
        CONVERTERS.add(q -> {
            if (q.entity instanceof IRadarDetectableNT detectable
                    && detectable.canBeSeenBy(q.radar)
                    && detectable.paramsApplicable(q.params)) {
                return new RadarEntry(detectable, q.entity, detectable.suppliesRedstone(q.params));
            }
            return null;
        });
        CONVERTERS.add(q -> {
            if (q.entity instanceof IRadarDetectable detectable && q.params.scanMissiles) {
                return new RadarEntry(detectable, q.entity);
            }
            return null;
        });
        CONVERTERS.add(q -> {
            if (q.entity instanceof Player player && q.params.scanPlayers) {
                return new RadarEntry(player);
            }
            return null;
        });
    }

    private RadarScanSystem() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        updateSystem();
    }

    public static void updateSystem() {
        MATCHING.clear();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof IRadarDetectableNT || entity instanceof IRadarDetectable || entity instanceof Player) {
                    MATCHING.add(entity);
                }
            }
        }
    }

    public static RadarEntry convert(Entity entity, Object radar, RadarScanParams params) {
        ScanQuery query = new ScanQuery(entity, radar, params);
        for (Function<ScanQuery, RadarEntry> converter : CONVERTERS) {
            RadarEntry entry = converter.apply(query);
            if (entry != null) {
                return entry;
            }
        }
        return null;
    }

    public record ScanQuery(Entity entity, Object radar, RadarScanParams params) {
    }
}
