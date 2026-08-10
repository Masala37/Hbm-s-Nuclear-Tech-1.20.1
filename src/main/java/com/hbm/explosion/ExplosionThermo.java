package com.hbm.explosion;

import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Thermal conversion helpers from legacy {@code ExplosionThermo}.
 */
public final class ExplosionThermo {
    private ExplosionThermo() {
    }

    public static void freeze(Level level, BlockPos origin, int strength) {
        if (level.isClientSide || strength <= 0) {
            return;
        }
        int r = strength * 2;
        int r2 = r * r;
        int r22 = r2 / 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -r; xx < r; xx++) {
            for (int yy = -r; yy < r; yy++) {
                for (int zz = -r; zz < r; zz++) {
                    int zzSum = xx * xx + yy * yy + zz * zz;
                    if (zzSum >= r22 + level.random.nextInt(Math.max(1, r22 / 2))) {
                        continue;
                    }
                    cursor.set(origin.getX() + xx, origin.getY() + yy, origin.getZ() + zz);
                    if (level.isInWorldBounds(cursor)) {
                        freezeDest(level, cursor);
                    }
                }
            }
        }
    }

    public static void scorch(Level level, BlockPos origin, int strength) {
        if (level.isClientSide || strength <= 0) {
            return;
        }
        int r = strength * 2;
        int r2 = r * r;
        int r22 = r2 / 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -r; xx < r; xx++) {
            for (int yy = -r; yy < r; yy++) {
                for (int zz = -r; zz < r; zz++) {
                    int zzSum = xx * xx + yy * yy + zz * zz;
                    if (zzSum >= r22 + level.random.nextInt(Math.max(1, r22 / 2))) {
                        continue;
                    }
                    cursor.set(origin.getX() + xx, origin.getY() + yy, origin.getZ() + zz);
                    if (level.isInWorldBounds(cursor)) {
                        scorchDest(level, cursor);
                    }
                }
            }
        }
    }

    /** Encases nearby living entities in ice and applies cold debuffs. */
    public static void freezer(Level level, double x, double y, double z, int strength) {
        if (level.isClientSide || strength <= 0) {
            return;
        }

        double wat = strength;
        double diameter = strength * 2.0D;
        AABB box = new AABB(x, y, z, x, y, z).inflate(wat + 1.0D);
        for (Entity entity : level.getEntities(null, box)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            double distScale = Math.sqrt(living.distanceToSqr(x, y, z)) / diameter;
            if (distScale > 1.0D) {
                continue;
            }
            double dist = Math.sqrt(living.distanceToSqr(x, y, z));
            if (dist >= wat) {
                continue;
            }

            int ax = Mth.floor(living.getX()) - 2;
            int bx = Mth.floor(living.getX()) + 1;
            int ay = Mth.floor(living.getY());
            int by = Mth.floor(living.getY()) + 3;
            int az = Mth.floor(living.getZ()) - 1;
            int bz = Mth.floor(living.getZ()) + 2;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int a = ax; a < bx; a++) {
                for (int b = ay; b < by; b++) {
                    for (int c = az; c < bz; c++) {
                        cursor.set(a, b, c);
                        if (level.getBlockState(cursor).canBeReplaced()) {
                            level.setBlock(cursor, Blocks.ICE.defaultBlockState(), 3);
                        }
                    }
                }
            }

            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2 * 60 * 20, 4));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 90 * 20, 2));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 3 * 60 * 20, 2));
        }
    }

    public static void setEntitiesOnFire(Level level, double x, double y, double z, int radius) {
        if (level.isClientSide || radius <= 0) {
            return;
        }
        AABB box = new AABB(x, y, z, x, y, z).inflate(radius);
        for (Entity entity : level.getEntities(null, box)) {
            if (Math.sqrt(entity.distanceToSqr(x, y, z)) > radius) {
                continue;
            }
            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 15 * 20, 4));
            }
            entity.setSecondsOnFire(10);
        }
    }

    public static void freeze(Level level, int x, int y, int z, int strength) {
        freeze(level, new BlockPos(x, y, z), strength);
    }

    public static void scorch(Level level, int x, int y, int z, int strength) {
        scorch(level, new BlockPos(x, y, z), strength);
    }

    public static void freezer(Level level, int x, int y, int z, int strength) {
        freezer(level, x + 0.5D, y + 0.5D, z + 0.5D, strength);
    }

    private static void freezeDest(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.LAVA) || state.getFluidState().is(Fluids.LAVA) || state.getFluidState().is(Fluids.FLOWING_LAVA)) {
            level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
            return;
        }
        if (state.is(Blocks.WATER) || state.getFluidState().is(Fluids.WATER) || state.getFluidState().is(Fluids.FLOWING_WATER)) {
            level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
            return;
        }
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM)) {
            level.setBlock(pos, ModBlocks.FROZEN_DIRT.get().defaultBlockState(), 3);
            return;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
            level.setBlock(pos, ModBlocks.FROZEN_DIRT.get().defaultBlockState(), 3);
            return;
        }
        if (state.is(BlockTags.LOGS)) {
            level.setBlock(pos, ModBlocks.FROZEN_LOG.get().defaultBlockState(), 3);
            return;
        }
        if (state.is(BlockTags.PLANKS)) {
            level.setBlock(pos, ModBlocks.FROZEN_PLANKS.get().defaultBlockState(), 3);
            return;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.STONE_BRICKS)
                || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            level.setBlock(pos, Blocks.PACKED_ICE.defaultBlockState(), 3);
            return;
        }
        if (state.is(BlockTags.LEAVES)) {
            level.setBlock(pos, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
        }
    }

    private static void scorchDest(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM)
                || state.is(ModBlocks.FROZEN_DIRT.get())) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            return;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(ModBlocks.WASTE_EARTH.get())) {
            level.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), 3);
            return;
        }
        if (state.is(Blocks.NETHERRACK)) {
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
            return;
        }
        if (state.is(BlockTags.LOGS) || state.is(ModBlocks.FROZEN_LOG.get())) {
            level.setBlock(pos, ModBlocks.WASTE_PLANKS.get().defaultBlockState(), 3);
            return;
        }
        if (state.is(BlockTags.PLANKS) || state.is(ModBlocks.FROZEN_PLANKS.get())) {
            level.setBlock(pos, ModBlocks.WASTE_PLANKS.get().defaultBlockState(), 3);
            return;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.STONE_BRICKS)
                || state.is(Blocks.OBSIDIAN) || state.is(Blocks.DEEPSLATE)) {
            level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
            return;
        }
        if (state.is(BlockTags.LEAVES) || state.is(Blocks.WATER) || state.getFluidState().is(Fluids.WATER)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        if (state.is(Blocks.PACKED_ICE)) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            return;
        }
        if (state.is(Blocks.ICE) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
