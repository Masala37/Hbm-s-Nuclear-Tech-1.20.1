package com.hbm.explosion;

import com.hbm.entity.effect.EntityFalloutRain;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.energy.ModEnergyStorage;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Shared nuke / EMP helpers.
 */
public final class ExplosionNukeGeneric {
    private ExplosionNukeGeneric() {
    }

    public static void dealDamage(Level level, double x, double y, double z, double radius) {
        dealDamage(level, x, y, z, radius, 250.0F);
    }

    public static void dealDamage(Level level, double x, double y, double z, double radius, float maxDamage) {
        if (level.isClientSide || radius <= 0.0D) {
            return;
        }

        AABB box = new AABB(x, y, z, x, y, z).inflate(radius);
        List<Entity> list = level.getEntities(null, box);

        for (Entity entity : list) {
            if (isExplosionExempt(entity)) {
                continue;
            }

            double dist = entity.distanceToSqr(x, y, z);
            double radiusSq = radius * radius;
            if (dist > radiusSq) {
                continue;
            }

            double distance = Math.sqrt(dist);
            double damage = maxDamage * (radius - distance) / radius;

            // Only living targets get fire/knockback; applying motion to MK5/Torex drifted the blast.
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            boolean hit = living.hurt(level.damageSources().explosion(null), (float) damage);
            if (!hit) {
                continue;
            }

            living.setSecondsOnFire(5);

            Vec3 knock = new Vec3(living.getX() - x, living.getEyeY() - y, living.getZ() - z);
            double len = knock.length();
            if (len > 1.0E-4D) {
                knock = knock.normalize().scale(0.2D);
                living.setDeltaMovement(living.getDeltaMovement().add(knock));
            }
        }
    }

