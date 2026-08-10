package com.hbm.explosion;

import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Spiral-column balefire dig (legacy {@code ExplosionBalefire}).
 * Digs from the column surface height downward — do not raise the start by blast radius
 * or the crater floor floats in the sky (looks like an empty volcano).
 * Clears leaves and vaporizes water/lava in the dig column (legacy {@code setBlockToAir}).
 */
public class ExplosionBalefire {
    public int posX;
    public int posY;
    public int posZ;
    public int lastposX;
    public int lastposZ;
    public int radius;
    public int radius2;
    public Level level;
    private int n = 1;
    private int nlimit;
    private int shell;
    private int leg;
    private int element;

    public ExplosionBalefire(int x, int y, int z, Level level, int rad) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.level = level;
        this.radius = Math.max(1, rad);
        this.radius2 = this.radius * this.radius;
        this.nlimit = this.radius2 * 4;
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
    }

    public void readFromNbt(CompoundTag nbt, String name) {
        posX = nbt.getInt(name + "posX");
        posY = nbt.getInt(name + "posY");
        posZ = nbt.getInt(name + "posZ");
        lastposX = nbt.getInt(name + "lastposX");
        lastposZ = nbt.getInt(name + "lastposZ");
        radius = nbt.getInt(name + "radius");
        radius2 = nbt.getInt(name + "radius2");
        n = Math.max(nbt.getInt(name + "n"), 1);
        nlimit = nbt.getInt(name + "nlimit");
        shell = nbt.getInt(name + "shell");
        leg = nbt.getInt(name + "leg");
        element = nbt.getInt(name + "element");
    }

    public boolean update() {
        if (n == 0) {
            return true;
        }
        breakColumn(this.lastposX, this.lastposZ);
        this.shell = (int) Math.floor((Math.sqrt(n) + 1) / 2);
        int shell2 = this.shell * 2;
        if (shell2 == 0) {
            return true;
        }
        this.leg = (int) Math.floor((this.n - (shell2 - 1) * (shell2 - 1)) / (double) shell2);
        this.element = (this.n - (shell2 - 1) * (shell2 - 1)) - shell2 * this.leg - this.shell + 1;
        this.lastposX = this.leg == 0 ? this.shell : this.leg == 1 ? -this.element : this.leg == 2 ? -this.shell : this.element;
        this.lastposZ = this.leg == 0 ? this.element : this.leg == 1 ? this.shell : this.leg == 2 ? -this.element : -this.shell;
        this.n++;
        return this.n > this.nlimit;
    }

    private void breakColumn(int x, int z) {
        int dist = (int) (radius - Math.sqrt(x * (double) x + z * (double) z));
        if (dist <= 0) {
            return;
        }
        int pX = posX + x;
        int pZ = posZ + z;

        if (level instanceof ServerLevel server) {
            server.getChunk(pX >> 4, pZ >> 4);
        }

        // Ground for crater depth (ignore canopy). World surface includes leaves/fluids for clear start.
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pX, pZ);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, pX, pZ);
        int y = Math.min(Math.max(ground, surface), level.getMaxBuildHeight() - 1);

        int maxdepth = (int) (10 + radius * 0.25);
        int dig = (int) ((maxdepth * dist / (double) radius) + (Math.sin(dist * 0.15 + 2) * 2));
        int depth = Math.max(ground - dig, level.getMinBuildHeight());

        BlockState balefire = ModBlocks.BALEFIRE.get().defaultBlockState();
        BlockState slaked = ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState();

        while (y > depth) {
            destroyColumnBlock(pX, y, pZ);
            y--;
        }

        if (level.random.nextInt(10) == 0) {
            BlockPos firePos = new BlockPos(pX, depth + 1, pZ);
            if (firePos.getY() < level.getMaxBuildHeight()) {
                BlockState atFire = level.getBlockState(firePos);
                if (atFire.isAir() || atFire.canBeReplaced()) {
                    BlockPos floor = firePos.below();
                    BlockState floorState = level.getBlockState(floor);
                    if (!floorState.isAir() && floorState.getFluidState().isEmpty()) {
                        level.setBlock(firePos, balefire, 3);
                    }
                }
            }
        }

        for (int i = depth; i > depth - 5 && i >= level.getMinBuildHeight(); i--) {
            BlockPos pos = new BlockPos(pX, i, pZ);
            BlockState below = level.getBlockState(pos);
            if (below.is(Blocks.STONE) || below.is(Blocks.DEEPSLATE) || below.is(Blocks.COBBLESTONE)) {
                level.setBlock(pos, slaked, 3);
            }
        }
    }

    /** Legacy {@code setBlockToAir}: solids, leaves, and fluids (no leftover water cubes). */
    private void destroyColumnBlock(int pX, int y, int pZ) {
        BlockPos pos = new BlockPos(pX, y, pZ);
        BlockState state = level.getBlockState(pos);

        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            ExplosionFluidHelper.vaporize(level, pos, 3);
            state = level.getBlockState(pos);
        }

        if (ExplosionFluidHelper.isFluidish(state) || state.getBlock() instanceof LiquidBlock) {
            ExplosionFluidHelper.vaporize(level, pos, 3);
            return;
        }

        if (state.isAir()) {
            return;
        }

        if (state.is(BlockTags.LEAVES) || state.is(BlockTags.WART_BLOCKS) || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.REPLACEABLE) || state.getBlock() instanceof BushBlock
                || state.is(Blocks.VINE) || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)
                || state.is(Blocks.GLOW_LICHEN) || state.is(Blocks.MOSS_CARPET)
                || state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW)) {
            level.removeBlock(pos, false);
            return;
        }

        if (state.getDestroySpeed(level, pos) >= 0.0F) {
            level.removeBlock(pos, false);
        }
    }
}
