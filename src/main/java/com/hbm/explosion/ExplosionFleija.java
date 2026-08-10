package com.hbm.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shell-expanding FLEIJA dig (legacy {@code ExplosionFleija}).
 * Clears a sphere column-by-column into air.
 */
public class ExplosionFleija {
    public int posX;
    public int posY;
    public int posZ;
    public int lastposX;
    public int lastposZ;
    public int radius;
    public int radius2;
    public float explosionCoefficient = 1.0F;
    public float explosionCoefficient2 = 1.0F;

    private final Level level;
    private int n = 1;
    private int nlimit;
    private int shell;
    private int leg;
    private int element;

    public ExplosionFleija(int x, int y, int z, Level level, int rad, float coefficient, float coefficient2) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.level = level;
        this.radius = rad;
        this.radius2 = rad * rad;
        this.explosionCoefficient = coefficient;
        this.explosionCoefficient2 = coefficient2;
        this.nlimit = this.radius2 * 4;
    }

    public boolean update() {
        breakColumn(lastposX, lastposZ);
        shell = (int) Math.floor((Math.sqrt(n) + 1) / 2);
        int shell2 = shell * 2;
        if (shell2 == 0) {
            return true;
        }
        leg = (int) Math.floor((n - (shell2 - 1) * (shell2 - 1)) / (double) shell2);
        element = (n - (shell2 - 1) * (shell2 - 1)) - shell2 * leg - shell + 1;
        lastposX = leg == 0 ? shell : leg == 1 ? -element : leg == 2 ? -shell : element;
        lastposZ = leg == 0 ? element : leg == 1 ? shell : leg == 2 ? -element : -shell;
        n++;
        return n > nlimit;
    }

    private void breakColumn(int x, int z) {
        int dist = radius2 - (x * x + z * z);
        if (dist <= 0) {
            return;
        }
        dist = (int) Math.sqrt(dist);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = (int) (dist / explosionCoefficient2); y > -dist / explosionCoefficient; y--) {
            int wy = posY + y;
            if (wy <= level.getMinBuildHeight()) {
                continue;
            }
            cursor.set(posX + x, wy, posZ + z);
            if (!level.isInWorldBounds(cursor)) {
                continue;
            }
            BlockState state = level.getBlockState(cursor);
            if (!state.isAir() && state.getDestroySpeed(level, cursor) >= 0) {
                level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    public void saveToNbt(CompoundTag nbt, String name) {
        nbt.putInt(name + "posX", posX);
        nbt.putInt(name + "posY", posY);
        nbt.putInt(name + "posZ", posZ);
        nbt.putInt(name + "lastposX", lastposX);
        nbt.putInt(name + "lastposZ", lastposZ);
        nbt.putInt(name + "radius", radius);
        nbt.putInt(name + "radius2", radius2);
        nbt.putInt(name + "n", n);
        nbt.putInt(name + "nlimit", nlimit);
        nbt.putInt(name + "shell", shell);
        nbt.putInt(name + "leg", leg);
        nbt.putInt(name + "element", element);
        nbt.putFloat(name + "c1", explosionCoefficient);
        nbt.putFloat(name + "c2", explosionCoefficient2);
    }

    public void readFromNbt(CompoundTag nbt, String name) {
        posX = nbt.getInt(name + "posX");
        posY = nbt.getInt(name + "posY");
        posZ = nbt.getInt(name + "posZ");
        lastposX = nbt.getInt(name + "lastposX");
        lastposZ = nbt.getInt(name + "lastposZ");
        radius = nbt.getInt(name + "radius");
        radius2 = nbt.getInt(name + "radius2");
        n = nbt.getInt(name + "n");
        nlimit = nbt.getInt(name + "nlimit");
        shell = nbt.getInt(name + "shell");
        leg = nbt.getInt(name + "leg");
        element = nbt.getInt(name + "element");
        explosionCoefficient = nbt.getFloat(name + "c1");
        explosionCoefficient2 = nbt.getFloat(name + "c2");
    }
}