    /**
     * Drain energy storages in a sphere and occasionally scrap electrical machines
     * (legacy {@code empBlast} / {@code emp}).
     */
    public static void empBlast(Level level, BlockPos origin, int radius) {
        if (level.isClientSide || radius <= 0) {
            return;
        }

        int r2 = radius * radius;
        int r22 = r2 / 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -radius; xx < radius; xx++) {
            for (int yy = -radius; yy < radius; yy++) {
                for (int zz = -radius; zz < radius; zz++) {
                    if (xx * xx + yy * yy + zz * zz >= r22) {
                        continue;
                    }
                    cursor.set(origin.getX() + xx, origin.getY() + yy, origin.getZ() + zz);
                    if (level.isInWorldBounds(cursor)) {
                        emp(level, cursor.immutable());
                    }
                }
            }
        }
    }

    public static void empBlast(Level level, int x, int y, int z, int radius) {
        empBlast(level, new BlockPos(x, y, z), radius);
    }

    public static void emp(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return;
        }

        boolean drained = drainEnergy(be, null);
        if (!drained) {
            for (Direction dir : Direction.values()) {
                if (drainEnergy(be, dir)) {
                    drained = true;
                }
            }
        }

        if (drained && level.random.nextInt(5) <= 1) {
            level.setBlock(pos, ModBlocks.ELECTRICAL_SCRAP.get().defaultBlockState(), 3);
        }
    }

    /**
     * Convert terrain to nuclear waste in a sphere (legacy {@code waste} / {@code wasteDest}).
     * Schrabidium ore rolls and chunk rad are omitted until those systems land.
     */
    public static void waste(Level level, int x, int y, int z, int radius) {
        if (level.isClientSide || radius <= 0) {
            return;
        }
        int r2 = radius * radius;
        int r22 = r2 / 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -radius; xx < radius; xx++) {
            for (int yy = -radius; yy < radius; yy++) {
                for (int zz = -radius; zz < radius; zz++) {
                    int dist = xx * xx + yy * yy + zz * zz;
                    if (dist >= r22 + level.random.nextInt(Math.max(1, r22 / 5))) {
                        continue;
                    }
                    cursor.set(x + xx, y + yy, z + zz);
                    if (level.isInWorldBounds(cursor) && !level.getBlockState(cursor).isAir()) {
                        wasteDest(level, cursor.immutable());
                    }
                }
            }
        }
    }

    /**
     * Terrain waste without schrabidium ore rolls (legacy {@code wasteNoSchrab}).
     * Used by multi-bomb poison warheads.
     */
    public static void wasteNoSchrab(Level level, int x, int y, int z, int radius) {
        if (level.isClientSide || radius <= 0) {
            return;
        }
        int r2 = radius * radius;
        int r22 = r2 / 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -radius; xx < radius; xx++) {
            for (int yy = -radius; yy < radius; yy++) {
                for (int zz = -radius; zz < radius; zz++) {
                    int dist = xx * xx + yy * yy + zz * zz;
                    if (dist >= r22 + level.random.nextInt(Math.max(1, r22 / 5))) {
                        continue;
                    }
                    cursor.set(x + xx, y + yy, z + zz);
                    if (level.isInWorldBounds(cursor) && !level.getBlockState(cursor).isAir()) {
                        wasteDestNoSchrab(level, cursor.immutable());
                    }
                }
            }
        }
    }

    public static void wasteDestNoSchrab(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (state.is(Blocks.GLASS) || state.is(BlockTags.IMPERMEABLE)
                || block instanceof DoorBlock || block instanceof TrapDoorBlock
                || state.is(BlockTags.LEAVES)) {
            level.removeBlock(pos, false);
            return;
        }
        if (state.is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos, ModBlocks.WASTE_EARTH.get().defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.MYCELIUM)) {
            level.setBlock(pos, ModBlocks.WASTE_EARTH.get().defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.SAND) && level.random.nextInt(20) == 1) {
            level.setBlock(pos, ModBlocks.WASTE_TRINITITE.get().defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.RED_SAND) && level.random.nextInt(20) == 1) {
            level.setBlock(pos, ModBlocks.WASTE_TRINITITE_RED.get().defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.CLAY)) {
            level.setBlock(pos, Blocks.TERRACOTTA.defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.MOSSY_COBBLESTONE)) {
            level.setBlock(pos, Blocks.COAL_ORE.defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
            int rand = level.random.nextInt(30);
            if (rand >= 1 && rand <= 3) {
                level.setBlock(pos, Blocks.DIAMOND_ORE.defaultBlockState(), 2);
            } else if (rand == 29) {
                level.setBlock(pos, Blocks.EMERALD_ORE.defaultBlockState(), 2);
            }
            return;
        }
        if (state.is(BlockTags.LOGS)) {
            Block wasteLog = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("hbm", "waste_log"));
            if (wasteLog != null && wasteLog != Blocks.AIR) {
                level.setBlock(pos, wasteLog.defaultBlockState(), 2);
            } else {
                level.setBlock(pos, ModBlocks.WASTE_PLANKS.get().defaultBlockState(), 2);
            }
            return;
        }
        if (state.is(BlockTags.PLANKS) && !state.is(ModBlocks.WASTE_PLANKS.get())) {
            level.setBlock(pos, ModBlocks.WASTE_PLANKS.get().defaultBlockState(), 2);
        }
    }

    public static void wasteDest(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof DoorBlock || block instanceof TrapDoorBlock) {
            level.removeBlock(pos, false);
            return;
        }
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM)) {
            level.setBlock(pos, ModBlocks.WASTE_EARTH.get().defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.SAND) && level.random.nextInt(20) == 1) {
            level.setBlock(pos, ModBlocks.WASTE_TRINITITE.get().defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.RED_SAND) && level.random.nextInt(20) == 1) {
            level.setBlock(pos, ModBlocks.WASTE_TRINITITE_RED.get().defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.CLAY)) {
            level.setBlock(pos, Blocks.TERRACOTTA.defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.MOSSY_COBBLESTONE)) {
            level.setBlock(pos, Blocks.COAL_ORE.defaultBlockState(), 2);
            return;
        }
        if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
            int rand = level.random.nextInt(10);
            if (rand >= 1 && rand <= 3) {
                level.setBlock(pos, Blocks.DIAMOND_ORE.defaultBlockState(), 2);
            } else if (rand == 9) {
                level.setBlock(pos, Blocks.EMERALD_ORE.defaultBlockState(), 2);
            }
            return;
        }
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) {
            if (!state.is(ModBlocks.WASTE_PLANKS.get())) {
                level.setBlock(pos, ModBlocks.WASTE_PLANKS.get().defaultBlockState(), 2);
            }
            return;
        }
        if (block == ModBlocks.ORE_URANIUM.get()) {
            if (level.random.nextInt(100) == 1) {
                level.setBlock(pos, ModBlocks.ORE_SCHRABIDIUM.get().defaultBlockState(), 2);
            } else {
                level.setBlock(pos, ModBlocks.ORE_URANIUM_SCORCHED.get().defaultBlockState(), 2);
            }
        }
    }

    /**
     * Solinium rinse conversion (legacy {@code ExplosionNukeGeneric.solinium}).
     * Strips foliage/organic and turns waste/grass into dirt.
     */
    public static void solinium(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM) || block == ModBlocks.WASTE_EARTH.get()) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
            return;
        }
        if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.CROPS)
                || state.is(BlockTags.REPLACEABLE) || state.is(Blocks.CACTUS) || state.is(Blocks.VINE)
                || state.is(Blocks.SUGAR_CANE) || state.is(Blocks.BAMBOO) || state.is(Blocks.COCOA)
                || state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN)) {
            level.removeBlock(pos, false);
        }
    }

    private static boolean drainEnergy(BlockEntity be, Direction side) {
        return be.getCapability(ForgeCapabilities.ENERGY, side).map(storage -> {
            int stored = storage.getEnergyStored();
            if (stored <= 0) {
                return false;
            }
            int extracted = storage.extractEnergy(stored, false);
            if (extracted < stored && storage instanceof ModEnergyStorage mod) {
                mod.setEnergy(0);
                return true;
            }
            return extracted > 0 || storage.getEnergyStored() == 0;
        }).orElse(false);
    }

    private static boolean isExplosionExempt(Entity entity) {
        if (entity instanceof ItemEntity) {
            return true;
        }
        if (entity instanceof EntityNukeExplosionMK5
                || entity instanceof EntityNukeExplosionMK3
                || entity instanceof EntityBalefire
                || entity instanceof EntityFalloutRain
                || entity instanceof EntityNukeTorex) {
            return true;
        }
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return true;
        }
        return false;
    }
}
