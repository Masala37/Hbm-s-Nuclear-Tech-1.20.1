package com.hbm.energy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PowerNetTest {
    @Test
    void splitsEvenlyBetweenTwoReceivers() {
        PowerNet.BufferNode gen = new PowerNet.BufferNode(1000, 1000, 1000, ConnectionPriority.NORMAL);
        PowerNet.BufferNode a = new PowerNet.BufferNode(0, 1000, 1000, ConnectionPriority.NORMAL);
        PowerNet.BufferNode b = new PowerNet.BufferNode(0, 1000, 1000, ConnectionPriority.NORMAL);
        long moved = PowerNet.transfer(List.of(gen), List.of(a, b));
        assertEquals(1000, moved);
        assertEquals(0, gen.stored);
        assertEquals(500, a.stored);
        assertEquals(500, b.stored);
    }

    @Test
    void highestPriorityTakesFirst() {
        PowerNet.BufferNode gen = new PowerNet.BufferNode(100, 100, 100, ConnectionPriority.NORMAL);
        PowerNet.BufferNode low = new PowerNet.BufferNode(0, 100, 100, ConnectionPriority.LOW);
        PowerNet.BufferNode high = new PowerNet.BufferNode(0, 100, 100, ConnectionPriority.HIGHEST);
        PowerNet.transfer(List.of(gen), List.of(low, high));
        assertEquals(100, high.stored);
        assertEquals(0, low.stored);
        assertEquals(0, gen.stored);
    }

    @Test
    void emptySidesMoveNothing() {
        assertEquals(0, PowerNet.transfer(List.of(), List.of(new PowerNet.BufferNode(0, 10, 10, ConnectionPriority.NORMAL))));
        assertEquals(0, PowerNet.transfer(List.of(new PowerNet.BufferNode(10, 10, 10, ConnectionPriority.NORMAL)), List.of()));
    }
}
