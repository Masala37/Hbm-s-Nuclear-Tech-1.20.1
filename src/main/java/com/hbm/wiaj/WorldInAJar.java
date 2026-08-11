package com.hbm.wiaj;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Minimal debris-only WorldInAJar — a BlockState grid for flying chunk particles.
 * Full GUI/script WIAJ is out of scope.
 */
public class WorldInAJar {
    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;

    private final BlockState[][][] blocks;

    public WorldInAJar(int x, int y, int z) {
        this.sizeX = Math.max(1, x);
        this.sizeY = Math.max(1, y);
        this.sizeZ = Math.max(1, z);
        this.blocks = new BlockState[sizeX][sizeY][sizeZ];
    }

    public BlockState getBlock(int x, int y, int z) {
        if (x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState state = blocks[x][y][z];
        return state != null ? state : Blocks.AIR.defaultBlockState();
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        if (x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) {
            return;
        }
        blocks[x][y][z] = state == null ? Blocks.AIR.defaultBlockState() : state;
    }

    public boolean isAir(int x, int y, int z) {
        return getBlock(x, y, z).isAir();
    }
}
