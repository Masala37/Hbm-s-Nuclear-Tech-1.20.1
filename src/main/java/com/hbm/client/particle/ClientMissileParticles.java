package com.hbm.client.particle;

import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.blockentity.machine.LaunchPadLargeBlockEntity;
import com.hbm.blockentity.machine.LaunchPadRustedBlockEntity;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Client-only missile exhaust / launch plume (legacy {@code missileContrail} + {@code launchSmoke}).
 */
@OnlyIn(Dist.CLIENT)
public final class ClientMissileParticles {
    private ClientMissileParticles() {
    }

    /**
     * Fuel-colored exhaust for assembled custom missiles. Xenon emits nothing.
     */
    public static void spawnFuelContrail(EntityMissileCustom missile) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        ItemCustomMissilePart fuselage = missile.fuselage();
        if (fuselage == null || fuselage.attributes == null
                || !(fuselage.attributes[0] instanceof FuelType type)) {
            return;
        }
        float r;
        float g;
        float b;
        switch (type) {
            case KEROSENE -> {
                r = 0.0F;
                g = 0.0F;
                b = 0.0F;
            }
            case SOLID -> {
                r = 0.3F;
                g = 0.2F;
                b = 0.05F;
            }
            case HYDROGEN -> {
                r = 0.7F;
                g = 0.7F;
                b = 0.7F;
            }
            case BALEFIRE -> {
                r = 0.2F;
                g = 0.7F;
                b = 0.2F;
            }
            default -> {
                return;
            }
        }
        Vec3 motion = missile.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-12D) {
            motion = new Vec3(0.0D, 1.0D, 0.0D);
        }
        Vec3 v = motion.normalize();
        double vel = missile.getFlightVelocity();
        double x = missile.getX();
        double y = missile.getY();
        double z = missile.getZ();
        for (int i = 0; i < vel; i++) {
            mc.particleEngine.add(new ParticleContrail(
                    level, x - v.x * i, y - v.y * i, z - v.z * i, r, g, b, 1.0F));
        }
    }

    /** Legacy {@code ABMContrail} — default black ParticleContrail. */
    public static void spawnAbmContrail(com.hbm.entity.missile.EntityMissileAntiBallistic missile) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Vec3 motion = missile.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-12D) {
            motion = new Vec3(0.0D, 1.0D, 0.0D);
        }
        Vec3 v = motion.normalize();
        mc.particleEngine.add(new ParticleContrail(
                level,
                missile.getX() - v.x,
                missile.getY() - v.y,
                missile.getZ() - v.z,
                0.0F, 0.0F, 0.0F, 1.0F));
    }

    /** Preset-missile exhaust ({@code missileContrail} / {@link ParticleRocketFlame}). */
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

        // Legacy uses getContrailScale as-is (Tier-1 / micro = 0.5). Do not clamp up to 1.
        float scale = missile.getContrailScalePublic();
        if (scale <= 0.0F) {
            scale = 1.0F;
        }
        int steps = Math.max(Math.min((int) Math.ceil(len), 10), 1);
        Vec3[] offsets = missile.contrailOffsets();
        Quaternionf orient = noseToThrust(thrust);

        for (Vec3 offset : offsets) {
            Vector3f world = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
            orient.transform(world);
            for (int i = 0; i < steps; i++) {
                double j = i - len;
                double px = missile.getX() - dx * j + world.x;
                double py = missile.getY() - dy * j + world.y;
                double pz = missile.getZ() - dz * j + world.z;
                ParticleRocketFlame fx = new ParticleRocketFlame(level, px, py, pz)
                        .setScale(scale)
                        .setMaxAge(60 + level.random.nextInt(20));
                fx.setMotion(-thrust.x, -thrust.y, -thrust.z);
                mc.particleEngine.add(fx);
            }
        }
    }

    /**
     * Legacy {@code launchSmoke} — 15 ground-hugging {@link ParticleSmokePlume}s with
     * horizontal motion only (moY = 0). Silo/rusted: facing ± opposite ± rotate UP;
     * large pad: facing ± opposite only.
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

        Direction facing = Direction.NORTH;
        boolean perpendicular = true;
        BlockEntity be = level.getBlockEntity(padPos);
        if (be instanceof LaunchPadLargeBlockEntity large) {
            facing = large.getFacing();
            perpendicular = false;
        } else if (be instanceof LaunchPadBlockEntity silo) {
            facing = silo.getFacing();
        } else if (be instanceof LaunchPadRustedBlockEntity rusted) {
            facing = rusted.getFacing();
        }

        for (int i = 0; i < 15; i++) {
            Direction dir = facing;
            if (rand.nextBoolean()) {
                dir = dir.getOpposite();
            }
            if (perpendicular && rand.nextBoolean()) {
                dir = dir.getCounterClockWise();
            }
            float moX = (float) (rand.nextGaussian() * 0.15F + 0.75F) * dir.getStepX();
            float moZ = (float) (rand.nextGaussian() * 0.15F + 0.75F) * dir.getStepZ();

            ParticleSmokePlume plume = new ParticleSmokePlume(level, x, y, z);
            plume.setMotion(moX, 0.0D, moZ);
            mc.particleEngine.add(plume);
        }
    }

    /**
     * Compact launcher / launch table: 15 plumes with motion on one axis only.
     */
    public static void spawnCustomLauncherSmoke(BlockPos padPos, float spread) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        double x = padPos.getX() + 0.5D;
        double y = padPos.getY() + 0.25D;
        double z = padPos.getZ() + 0.5D;
        RandomSource rand = level.random;
        for (int i = 0; i < 15; i++) {
            boolean dir = rand.nextBoolean();
            float moX = dir ? 0.0F : (float) (rand.nextGaussian() * spread);
            float moZ = !dir ? 0.0F : (float) (rand.nextGaussian() * spread);
            ParticleSmokePlume plume = new ParticleSmokePlume(level, x, y, z);
            plume.setMotion(moX, 0.0D, moZ);
            mc.particleEngine.add(plume);
        }
    }

    /**
     * Same orientation as {@code RenderMissile}: local +Y is the nose, so engine
     * offsets in missile space follow the airframe instead of entity yaw/pitch
     * (yaw faces the target; the mesh follows velocity).
     */
    private static Quaternionf noseToThrust(Vec3 thrust) {
        Vector3f to = new Vector3f((float) thrust.x, (float) thrust.y, (float) thrust.z);
        if (to.lengthSquared() < 1.0E-8F) {
            return new Quaternionf();
        }
        to.normalize();
        return new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), to);
    }

    /** True when a flying missile is still in the legacy pad smoke AABB. */
    public static boolean hasMissileNearPad(ClientLevel level, BlockPos pos) {
        AABB box = new AABB(
                pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                pos.getX() + 1.5D, pos.getY() + 10.0D, pos.getZ() + 1.5D);
        return !level.getEntitiesOfClass(EntityMissileBaseNT.class, box).isEmpty();
    }

    public static boolean hasCustomMissileNearPad(ClientLevel level, BlockPos pos) {
        AABB box = new AABB(
                pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                pos.getX() + 1.5D, pos.getY() + 10.0D, pos.getZ() + 1.5D);
        return !level.getEntitiesOfClass(EntityMissileCustom.class, box).isEmpty();
    }
}
