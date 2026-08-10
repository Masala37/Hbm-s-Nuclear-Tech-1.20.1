package com.hbm.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

import java.util.Collection;

/**
 * Clears water/lava (and waterlogged states) so blasts don't leave cubic air pockets in fluids.
 */
public final class ExplosionFluidHelper {
    private ExplosionFluidHelper() {
    }

    public static boolean isFluidish(BlockState state) {
        return state.liquid() || !state.getFluidState().isEmpty();
    }

    public static void vaporize(Level level, BlockPos pos, int flags) {
        // Always notify neighbors so fluid meshing / flow updates and empty "square" holes don't linger.
        int f = flags | 3;
        BlockState state = level.getBlockState(pos);

        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), f);
            state = level.getBlockState(pos);
        }

        if (state.isAir() && state.getFluidState().isEmpty()) {
            return;
        }

        if (isFluidish(state) || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA
                || state.getBlock() == Blocks.BUBBLE_COLUMN || state.getBlock() == Blocks.KELP
                || state.getBlock() == Blocks.KELP_PLANT || state.getBlock() == Blocks.SEAGRASS
                || state.getBlock() == Blocks.TALL_SEAGRASS
                || state.getBlock() == Blocks.WATER_CAULDRON
                || state.getBlock() == Blocks.LAVA_CAULDRON) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), f);
        }
    }

    public static void vaporizeWithNeighbors(Level level, Collection<BlockPos> centers, int flags) {
        for (BlockPos pos : centers) {
            vaporize(level, pos, flags);
            for (Direction dir : Direction.values()) {
                vaporize(level, pos.relative(dir), flags);
            }
        }
    }

    /** Vaporize all fluids in a cube (used to dry nuke craters so lakes don't keep cubic holes). */
    public static void vaporizeCube(Level level, BlockPos center, int radius, int flags) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radius * radius) {
                        continue;
                    }
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    vaporize(level, cursor, flags);
                }
            }
        }
    }

    public static float blastResistance(Level level, BlockPos pos, BlockState state) {
        // Fluids vaporize — don't soak blast power or leave holes in lakes.
        if (isFluidish(state) || state.getFluidState().getType() != Fluids.EMPTY) {
            return 0.0F;
        }
        // Solid magma stand-ins (if any remain) must not stall volcano channel digs.
        if (state.is(com.hbm.registry.ModBlocks.VOLCANIC_LAVA.get())
                || state.is(com.hbm.registry.ModBlocks.RAD_LAVA.get())) {
            return 0.0F;
        }
        return Math.max(0.0F, state.getExplosionResistance(level, pos, null));
    }
}
