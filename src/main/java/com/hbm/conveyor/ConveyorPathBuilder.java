package com.hbm.conveyor;

import com.hbm.blocks.network.ConveyorBlock;
import com.hbm.blocks.network.CraneBaseBlock;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 {@code ItemConveyorWand.construct} for regular horizontal belts (no lift/chute in this pass).
 */
public final class ConveyorPathBuilder {
    private ConveyorPathBuilder() {
    }

    public static int facingMeta(Player player) {
        return player.getDirection().getOpposite().ordinal();
    }

    public static Direction facingFromPlayer(Player player) {
        return player.getDirection().getOpposite();
    }

    /**
     * @return belt count, 0 if not enough, -1 if obstructed
     */
    public static int construct(Level route, boolean place, Player player,
                                BlockPos start, Direction startFace,
                                BlockPos end, Direction endFace, int max) {
        Direction dir = startFace;
        Direction targetDir = endFace;

        if (start.equals(end) && startFace == endFace && (dir.getAxis() == Direction.Axis.Y)) {
            BlockPos at = start.relative(dir);
            if (!replaceable(route, at)) {
                return -1;
            }
            if (place) {
                route.setBlock(at, ModBlocks.CONVEYOR.get().defaultBlockState()
                        .setValue(ConveyorBlock.FACING, facingFromPlayer(player)), Block.UPDATE_ALL);
            }
            return 1;
        }

        BlockPos target = end.relative(targetDir);
        BlockPos cur = start.relative(dir);

        if (dir.getAxis() == Direction.Axis.Y) {
            dir = targetDirection(cur, end, false);
        }

        BlockState targetState = route.getBlockState(end);
        boolean isTargetHorizontal = targetDir.getAxis() != Direction.Axis.Y;
        boolean shouldTurnToTarget = isTargetHorizontal
                || targetState.getBlock() instanceof CraneBaseBlock;

        Direction horDir = dir.getAxis() == Direction.Axis.Y ? facingFromPlayer(player).getOpposite() : dir;

        for (int loopDepth = 1; loopDepth <= max; loopDepth++) {
            if (!replaceable(route, cur)) {
                return -1;
            }

            Direction travel = dir;
            int bend = ConveyorTravel.BEND_STRAIGHT;
            BlockPos next = cur.relative(dir);

            int fromDistance = taxi(cur, target);
            int toDistance = taxi(next, target);
            int finalDistance = taxi(next, end);
            boolean notAtTarget = (shouldTurnToTarget ? finalDistance : fromDistance) > 0;
            boolean willBeObstructed = notAtTarget && !replaceable(route, next);
            boolean shouldTurn = (toDistance >= fromDistance && notAtTarget) || willBeObstructed;

            if (shouldTurn) {
                BlockPos aim = shouldTurnToTarget ? end : target;
                Direction newDir = targetDirection(cur, aim, target, dir, willBeObstructed);
                if (newDir.getAxis() == Direction.Axis.Y) {
                    return -1;
                }
                if (dir.getClockWise() == newDir) {
                    bend = ConveyorTravel.BEND_RIGHT;
                } else if (dir.getCounterClockWise() == newDir) {
                    bend = ConveyorTravel.BEND_LEFT;
                }
                travel = dir;
                dir = newDir;
                if (dir.getAxis() != Direction.Axis.Y) {
                    horDir = dir;
                }
            }

            if (place) {
                Direction facing = travel.getOpposite();
                if (facing.getAxis() == Direction.Axis.Y) {
                    facing = horDir.getOpposite();
                }
                route.setBlock(cur, ModBlocks.CONVEYOR.get().defaultBlockState()
                        .setValue(ConveyorBlock.FACING, facing)
                        .setValue(ConveyorBlock.BEND, bend), Block.UPDATE_ALL);
            }

            if (cur.equals(target)) {
                return loopDepth;
            }
            cur = cur.relative(dir);
        }
        return 0;
    }

    public static boolean replaceable(Level level, BlockPos pos) {
        return level.getBlockState(pos).canBeReplaced();
    }

    private static Direction targetDirection(BlockPos from, BlockPos to, boolean unused) {
        return targetDirection(from, to, to, null, false);
    }

    private static Direction targetDirection(BlockPos from, BlockPos to, BlockPos target, Direction heading,
                                             boolean willBeObstructed) {
        if (Math.abs(from.getX() - to.getX()) > Math.abs(from.getZ() - to.getZ())) {
            if (heading == Direction.EAST || heading == Direction.WEST) {
                return from.getZ() > to.getZ() ? Direction.NORTH : Direction.SOUTH;
            }
            return from.getX() > to.getX() ? Direction.WEST : Direction.EAST;
        }
        if (heading == Direction.NORTH || heading == Direction.SOUTH) {
            return from.getX() > to.getX() ? Direction.WEST : Direction.EAST;
        }
        return from.getZ() > to.getZ() ? Direction.NORTH : Direction.SOUTH;
    }

    private static int taxi(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }
}
