package com.hbm.explosion;

import com.hbm.entity.projectile.EntityRubble;
import com.hbm.entity.projectile.EntityShrapnel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * Lite port of legacy {@code ExplosionLarge} for bomb callers.
 */
public final class ExplosionLarge {
    private static final Random RAND = new Random();

    private ExplosionLarge() {
    }

    public static void explode(Level level, double x, double y, double z, float strength,
                               boolean cloud, boolean rubble, boolean shrapnel) {
        explode(level, x, y, z, strength, cloud, rubble, shrapnel, null);
    }

    public static void explode(Level level, double x, double y, double z, float strength,
                               boolean cloud, boolean rubble, boolean shrapnel, Entity exploder) {
        if (level.isClientSide) {
            return;
        }
        level.explode(exploder, x, y, z, strength, true, Level.ExplosionInteraction.TNT);
        if (cloud) {
            spawnCloud(level, x, y, z, cloudFunction((int) strength));
        }
        if (rubble) {
            spawnRubble(level, x, y, z, rubbleFunction((int) strength));
        }
        if (shrapnel) {
            spawnShrapnels(level, x, y, z, shrapnelFunction((int) strength));
        }
    }

    public static void explodeFire(Level level, double x, double y, double z, float strength,
                                   boolean cloud, boolean rubble, boolean shrapnel) {
        if (level.isClientSide) {
            return;
        }
        level.explode(null, x, y, z, strength, true, Level.ExplosionInteraction.TNT);
        if (cloud) {
            spawnCloud(level, x, y, z, cloudFunction((int) strength));
        }
        if (rubble) {
            spawnRubble(level, x, y, z, rubbleFunction((int) strength));
        }
        if (shrapnel) {
            spawnShrapnels(level, x, y, z, shrapnelFunction((int) strength));
        }
    }

    public static void spawnRubble(Level level, double x, double y, double z, int count) {
        if (level.isClientSide || count <= 0) {
            return;
        }
        BlockPos origin = BlockPos.containing(x, y, z);
        for (int i = 0; i < count; i++) {
            EntityRubble rubble = new EntityRubble(level, x, y, z);
            double scale = 1.0D + (count / 50.0D);
            rubble.setDeltaMovement(
                    RAND.nextGaussian() * 0.75D * scale,
                    0.75D * (1.0D + ((count + RAND.nextInt(Math.max(1, count * 5))) / 25.0D)),
                    RAND.nextGaussian() * 0.75D * scale);
            rubble.setBasedOnBlock(sampleRubbleBlock(level, origin));
            level.addFreshEntity(rubble);
        }
    }

    /** Prefer a nearby solid block's appearance; fall back to stone. */
    private static Block sampleRubbleBlock(Level level, BlockPos origin) {
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos sample = origin.offset(
                    RAND.nextInt(5) - 2,
                    RAND.nextInt(3) - 1,
                    RAND.nextInt(5) - 2);
            BlockState state = level.getBlockState(sample);
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                continue;
            }
            if (state.getDestroySpeed(level, sample) < 0.0F) {
                continue; // unbreakable (bedrock)
            }
            if (state.canOcclude() || state.isSolidRender(level, sample)) {
                return state.getBlock();
            }
        }
        return Blocks.STONE;
    }

    public static void spawnShrapnels(Level level, double x, double y, double z, int count) {
        if (level.isClientSide || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            EntityShrapnel shrapnel = new EntityShrapnel(level, x, y, z);
            double scale = 1.0D + (count / 50.0D);
            shrapnel.setDeltaMovement(
                    RAND.nextGaussian() * scale,
                    ((RAND.nextFloat() * 0.5F) + 0.5F) * (1.0D + (count / (15.0D + RAND.nextInt(21))))
                            + (RAND.nextFloat() / 50.0F * count),
                    RAND.nextGaussian() * scale);
            shrapnel.setTrail(RAND.nextInt(3) == 0);
            level.addFreshEntity(shrapnel);
        }
    }

    public static void spawnShrapnelShower(Level level, double x, double y, double z,
                                           double motionX, double motionY, double motionZ,
                                           int count, double deviation) {
        if (level.isClientSide || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            EntityShrapnel shrapnel = new EntityShrapnel(level, x, y, z);
            shrapnel.setDeltaMovement(
                    motionX + RAND.nextGaussian() * deviation,
                    motionY + RAND.nextGaussian() * deviation,
                    motionZ + RAND.nextGaussian() * deviation);
            shrapnel.setTrail(RAND.nextInt(3) == 0);
            level.addFreshEntity(shrapnel);
        }
    }

    private static void spawnCloud(Level level, double x, double y, double z, int count) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        int n = Math.min(count, 80);
        server.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5D, z, n, 1.2D, 0.8D, 1.2D, 0.05D);
        server.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5D, z, Math.max(2, n / 20), 0.5D, 0.5D, 0.5D, 0.0D);
    }

    public static int cloudFunction(int i) {
        return (int) (850 * (1 - Math.pow(Math.E, -i / 15.0)) + 15);
    }

    public static int rubbleFunction(int i) {
        return i / 10;
    }

    public static int shrapnelFunction(int i) {
        return i / 3;
    }
}
