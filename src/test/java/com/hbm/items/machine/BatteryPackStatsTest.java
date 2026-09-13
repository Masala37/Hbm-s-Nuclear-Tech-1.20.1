package com.hbm.items.machine;

import com.hbm.energy.ItemChargeStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatteryPackStatsTest {
    @Test
    void redstoneBatteryMatches17DurationMath() {
        ItemBatteryPack.Pack pack = ItemBatteryPack.Pack.BATTERY_REDSTONE;
        assertEquals(1_800_000L, pack.capacity());
        assertEquals(1_000L, pack.chargeRate());
        assertEquals(100L, pack.dischargeRate());
        assertFalse(pack.capacitor());
    }

    @Test
    void lithiumBatteryIsTenTimesLeadDischarge() {
        assertEquals(1_000L, ItemBatteryPack.Pack.BATTERY_LEAD.dischargeRate());
        assertEquals(10_000L, ItemBatteryPack.Pack.BATTERY_LITHIUM.dischargeRate());
        assertEquals(18_000_000L, ItemBatteryPack.Pack.BATTERY_LEAD.capacity());
        assertEquals(180_000_000L, ItemBatteryPack.Pack.BATTERY_LITHIUM.capacity());
    }

    @Test
    void copperCapacitorIsThirtySecondsAtDischarge() {
        ItemBatteryPack.Pack pack = ItemBatteryPack.Pack.CAPACITOR_COPPER;
        assertTrue(pack.capacitor());
        assertEquals(600_000L, pack.capacity());
        assertEquals(1_000L, pack.chargeRate());
        assertEquals(1_000L, pack.dischargeRate());
    }

    @Test
    void shortNumberUses1kSteps() {
        assertEquals("100", ItemChargeStorage.shortNumber(100));
        assertEquals("1k", ItemChargeStorage.shortNumber(1_000));
        assertEquals("1M", ItemChargeStorage.shortNumber(1_000_000));
        assertEquals("1G", ItemChargeStorage.shortNumber(1_000_000_000L));
    }

    @Test
    void lateTiersExceedIntCapacity() {
        assertEquals(4_500_000_000L, ItemBatteryPack.Pack.BATTERY_SCHRABIDIUM.capacity());
        assertEquals(72_000_000_000L, ItemBatteryPack.Pack.BATTERY_QUANTUM.capacity());
        assertEquals(6_000_000_000L, ItemBatteryPack.Pack.CAPACITOR_SPARK.capacity());
        assertTrue(ItemBatteryPack.Pack.BATTERY_SCHRABIDIUM.capacity() > Integer.MAX_VALUE);
    }

    @Test
    void packLookupAcceptsLangKeyAndEnumName() {
        assertEquals(ItemBatteryPack.Pack.BATTERY_LITHIUM, ItemBatteryPack.Pack.byName("BATTERY_LITHIUM"));
        assertEquals(ItemBatteryPack.Pack.BATTERY_LITHIUM, ItemBatteryPack.Pack.byName("battery_lithium"));
        assertEquals(ItemBatteryPack.Pack.CAPACITOR_COPPER, ItemBatteryPack.Pack.byOrdinal(6));
    }
}
