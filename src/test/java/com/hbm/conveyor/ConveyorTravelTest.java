package com.hbm.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConveyorTravelTest {
    @Test
    void outputIsOppositeWhenStraight() {
        assertEquals(Direction.SOUTH, ConveyorTravel.output(Direction.NORTH, ConveyorTravel.BEND_STRAIGHT));
        assertEquals(Direction.EAST, ConveyorTravel.output(Direction.WEST, ConveyorTravel.BEND_STRAIGHT));
    }

    @Test
    void bendsRotateOutput() {
        assertEquals(Direction.EAST, ConveyorTravel.output(Direction.NORTH, ConveyorTravel.BEND_LEFT));
        assertEquals(Direction.WEST, ConveyorTravel.output(Direction.NORTH, ConveyorTravel.BEND_RIGHT));
    }

    @Test
    void snapKeepsTravelAxis() {
        BlockPos pos = new BlockPos(4, 10, 8);
        Vec3 item = new Vec3(4.2D, 11.0D, 8.9D);
        Vec3 snap = ConveyorTravel.snap(pos, Direction.NORTH, item);
        assertEquals(4.5D, snap.x, 1.0E-6D);
        assertEquals(10.25D, snap.y, 1.0E-6D);
        assertEquals(8.9D, snap.z, 1.0E-6D);
    }
}
