package com.hbm.explosion;

import com.hbm.entity.projectile.EntityRubble;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.network.ModMessages;
import com.hbm.network.SmokeCloudEffectPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

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

    /**
     * Legacy AuxParticle smoke {@code mode=cloud} — bunker buster / large explode FX.
     */
    public static void spawnParticles(Level level, double x, double y, double z, int count) {
        spawnParticles(level, x, y, z, count, false);
    }

    public static void spawnParticles(Level level, double x, double y, double z, int count, boolean bang) {
        if (!(level instanceof ServerLevel server) || count <= 0) {
            return;
        }
        ModMessages.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        x, y, z, 250.0D, server.dimension())),
                bang ? SmokeCloudEffectPacket.cloudBang(x, y, z, count)
                        : SmokeCloudEffectPacket.cloud(x, y, z, count));
    }

    /** Legacy AuxParticle smoke {@code mode=radial}. */
    public static void spawnParticlesRadial(Level level, double x, double y, double z, int count) {
        if (!(level instanceof ServerLevel server) || count <= 0) {
            return;
        }
        ModMessages.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        x, y, z, 250.0D, server.dimension())),
                new SmokeCloudEffectPacket(x, y, z, count, SmokeCloudEffectPacket.Mode.RADIAL));
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
        spawnParticles(level, x, y, z, count);
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

    public static void jolt(Level level, double posX, double posY, double posZ, double strength, int count, double vel) {
        if (level.isClientSide) {
            return;
        }
        for (int j = 0; j < count; j++) {
            double phi = RAND.nextDouble() * (Math.PI * 2);
            double costheta = RAND.nextDouble() * 2 - 1;
            double theta = Math.acos(costheta);
            double vx = Math.sin(theta) * Math.cos(phi);
            double vy = Math.sin(theta) * Math.sin(phi);
            double vz = Math.cos(theta);

            for (int i = 0; i < strength; i++) {
                double x0 = posX + vx * i;
                double y0 = posY + vy * i;
                double z0 = posZ + vz * i;
                BlockPos pos = BlockPos.containing(x0, y0, z0);
                if (!level.isInWorldBounds(pos)) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (!state.getFluidState().isEmpty() || state.liquid()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    continue;
                }
                if (state.isAir()) {
                    continue;
                }
                if (state.getExplosionResistance(level, pos, null) > 70.0F) {
                    continue;
                }

                EntityRubble rubble = new EntityRubble(level, x0 + 0.5D, y0 + 0.5D, z0 + 0.5D);
                rubble.setBasedOnBlock(state.getBlock());
                double dx = posX - (x0 + 0.5D);
                double dy = posY - (y0 + 0.5D);
                double dz = posZ - (z0 + 0.5D);
                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len > 1.0E-6D) {
                    dx /= len;
                    dy /= len;
                    dz /= len;
                }
                rubble.setDeltaMovement(dx * vel, dy * vel, dz * vel);
                level.addFreshEntity(rubble);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                break;
            }
        }
    }

    /** Legacy {@code ExplosionLarge.buster}: explosions every 2 blocks along the incoming vector. */
    public static void buster(Level level, double x, double y, double z, Vec3 vector, float strength, float depth) {
        if (level.isClientSide) {
            return;
        }
        Vec3 dir = vector.normalize();
        for (int i = 0; i < depth; i += 2) {
            level.explode(null, x + dir.x * i, y + dir.y * i, z + dir.z * i, strength, true,
                    Level.ExplosionInteraction.TNT);
        }
    }
}
