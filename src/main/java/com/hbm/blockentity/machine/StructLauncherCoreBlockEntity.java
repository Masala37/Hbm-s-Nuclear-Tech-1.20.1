package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.CompactLauncherBlock;
import com.hbm.blocks.machine.LaunchTableBlock;
import com.hbm.blocks.machine.StructLauncherCoreBlock;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 {@code TileEntityMultiblock}: 3×3 struct_launcher → compact launcher;
 * 9×9 plus a scaffold tower → launch table.
 */
public class StructLauncherCoreBlockEntity extends BlockEntity {
    public StructLauncherCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRUCT_LAUNCHER_CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  StructLauncherCoreBlockEntity be) {
        if (level.isClientSide || !(state.getBlock() instanceof StructLauncherCoreBlock core)) {
            return;
        }
        if (core.kind() == StructLauncherCoreBlock.Kind.COMPACT) {
            if (isCompactFrame(level, pos)) {
                CompactLauncherBlock.formFromStruct(level, pos, ModBlocks.COMPACT_LAUNCHER.get());
            }
            return;
        }
        Direction tower = tableTowerFacing(level, pos);
        if (tower != null) {
            LaunchTableBlock.formFromStruct(level, pos, tower, ModBlocks.LAUNCH_TABLE.get());
        }
    }

    private static boolean isCompactFrame(Level level, BlockPos core) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (!isLauncherPad(level.getBlockState(core.offset(dx, 0, dz)))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Direction tableTowerFacing(Level level, BlockPos core) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (!isLauncherPad(level.getBlockState(core.offset(dx, 0, dz)))) {
                    return null;
                }
            }
        }
        for (Direction facing : new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH}) {
            if (hasScaffoldTower(level, core.relative(facing, 3))) {
                return facing;
            }
        }
        return null;
    }

    private static boolean hasScaffoldTower(Level level, BlockPos base) {
        for (int y = 1; y < 12; y++) {
            if (!isScaffold(level.getBlockState(base.above(y)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLauncherPad(BlockState state) {
        return state.is(ModBlocks.STRUCT_LAUNCHER.get());
    }

    private static boolean isScaffold(BlockState state) {
        return state.is(ModBlocks.STRUCT_SCAFFOLD.get());
    }
}
