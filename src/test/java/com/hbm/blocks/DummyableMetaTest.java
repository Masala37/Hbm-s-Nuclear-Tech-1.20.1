package com.hbm.blocks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyableMetaTest {
    @Test
    void coreMetaIsOffsetPlusFacing() {
        assertEquals(12, DummyableMeta.coreMeta(DummyableMeta.NORTH));
        assertEquals(13, DummyableMeta.coreMeta(DummyableMeta.SOUTH));
        assertTrue(DummyableMeta.isCore(12));
        assertTrue(DummyableMeta.isCore(15));
        assertFalse(DummyableMeta.isCore(0));
        assertFalse(DummyableMeta.isCore(6));
    }

    @Test
    void extraStripsBackToDummyFacing() {
        int extra = DummyableMeta.makeExtra(DummyableMeta.WEST);
        assertTrue(DummyableMeta.isExtra(extra));
        assertEquals(DummyableMeta.WEST, DummyableMeta.stripExtra(extra));
        assertEquals(1, DummyableMeta.towardCoreX(extra));
    }

    @Test
    void dummyBelowWalksUpToCore() {
        int meta = DummyableMeta.dummyMeta(0, 10, 0, 0, 9, 0);
        assertEquals(DummyableMeta.DOWN, meta);
        assertEquals(1, DummyableMeta.towardCoreY(meta));
    }

    @Test
    void dummyCellsAreNotCoresSoTheyMustNotKeepATesr() {
        assertFalse(DummyableMeta.isCore(DummyableMeta.UP));
        assertFalse(DummyableMeta.isCore(DummyableMeta.WEST));
        int extra = DummyableMeta.makeExtra(DummyableMeta.NORTH);
        assertTrue(DummyableMeta.isExtra(extra));
        assertFalse(DummyableMeta.isCore(extra));
        assertTrue(DummyableMeta.isCore(DummyableMeta.coreMeta(DummyableMeta.SOUTH)));
    }

    @Test
    void yawBucketsMatchLegacy() {
        assertEquals(DummyableMeta.NORTH, DummyableMeta.facingFromYaw(0.0F));
        assertEquals(DummyableMeta.EAST, DummyableMeta.facingFromYaw(90.0F));
        assertEquals(DummyableMeta.SOUTH, DummyableMeta.facingFromYaw(180.0F));
        assertEquals(DummyableMeta.WEST, DummyableMeta.facingFromYaw(270.0F));
        assertEquals(90.0F, DummyableMeta.tesrYaw(DummyableMeta.NORTH));
        assertEquals(0.0F, DummyableMeta.tesrYaw(DummyableMeta.EAST));
    }

    @Test
    void dummyableYawMatchesLegacyFireboxBoiler() {
        assertEquals(0.0F, DummyableMeta.dummyableYaw(DummyableMeta.SOUTH));
        assertEquals(90.0F, DummyableMeta.dummyableYaw(DummyableMeta.EAST));
        assertEquals(180.0F, DummyableMeta.dummyableYaw(DummyableMeta.NORTH));
        assertEquals(270.0F, DummyableMeta.dummyableYaw(DummyableMeta.WEST));
        assertEquals(DummyableMeta.EAST, DummyableMeta.rotateYClockwise(DummyableMeta.NORTH));
        assertEquals(DummyableMeta.WEST, DummyableMeta.rotateYCounterClockwise(DummyableMeta.NORTH));
        assertEquals(DummyableMeta.EAST, DummyableMeta.rotateYCounterClockwise(DummyableMeta.SOUTH));
    }
}
