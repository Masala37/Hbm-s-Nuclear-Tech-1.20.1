package com.hbm.blocks.generic;

import com.hbm.blocks.DummyableMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiloHatchLogicTest {
    @Test
    void hatchSitsClosedThenPitchesTowardMinusOneTwenty() {
        assertEquals(0.0F, SiloHatchLogic.hatchLift(0.0F), 1.0e-4F);
        assertEquals(0.25F, SiloHatchLogic.hatchLift(10.0F), 1.0e-4F);
        assertEquals(0.0F, SiloHatchLogic.hatchPitch(0.0F), 1.0e-3F);
        assertEquals(-120.0F, SiloHatchLogic.hatchPitch(SiloHatchLogic.TIME_TO_OPEN), 1.0e-2F);
        assertEquals(-1.875F, SiloHatchLogic.hatchOriginZ(false));
        assertEquals(-2.875F, SiloHatchLogic.hatchOriginZ(true));
    }

    @Test
    void clientTicksMatchOneSevenMillis() {
        long start = 1_000_000L;
        assertEquals(0.0F, SiloHatchLogic.clientOpenTicks(SiloHatchLogic.STATE_CLOSED, start, start + 3_000L), 1.0e-3F);
        assertEquals(60.0F, SiloHatchLogic.clientOpenTicks(SiloHatchLogic.STATE_OPEN, start, start), 1.0e-3F);
        assertEquals(20.0F, SiloHatchLogic.clientOpenTicks(SiloHatchLogic.STATE_OPENING, start, start + 1_000L), 1.0e-3F);
        assertEquals(40.0F, SiloHatchLogic.clientOpenTicks(SiloHatchLogic.STATE_CLOSING, start, start + 1_000L), 1.0e-3F);
    }

    @Test
    void eastWestDoorOffsetsMatchOneSevenRotationHack() {
        assertEquals(-2, SiloHatchLogic.rotateDoorOffset(DummyableMeta.SOUTH, 2, 0, 1).getX());
        assertEquals(-1, SiloHatchLogic.rotateDoorOffset(DummyableMeta.SOUTH, 2, 0, 1).getZ());
        assertEquals(-1, SiloHatchLogic.rotateDoorOffset(DummyableMeta.EAST, 2, 0, 1).getX());
        assertEquals(2, SiloHatchLogic.rotateDoorOffset(DummyableMeta.EAST, 2, 0, 1).getZ());
        assertEquals(1, SiloHatchLogic.rotateDoorOffset(DummyableMeta.WEST, 2, 0, 1).getX());
        assertEquals(-2, SiloHatchLogic.rotateDoorOffset(DummyableMeta.WEST, 2, 0, 1).getZ());
        assertEquals(2, SiloHatchLogic.rotateDoorOffset(DummyableMeta.NORTH, 2, 0, 1).getX());
        assertEquals(1, SiloHatchLogic.rotateDoorOffset(DummyableMeta.NORTH, 2, 0, 1).getZ());
    }

    @Test
    void extraMetaTogglesWithoutTouchingCores() {
        int dummy = DummyableMeta.dummyMeta(0, 0, 0, 1, 0, 0);
        int extra = DummyableMeta.makeExtra(dummy);
        assertTrue(DummyableMeta.isExtra(extra));
        assertEquals(dummy, DummyableMeta.removeExtra(extra));
        int core = DummyableMeta.coreMeta(DummyableMeta.SOUTH);
        assertEquals(core, DummyableMeta.makeExtra(core));
        assertEquals(core, DummyableMeta.removeExtra(core));
        assertFalse(SiloHatchLogic.passable(SiloHatchLogic.STATE_CLOSED));
        assertTrue(SiloHatchLogic.passable(SiloHatchLogic.STATE_OPENING));
    }
}
