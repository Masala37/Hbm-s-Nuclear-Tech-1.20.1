package com.hbm.world.gen.nbt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyMetaTransformTest {
    @Test
    void doorKeepsUpperHingeAndRotatesLower() {
        assertEquals(8, LegacyMetaTransform.door(8, 1));
        assertEquals(9, LegacyMetaTransform.door(9, 2));
        assertEquals(0, LegacyMetaTransform.door(0, 0));
        assertEquals(1, LegacyMetaTransform.door(0, 1));
        assertEquals(2, LegacyMetaTransform.door(0, 2));
        assertEquals(3, LegacyMetaTransform.door(0, 3));
        assertEquals(6, LegacyMetaTransform.door(1 | 4, 1));
    }

    @Test
    void decoFurnaceFacing() {
        assertEquals(5, LegacyMetaTransform.deco(2, 1));
        assertEquals(3, LegacyMetaTransform.deco(2, 2));
        assertEquals(4, LegacyMetaTransform.deco(2, 3));
    }

    @Test
    void decoModelHighMatchesBlockDecoModel() {
        assertEquals(12, LegacyMetaTransform.decoModelHigh(0, 1));
        assertEquals(4, LegacyMetaTransform.decoModelHigh(0, 2));
        assertEquals(8, LegacyMetaTransform.decoModelHigh(0, 3));
    }

    @Test
    void crtScreenFacesOppositeOfYawIndex() {
        assertEquals(2, LegacyMetaTransform.crtScreen2d(0));
        assertEquals(3, LegacyMetaTransform.crtScreen2d(1));
        assertEquals(0, LegacyMetaTransform.crtScreen2d(2));
        assertEquals(1, LegacyMetaTransform.crtScreen2d(3));
        assertEquals(2, LegacyMetaTransform.crtScreen2d(8));
        assertEquals(0, LegacyMetaTransform.decoModelLow(0, 0));
        assertEquals(1, LegacyMetaTransform.decoModelLow(0, 1));
    }

    @Test
    void stairsAndTrapdoors() {
        assertEquals(2, LegacyMetaTransform.stairs(0, 1));
        assertEquals(1, LegacyMetaTransform.stairs(0, 2));
        assertEquals(3, LegacyMetaTransform.trapdoor(0, 1));
    }
}
