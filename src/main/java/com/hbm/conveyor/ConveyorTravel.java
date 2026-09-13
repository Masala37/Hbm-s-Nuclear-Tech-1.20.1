package com.hbm.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 1.7.10 {@code BlockConveyorBase}/{@code BlockConveyorBendable} travel math.
 * {@code FACING} is the 1.7 metadata direction (input). Items travel toward the output.
 */
public final class ConveyorTravel {
    public static final int BEND_STRAIGHT = 0;
    public static final int BEND_LEFT = 1;
    public static final int BEND_RIGHT = 2;

    private ConveyorTravel() {
    }

    public static Direction input(Direction facing, int bend) {
        return facing;
    }

    public static Direction output(Direction facing, int bend) {
        Direction primary = facing.getOpposite();
        if (bend == BEND_RIGHT) {
            return primary.getClockWise();
        }
        if (bend == BEND_LEFT) {
            return primary.getCounterClockWise();
        }
        return primary;
    }

    public static Direction travel(Direction facing, int bend, Vec3 itemPos, BlockPos pos) {
        Direction primary = facing;
        if (bend > BEND_STRAIGHT) {
            int corner = bend - 1;
            double ix = pos.getX() + 0.5D;
            double iz = pos.getZ() + 0.5D;
            Direction secondary = primary.getClockWise();
            ix -= -primary.getStepX() * 0.5D + secondary.getStepX() * (0.5D - corner);
            iz -= -primary.getStepZ() * 0.5D + secondary.getStepZ() * (0.5D - corner);
            double dX = Math.abs(itemPos.x - ix);
            double dZ = Math.abs(itemPos.z - iz);
            if (dX + dZ >= 1.0D) {
                return corner == 0 ? secondary.getOpposite() : secondary;
            }
        }
        return primary;
    }

    public static Vec3 snap(BlockPos pos, Direction travel, Vec3 itemPos) {
        double x = Mth.clamp(itemPos.x, pos.getX(), pos.getX() + 1.0D);
        double z = Mth.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1.0D);
        double posX = pos.getX() + 0.5D;
        double posZ = pos.getZ() + 0.5D;
        if (travel.getStepX() != 0) {
            posX = x;
        }
        if (travel.getStepZ() != 0) {
            posZ = z;
        }
        return new Vec3(posX, pos.getY() + 0.25D, posZ);
    }

    public static Vec3 travelLocation(BlockPos pos, Direction facing, int bend, Vec3 itemPos, double speed) {
        Direction dir = travel(facing, bend, itemPos, pos);
        Vec3 snapPoint = snap(pos, dir, itemPos);
        Vec3 dest = new Vec3(
                snapPoint.x - dir.getStepX() * speed,
                snapPoint.y - dir.getStepY() * speed,
                snapPoint.z - dir.getStepZ() * speed);
        Vec3 motion = dest.subtract(itemPos);
        double len = motion.length();
        if (len < 1.0E-6D) {
            return itemPos;
        }
        return itemPos.add(motion.scale(speed / len));
    }

    public static int clockwiseFacing(Direction facing) {
        return facing.getClockWise().ordinal();
    }
}
