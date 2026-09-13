package com.hbm.items.machine;

import com.hbm.registry.SoundEventCatalog;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemCassetteTest {
    @Test
    void preservesLegacyTrackOrderTitlesTypesVolumesAndColors() {
        List<ItemCassette.TrackType> tracks = Arrays.asList(ItemCassette.TrackType.values());
        assertEquals(List.of(
                        "NULL", "HATCH", "ATUOPILOT", "AMS_SIREN", "BLAST_DOOR", "APC_LOOP", "KLAXON",
                        "KLAXON_A", "KLAXON_B", "SIREN", "CLASSIC", "BANK_ALARM", "BEEP_SIREN",
                        "CONTAINER_ALARM", "SWEEP_SIREN", "STRIDER_SIREN", "AIR_RAID", "NOSTROMO_SIREN",
                        "EAS_ALARM", "APC_PASS", "RAZORTRAIN"),
                tracks.stream().map(Enum::name).toList());
        assertEquals(List.of(
                        " ",
                        "Hatch Siren",
                        "Autopilot Disconnected",
                        "AMS Siren",
                        "Blast Door Alarm",
                        "APC Siren",
                        "Klaxon",
                        "Vault Door Alarm",
                        "Security Alert",
                        "Standard Siren",
                        "Classic Siren",
                        "Bank Alarm",
                        "Beep Siren",
                        "Container Alarm",
                        "Sweep Siren",
                        "Missile Silo Siren",
                        "Air Raid Siren",
                        "Nostromo Self Destruct",
                        "EAS Alarm Screech",
                        "APC Pass",
                        "Razortrain Horn"),
                tracks.stream().map(ItemCassette.TrackType::getTrackTitle).toList());
        assertEquals(List.of(
                        ItemCassette.SoundType.SOUND,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.LOOP,
                        ItemCassette.SoundType.PASS,
                        ItemCassette.SoundType.SOUND),
                tracks.stream().map(ItemCassette.TrackType::getType).toList());
        assertEquals(List.of(
                        0, 250, 50, 50, 50, 50, 50, 50, 50, 100, 100, 100, 100, 100, 500, 500, 500, 100, 50, 50, 250),
                tracks.stream().map(track -> track.getVolume()).toList());
        assertEquals(List.of(
                        0, 3358839, 11908533, 15055698, 11665408, 3565216, 8421504, 0x8c810b, 0x76818e,
                        6684672, 0xc0cfe8, 3572962, 13882323, 14727839, 15592026, 11250586, 0xDF3795,
                        0x5dd800, 0xb3a8c1, 3422163, 7819501),
                tracks.stream().map(track -> track.getColor()).toList());
    }

    @Test
    void everyPlayableTrackMapsToACataloguedSound() {
        Set<String> catalog = Set.of(SoundEventCatalog.PATHS);
        for (ItemCassette.TrackType type : ItemCassette.TrackType.values()) {
            if (type == ItemCassette.TrackType.NULL) {
                continue;
            }
            assertTrue(catalog.contains(type.getSoundPath()), type.name() + " -> " + type.getSoundPath());
        }
        assertEquals(ItemCassette.TrackType.NULL, ItemCassette.TrackType.getEnum(-1));
        assertEquals(ItemCassette.TrackType.NULL, ItemCassette.TrackType.getEnum(99));
        assertEquals(ItemCassette.TrackType.HATCH, ItemCassette.TrackType.getEnum(1));
    }
}
