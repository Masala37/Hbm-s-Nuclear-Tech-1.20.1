package com.hbm.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PylonDyeTest {
    @Test
    void oreSuffixMatches17FireworkInts() {
        assertEquals(1_973_019, PylonDye.fromOreSuffix("black"));
        assertEquals(11_743_532, PylonDye.fromOreSuffix("red"));
        assertEquals(15_790_320, PylonDye.fromOreSuffix("white"));
        assertEquals(15_790_320, PylonDye.fromOreSuffix("White"));
        assertEquals(6_719_955, PylonDye.fromOreSuffix("lightblue"));
        assertEquals(6_719_955, PylonDye.fromOreSuffix("light_blue"));
        assertEquals(11_250_603, PylonDye.fromOreSuffix("silver"));
        assertEquals(11_250_603, PylonDye.fromOreSuffix("lightgray"));
        assertEquals(11_250_603, PylonDye.fromOreSuffix("light_gray"));
        assertEquals(0, PylonDye.fromOreSuffix(""));
        assertEquals(0, PylonDye.fromOreSuffix("notadyes"));
    }
}
