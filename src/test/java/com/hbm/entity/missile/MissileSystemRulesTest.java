package com.hbm.entity.missile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissileSystemRulesTest {

    @Test
    void padLaunchesSize10And15Tops() {
        assertTrue(MissileSystemRules.isPadLaunchableFuselageTop(MissileSystemRules.SIZE_10));
        assertTrue(MissileSystemRules.isPadLaunchableFuselageTop(MissileSystemRules.SIZE_15));
        assertFalse(MissileSystemRules.isPadLaunchableFuselageTop(MissileSystemRules.SIZE_20));
        assertFalse(MissileSystemRules.isPadLaunchableFuselageTop("ANY"));
    }

    @Test
    void size15Over20AdapterIsNotPadLaunchable() {
        assertTrue(MissileSystemRules.isPadLaunchable(
                MissileSystemRules.SIZE_15, MissileSystemRules.SIZE_15));
        assertFalse(MissileSystemRules.isPadLaunchable(
                MissileSystemRules.SIZE_15, MissileSystemRules.SIZE_20));
        assertTrue(MissileSystemRules.thrusterFits(
                "KEROSENE", "KEROSENE", MissileSystemRules.SIZE_20, MissileSystemRules.SIZE_20));
        assertTrue(MissileSystemRules.warheadFits(
                MissileSystemRules.SIZE_15, MissileSystemRules.SIZE_15, 1.5F, 100.0F));
        assertTrue(MissileSystemRules.finsFit(MissileSystemRules.SIZE_20, MissileSystemRules.SIZE_20));
    }

    @Test
    void solidAndXenonNeedNoPadFuel() {
        assertEquals(0, MissileSystemRules.fuelCapacity(MissileSystemRules.FUEL_SOLID, 20_000.0F));
        assertEquals(0, MissileSystemRules.fuelCapacity(MissileSystemRules.FUEL_XENON, 5_000.0F));
        assertEquals(20_000, MissileSystemRules.fuelCapacity("KEROSENE", 20_000.0F));
        assertEquals(20_000, MissileSystemRules.fuelCapacity("HYDROGEN", 20_000.0F));
        assertEquals(20_000, MissileSystemRules.fuelCapacity("BALEFIRE", 20_000.0F));
    }

    @Test
    void warheadMustMatchTopAndLift() {
        assertTrue(MissileSystemRules.warheadFits(MissileSystemRules.SIZE_10, MissileSystemRules.SIZE_10, 1.5F, 10.0F));
        assertFalse(MissileSystemRules.warheadFits(MissileSystemRules.SIZE_15, MissileSystemRules.SIZE_10, 1.5F, 10.0F));
        assertFalse(MissileSystemRules.warheadFits(MissileSystemRules.SIZE_10, MissileSystemRules.SIZE_10, 20.0F, 10.0F));
    }

    @Test
    void thrusterMustMatchFuelAndBottomSize() {
        assertTrue(MissileSystemRules.thrusterFits("KEROSENE", "KEROSENE", MissileSystemRules.SIZE_10, MissileSystemRules.SIZE_10));
        assertFalse(MissileSystemRules.thrusterFits("SOLID", "KEROSENE", MissileSystemRules.SIZE_10, MissileSystemRules.SIZE_10));
        assertFalse(MissileSystemRules.thrusterFits("KEROSENE", "KEROSENE", MissileSystemRules.SIZE_10, MissileSystemRules.SIZE_15));
    }

    @Test
    void finsOptionalButMustMatchWhenPresent() {
        assertTrue(MissileSystemRules.finsFit(MissileSystemRules.SIZE_10, MissileSystemRules.SIZE_10));
        assertFalse(MissileSystemRules.finsFit(MissileSystemRules.SIZE_15, MissileSystemRules.SIZE_10));
        assertTrue(MissileSystemRules.canAssemble(1, 1, 1, 1, -1, true));
        assertFalse(MissileSystemRules.canAssemble(1, 1, 1, 1, 0, true));
        assertFalse(MissileSystemRules.canAssemble(1, 1, 1, 1, 1, false));
    }

    @Test
    void abmSkipsStealthAndArmsAfterClimb() {
        assertFalse(MissileSystemRules.abmTracks(true));
        assertTrue(MissileSystemRules.abmTracks(false));
        assertFalse(MissileSystemRules.abmArmed(39));
        assertTrue(MissileSystemRules.abmArmed(40));
        assertFalse(MissileSystemRules.abmProximityDetonate(9.0D, 39));
        assertTrue(MissileSystemRules.abmProximityDetonate(9.0D, 40));
        assertFalse(MissileSystemRules.abmProximityDetonate(10.0D, 40));
    }

    @Test
    void abmGivesUpWithoutATarget() {
        assertTrue(MissileSystemRules.abmGiveUp(601, false, 100.0D));
        assertFalse(MissileSystemRules.abmGiveUp(601, true, 100.0D));
        assertTrue(MissileSystemRules.abmGiveUp(10, false, 2001.0D));
        assertFalse(MissileSystemRules.abmGiveUp(10, true, 2001.0D));
    }

    @Test
    void padLaunchGates() {
        assertTrue(MissileSystemRules.canLaunch(true, true, MissileSystemRules.PAD_LAUNCH_COST, 0, true, true));
        assertTrue(MissileSystemRules.canLaunch(true, true, MissileSystemRules.PAD_LAUNCH_COST, 0, false, false));
        assertFalse(MissileSystemRules.canLaunch(true, true, MissileSystemRules.PAD_LAUNCH_COST, 0, true, false));
        assertFalse(MissileSystemRules.canLaunch(true, true, MissileSystemRules.PAD_LAUNCH_COST - 1, 0, false, false));
        assertFalse(MissileSystemRules.canLaunch(true, false, MissileSystemRules.PAD_LAUNCH_COST, 0, false, false));
        assertFalse(MissileSystemRules.canLaunch(true, true, MissileSystemRules.PAD_LAUNCH_COST, 1, false, false));
        assertFalse(MissileSystemRules.canLaunch(false, true, MissileSystemRules.PAD_LAUNCH_COST, 0, false, false));
    }

    @Test
    void scatterWithZeroChipHitsTarget() {
        assertArrayEquals(new int[] { 100, 200 },
                MissileSystemRules.scatterTarget(0, 0, 100, 200, 0.0F, 1.0F, 0.0F));
    }

    @Test
    void scatterWithFullChipAndZeroAngle() {
        assertArrayEquals(new int[] { 0, 0 },
                MissileSystemRules.scatterTarget(0, 0, 100, 0, 1.0F, 1.0F, 0.0F));
    }

    @Test
    void compactAcceptsSize10TopOnly() {
        assertTrue(MissileSystemRules.compactAcceptsTop(MissileSystemRules.SIZE_10));
        assertFalse(MissileSystemRules.compactAcceptsTop(MissileSystemRules.SIZE_15));
        assertFalse(MissileSystemRules.compactAcceptsTop(MissileSystemRules.SIZE_20));
    }

    @Test
    void tableMatchesSelectedPadSizeIncludingSize20() {
        assertTrue(MissileSystemRules.tableAcceptsTop(
                MissileSystemRules.SIZE_20, MissileSystemRules.SIZE_20));
        assertFalse(MissileSystemRules.tableAcceptsTop(
                MissileSystemRules.SIZE_20, MissileSystemRules.SIZE_10));
        assertTrue(MissileSystemRules.tableAcceptsTop(
                MissileSystemRules.SIZE_10, MissileSystemRules.SIZE_10));
        assertFalse(MissileSystemRules.tableAcceptsTop(
                MissileSystemRules.SIZE_15, MissileSystemRules.SIZE_20));
    }

    @Test
    void compactLaunchNeedsDesignatorAndTableDoesNot() {
        assertTrue(MissileSystemRules.compactCanLaunch(true, true, true, true));
        assertFalse(MissileSystemRules.compactCanLaunch(true, true, false, true));
        assertTrue(MissileSystemRules.tableCanLaunch(true, true, true));
        assertFalse(MissileSystemRules.tableCanLaunch(true, true, false));
        assertTrue(MissileSystemRules.launcherPowerReady(
                MissileSystemRules.LAUNCHER_LAUNCH_COST, MissileSystemRules.LAUNCHER_MAX_POWER));
        assertFalse(MissileSystemRules.launcherPowerReady(
                MissileSystemRules.LAUNCHER_LAUNCH_COST - 1, MissileSystemRules.LAUNCHER_MAX_POWER));
    }

    @Test
    void solidXenonSkipUnusedTanks() {
        assertEquals(1, MissileSystemRules.fuelLamp(true, 2500, 2500.0F));
        assertEquals(0, MissileSystemRules.fuelLamp(true, 2499, 2500.0F));
        assertEquals(-1, MissileSystemRules.fuelLamp(false, 0, 2500.0F));
        assertTrue(MissileSystemRules.hasLauncherFuel(-1, 1, -1));
        assertFalse(MissileSystemRules.hasLauncherFuel(0, -1, -1));
        assertTrue(MissileSystemRules.usesSolidFuel(MissileSystemRules.FUEL_SOLID));
        assertTrue(MissileSystemRules.usesLiquidFuel("KEROSENE"));
        assertTrue(MissileSystemRules.usesOxidizer("BALEFIRE"));
        assertFalse(MissileSystemRules.usesOxidizer(MissileSystemRules.FUEL_XENON));
        assertFalse(MissileSystemRules.usesLiquidFuel(MissileSystemRules.FUEL_SOLID));
    }

    @Test
    void launchTablePortPlateFootprint() {
        assertTrue(MissileSystemRules.launchTableIsPlate(0, 1, "south"));
        assertTrue(MissileSystemRules.launchTableIsPort(1, 0, "south"));
        assertTrue(MissileSystemRules.launchTableIsPort(1, 1, "south"));
        assertFalse(MissileSystemRules.launchTableIsPlate(0, 0, "south"));
        assertTrue(MissileSystemRules.launchTableIsPlate(1, 0, "west"));
        assertTrue(MissileSystemRules.launchTableIsPort(0, 1, "west"));
        assertEquals(90.0F, MissileSystemRules.launchTableYaw("south"), 0.01F);
        assertEquals(0.0F, MissileSystemRules.launchTableYaw("west"), 0.01F);
        assertEquals(270.0F, MissileSystemRules.launchTableYaw("north"), 0.01F);
        assertEquals(180.0F, MissileSystemRules.launchTableYaw("east"), 0.01F);
    }
}
