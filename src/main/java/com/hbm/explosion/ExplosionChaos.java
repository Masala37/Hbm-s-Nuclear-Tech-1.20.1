package com.hbm.explosion;

import com.hbm.entity.projectile.EntityClusterBomblet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Chaos / physics helpers from legacy {@code ExplosionChaos}.
 */
public final class ExplosionChaos {
    private ExplosionChaos() {
    }

    /**
     * Fire cluster bomblets (legacy catapult cluster submunitions).
     */
    public static void cluster(Level level, double x, double y, double z, int count,
                               float yaw, float pitch, float yawRand, float pitchRand, float speed) {
        if (level.isClientSide || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            float yRot = yaw + (float) (yawRand * level.random.nextGaussian());
            float xRot = pitch + (float) (pitchRand * level.random.nextGaussian());
            // Treat pitch like a spherical polar angle (legacy multi uses ~π/2 = upward).
            double mx = Math.sin(yRot) * Math.sin(xRot) * speed;
            double my = Math.cos(xRot) * speed;
            // cos(π/2)=0 for exact up; add a little lift so bomblets clear the casing.
            if (my < 0.15D) {
                my = 0.25D + level.random.nextDouble() * 0.55D;
            }
            double mz = Math.cos(yRot) * Math.sin(xRot) * speed;
            EntityClusterBomblet bomblet = new EntityClusterBomblet(level, x, y, z);
            bomblet.setDeltaMovement(mx, my, mz);
            level.addFreshEntity(bomblet);
        }
    }

    /**
     * Lift a sphere of blocks upward by {@code height} (legacy float bomb).
     * Skips air, bedrock-hardness blocks, and fluids.
     */
    public static void floater(Level level, BlockPos origin, int radius, int height) {
        if (level.isClientSide || radius <= 0 || height == 0) {
            return;
        }

        int r2 = radius * radius;
        int r22 = r2 / 2;
        List<MoveOp> moves = new ArrayList<>();

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -radius; xx < radius; xx++) {
            for (int yy = -radius; yy < radius; yy++) {
                for (int zz = -radius; zz < radius; zz++) {
                    if (xx * xx + yy * yy + zz * zz >= r22) {
                        continue;
                    }
                    cursor.set(origin.getX() + xx, origin.getY() + yy, origin.getZ() + zz);
                    if (!level.isInWorldBounds(cursor)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir() || !state.getFluidState().isEmpty()) {
                        continue;
                    }
                    float hardness = state.getDestroySpeed(level, cursor);
                    if (hardness < 0.0F) {
                        continue;
                    }

                    BlockPos from = cursor.immutable();
                    BlockPos to = from.above(height);
                    if (!level.isInWorldBounds(to)) {
                        continue;
                    }

                    CompoundTag beTag = null;
                    BlockEntity be = level.getBlockEntity(from);
                    if (be != null) {
                        beTag = be.saveWithFullMetadata();
                    }
                    moves.add(new MoveOp(from, to, state, beTag));
                }
            }
        }

        // Clear sources first so we do not clobber destination stacks mid-pass.
        for (MoveOp op : moves) {
            level.removeBlockEntity(op.from);
            level.setBlock(op.from, Blocks.AIR.defaultBlockState(), 2);
        }
        for (MoveOp op : moves) {
            level.setBlock(op.to, op.state, 2);
            if (op.beTag != null) {
                BlockEntity created = BlockEntity.loadStatic(op.to, op.state, op.beTag);
                if (created != null) {
                    level.setBlockEntity(created);
                }
            }
            level.sendBlockUpdated(op.to, op.state, op.state, 3);
        }
    }

    /**
     * Instantly displace entities in a sphere by {@code (dx, dy, dz)}.
     * Living mobs get Dinnerbone/Grumm (sheep → jeb_), matching legacy.
     */
    public static void move(Level level, double x, double y, double z, int radius, int dx, int dy, int dz) {
        if (level.isClientSide || radius <= 0) {
            return;
        }

        double wat = radius;
        double diameter = radius * 2.0D;
        AABB box = new AABB(x, y, z, x, y, z).inflate(wat + 1.0D);
        List<Entity> list = level.getEntities(null, box);
        RandomSource random = level.random;

        for (Entity entity : list) {
            double distScale = Math.sqrt(entity.distanceToSqr(x, y, z)) / diameter;
            if (distScale > 1.0D) {
                continue;
            }

            double dist = Math.sqrt(entity.distanceToSqr(x, y, z));
            if (dist >= wat) {
                continue;
            }

            if (entity instanceof LivingEntity living) {
                if (living instanceof Sheep) {
                    living.setCustomName(net.minecraft.network.chat.Component.literal("jeb_"));
                    living.setCustomNameVisible(true);
                } else {
                    living.setCustomName(net.minecraft.network.chat.Component.literal(
                            random.nextBoolean() ? "Dinnerbone" : "Grumm"));
                    living.setCustomNameVisible(true);
                }
            }

            entity.teleportTo(entity.getX() + dx, entity.getY() + dy, entity.getZ() + dz);
        }
    }

    /** Convenience overload matching legacy int block coords. */
    public static void floater(Level level, int x, int y, int z, int radius, int height) {
        floater(level, new BlockPos(x, y, z), radius, height);
    }

    public static void move(Level level, int x, int y, int z, int radius, int dx, int dy, int dz) {
        move(level, x + 0.5D, y + 0.5D, z + 0.5D, radius, dx, dy, dz);
    }

    private record MoveOp(BlockPos from, BlockPos to, BlockState state, @Nullable CompoundTag beTag) {
    }
}
