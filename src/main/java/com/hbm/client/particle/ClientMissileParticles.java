package com.hbm.client.particle;

import com.hbm.entity.missile.EntityMissileBaseNT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-only missile exhaust / launch plume (legacy {@code missileContrail} + {@code launchSmoke}).
 */
@OnlyIn(Dist.CLIENT)
public final class ClientMissileParticles {
    private ClientMissileParticles() {
    }

    /** Legacy {@code EntityMissileBaseNT.spawnContraolWithOffset} using {@link ParticleRocketFlame}. */
    public static void spawnContrail(EntityMissileBaseNT missile) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        if (cam.distanceToSqr(missile.getX(), missile.getY(), missile.getZ()) > 350.0D * 350.0D) {
            return;
        }

        double dx = missile.xo - missile.getX();
        double dy = missile.yo - missile.getY();
        double dz = missile.zo - missile.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-4D) {
            dx = 0.0D;
            dy = -1.0D;
            dz = 0.0D;
            len = 1.0D;
        }
        dx /= len;
        dy /= len;
        dz /= len;

        Vec3 motion = missile.getDeltaMovement();
        Vec3 thrust;
        if (motion.lengthSqr() > 1.0E-6D) {
            thrust = motion.normalize();
        } else {
            float yaw = missile.getYRot();
            float pitch = missile.getXRot();
            double elev = Math.toRadians(pitch + 90.0F);
            double yawRad = Math.toRadians(yaw);
            double horiz = Math.cos(elev);
            thrust = new Vec3(-Math.sin(yawRad) * horiz, Math.sin(elev), Math.cos(yawRad) * horiz);
        }

        float scale = Math.max(missile.getContrailScalePublic(), 1.0F);
        int steps = Math.max(Math.min((int) Math.ceil(len), 10), 1);

        for (int i = 0; i < steps; i++) {
            double j = i - len;
            double px = missile.getX() - dx * j;
            double py = missile.getY() - dy * j;
            double pz = missile.getZ() - dz * j;
            ParticleRocketFlame fx = new ParticleRocketFlame(level, px, py, pz)
                    .setScale(scale)
                    .setMaxAge(60 + level.random.nextInt(20));
            fx.setMotion(-thrust.x, -thrust.y, -thrust.z);
            mc.particleEngine.add(fx);
        }
    }

    /**
     * Legacy {@code launchSmoke} — 15 ground-hugging {@link ParticleSmokePlume}s with
     * horizontal motion only (moY = 0), cardinally biased like the old TE tick.
     */
    public static void spawnLaunchSmoke(BlockPos padPos) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }

        double x = padPos.getX() + 0.5D;
        double y = padPos.getY() + 0.25D;
        double z = padPos.getZ() + 0.5D;
        RandomSource rand = level.random;

        // Prefer a random horizontal axis each puff (legacy ForgeDirection ± opposite ± rotate UP).
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        for (int i = 0; i < 15; i++) {
            Direction dir = dirs[rand.nextInt(dirs.length)];
            if (rand.nextBoolean()) {
                dir = dir.getOpposite();
            }
            if (rand.nextBoolean()) {
                dir = dir.getClockWise();
            }
            float moX = (float) (rand.nextGaussian() * 0.15F + 0.75F) * dir.getStepX();
            float moZ = (float) (rand.nextGaussian() * 0.15F + 0.75F) * dir.getStepZ();

            ParticleSmokePlume plume = new ParticleSmokePlume(level, x, y, z);
            plume.setMotion(moX, 0.0D, moZ);
            mc.particleEngine.add(plume);
        }
    }

    /** True when a flying missile is still in the legacy pad smoke AABB. */
    public static boolean hasMissileNearPad(ClientLevel level, BlockPos pos) {
        AABB box = new AABB(
                pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                pos.getX() + 1.5D, pos.getY() + 10.0D, pos.getZ() + 1.5D);
        return !level.getEntitiesOfClass(EntityMissileBaseNT.class, box).isEmpty();
    }
}
