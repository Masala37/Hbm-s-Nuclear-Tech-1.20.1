package com.hbm.blockentity.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LauncherFluidTransferTest {
    @Test
    void incompatibleOutputBlocksTransfer() {
        assertFalse(LauncherFluidTransfer.canStore(1, 1, 16, 64, false));
    }

    @Test
    void fullOutputBlocksTransfer() {
        assertFalse(LauncherFluidTransfer.canStore(16, 1, 16, 64, true));
        assertFalse(LauncherFluidTransfer.canStore(64, 1, 64, 64, true));
    }

    @Test
    void entireRemainderMustFitIncludingMultiItemContainers() {
        assertTrue(LauncherFluidTransfer.canStore(15, 1, 16, 64, true));
        assertTrue(LauncherFluidTransfer.canStore(14, 2, 16, 64, true));
        assertFalse(LauncherFluidTransfer.canStore(15, 2, 16, 64, true));
        assertFalse(LauncherFluidTransfer.canStore(0, 17, 16, 64, true));
    }

    @Test
    void slotLimitAndUnstackableContainersAreRespected() {
        assertFalse(LauncherFluidTransfer.canStore(0, 2, 16, 1, true));
        assertTrue(LauncherFluidTransfer.canStore(0, 1, 1, 64, true));
        assertFalse(LauncherFluidTransfer.canStore(1, 1, 1, 64, true));
    }

    @Test
    void consumedContainerNeedsNoOutputSpace() {
        assertTrue(LauncherFluidTransfer.canStore(64, 0, 64, 64, false));
    }
}
