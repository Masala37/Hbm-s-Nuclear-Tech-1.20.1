package com.hbm.client.particle;

import com.hbm.registry.ModSounds;
import com.hbm.wiaj.WorldInAJar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side legacy {@code ExplosionCreator} / {@code ExplosionSmallCreator} particle suites.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientExplosionEffects {
    /** Legacy {@code ExplosionCreator.speedOfSound} (half real SoS in blocks/tick). */
    private static final double SPEED_OF_SOUND = 17.15D * 0.5D;

    private ClientExplosionEffects() {
    }

    /** Legacy {@code ExplosionCreator.composeEffectSmall}. */
    public static void composeEffectSmall(double x, double y, double z) {
        composeEffect(x, y, z, 10, 2.0F, 0.5F, 25.0F,
                5, 8, 20, 0.75F, 1.0F, -2.0F, 150.0F);
    }

    /** Legacy {@code ExplosionCreator.composeEffectStandard}. */
    public static void composeEffectStandard(double x, double y, double z) {
        composeEffect(x, y, z, 15, 5.0F, 1.0F, 45.0F,
                10, 16, 50, 1.0F, 3.0F, -2.0F, 200.0F);
    }

    /** Legacy {@code ExplosionCreator.composeEffectLarge}. */
    public static void composeEffectLarge(double x, double y, double z) {
        composeEffect(x, y, z, 30, 6.5F, 2.0F, 65.0F,
                25, 16, 50, 1.25F, 3.0F, -2.0F, 350.0F);
    }

    public static void composeEffect(double x, double y, double z,
                                     int cloudCount, float cloudScale, float cloudSpeedMult,
                                     float waveScale,
                                     int debrisCount, int debrisSize, int debrisRetry,
                                     float debrisVelocity, float debrisHorizontalDeviation,
                                     float debrisVerticalOffset, float soundRange) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }

        playLargeExplosionBang(x, y, z, soundRange);

        ParticleMukeWave wave = new ParticleMukeWave(level, x, y + 2.0D, z)
                .setup(waveScale, (int) (25.0F * waveScale / 45.0F));
        mc.particleEngine.add(wave);

        RandomSource rand = level.random;
        for (int i = 0; i < cloudCount; i++) {
            ParticleRocketFlame fx = new ParticleRocketFlame(level, x, y, z)
                    .setScale(cloudScale)
                    .setMaxAge(70 + rand.nextInt(20));
            fx.setMotion(
                    rand.nextGaussian() * 0.5D * cloudSpeedMult,
                    rand.nextDouble() * 3.0D * cloudSpeedMult,
                    rand.nextGaussian() * 0.5D * cloudSpeedMult);
            mc.particleEngine.add(fx);
        }

        spawnDebris(level, x, y, z, debrisCount, debrisSize, debrisRetry,
                debrisVelocity, debrisHorizontalDeviation, debrisVerticalOffset, rand);
    }

    /**
     * Legacy {@code ExplosionCreator} debris loop — sample nearby blocks into a WorldInAJar
     * and launch {@link ParticleDebris}.
     */
    private static void spawnDebris(ClientLevel level, double x, double y, double z,
                                    int debrisCount, int debrisSize, int debrisRetry,
                                    float debrisVelocity, float debrisHorizontalDeviation,
                                    float debrisVerticalOffset, RandomSource rand) {
        if (debrisCount <= 0 || debrisSize <= 0) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        for (int c = 0; c < debrisCount; c++) {
            double oX = rand.nextGaussian() * debrisHorizontalDeviation;
            double oY = debrisVerticalOffset;
            double oZ = rand.nextGaussian() * debrisHorizontalDeviation;
            int cX = Mth.floor(x + oX + 0.5D);
            int cY = Mth.floor(y + oY + 0.5D);
            int cZ = Mth.floor(z + oZ + 0.5D);

            // Legacy Vec3: (vel,0,0).rotateAroundZ(angle) then rotateAroundY(yaw)
            // rotateAroundZ: x'=x*c+y*s, y'=y*c-x*s  → for (vel,0): y' = -vel*sin(angle)
            // angle is negative (−45..−70°), so Y is upward.
            double angle = -Math.toRadians(45.0D + rand.nextFloat() * 25.0D);
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);
            double mx0 = debrisVelocity * cosA;
            double my0 = -debrisVelocity * sinA;
            double mz0 = 0.0D;
            double yawRad = rand.nextDouble() * Math.PI * 2.0D;
            double cosY = Math.cos(yawRad);
            double sinY = Math.sin(yawRad);
            double mx = mx0 * cosY - mz0 * sinY;
            double my = my0;
            double mz = mx0 * sinY + mz0 * cosY;

            ParticleDebris particle = new ParticleDebris(level, x, y, z, mx, my, mz);
            WorldInAJar wiaj = new WorldInAJar(debrisSize, debrisSize, debrisSize);
            particle.jar = wiaj;

            int middle = debrisSize / 2 - 1;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 2; k++) {
                        BlockPos sample = new BlockPos(cX + i, cY + j, cZ + k);
                        wiaj.setBlock(middle + i, middle + j, middle + k, safeState(level, sample));
                    }
                }
            }

            for (int layer = 2; layer <= (debrisSize / 2); layer++) {
                for (int i = 0; i < debrisRetry; i++) {
                    int jx = -layer + rand.nextInt(layer * 2 + 1);
                    int jy = -layer + rand.nextInt(layer * 2 + 1);
                    int jz = -layer + rand.nextInt(layer * 2 + 1);

                    boolean adjacent =
                            !wiaj.isAir(middle + jx + 1, middle + jy, middle + jz)
                                    || !wiaj.isAir(middle + jx - 1, middle + jy, middle + jz)
                                    || !wiaj.isAir(middle + jx, middle + jy + 1, middle + jz)
                                    || !wiaj.isAir(middle + jx, middle + jy - 1, middle + jz)
                                    || !wiaj.isAir(middle + jx, middle + jy, middle + jz + 1)
                                    || !wiaj.isAir(middle + jx, middle + jy, middle + jz - 1);
                    if (adjacent) {
                        BlockPos sample = new BlockPos(cX + jx, cY + jy, cZ + jz);
                        wiaj.setBlock(middle + jx, middle + jy, middle + jz, safeState(level, sample));
                    }
                }
            }

            mc.particleEngine.add(particle);
        }
    }

    private static BlockState safeState(ClientLevel level, BlockPos pos) {
        if (!level.isInWorldBounds(pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return level.getBlockState(pos);
    }

    /**
     * Legacy {@code ExplosionSmallCreator.composeEffect} — weapon/cluster bomblet bang.
     * Params match {@code ExplosionEffectWeapon(cloudCount, cloudScale, cloudSpeedMult)}.
     */
    public static void composeEffectSmallWeapon(double x, double y, double z,
                                                int cloudCount, float cloudScale, float cloudSpeedMult) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }

        float soundRange = 200.0F;
        double dist = Math.sqrt(mc.player.distanceToSqr(x, y, z));
        if (dist <= soundRange) {
            SoundEvent sound = dist <= soundRange * 0.4D
                    ? ModSounds.EXPLOSION_SMALL_NEAR.get()
                    : ModSounds.EXPLOSION_SMALL_FAR.get();
            float pitch = 0.9F + level.random.nextFloat() * 0.2F;
            mc.getSoundManager().playDelayed(
                    carrying(sound, SoundSource.BLOCKS, 1.0F, pitch, x, y, z),
                    Math.max(0, (int) (dist / SPEED_OF_SOUND)));
        }

        for (int i = 0; i < cloudCount; i++) {
            mc.particleEngine.add(new ParticleExplosionSmall(level, x, y, z, cloudScale, cloudSpeedMult));
        }

        // Legacy samples a neighboring solid for block-dust debris (15×).
        BlockPos origin = BlockPos.containing(x, y, z);
        BlockState sample = level.getBlockState(origin);
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(origin.relative(dir));
            if (!neighbor.isAir()) {
                sample = neighbor;
                break;
            }
        }
        if (!sample.isAir()) {
            RandomSource rand = level.random;
            BlockParticleOption option = new BlockParticleOption(ParticleTypes.BLOCK, sample);
            for (int i = 0; i < 15; i++) {
                level.addParticle(option, x, y + 0.1D, z,
                        rand.nextGaussian() * 0.2D,
                        0.5D + rand.nextDouble() * 0.7D,
                        rand.nextGaussian() * 0.2D);
            }
        }
    }

    /**
     * Legacy ClientProxy {@code type=smoke} cloud/radial — {@link ParticleExSmoke} burst.
     */
    public static void spawnExSmoke(double x, double y, double z, int count, boolean radial,
                                     boolean playBang) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || count <= 0) {
            return;
        }
        if (playBang && !radial) {
            playLargeExplosionBang(x, y, z, 150.0F);
        }
        int n = Math.max(1, count);
        RandomSource rand = level.random;
        for (int i = 0; i < n; i++) {
            ParticleExSmoke fx = new ParticleExSmoke(level, x, y, z);
            double my;
            double mx;
            double mz;
            if (radial) {
                my = rand.nextGaussian() * (1.0D + (n / 50.0D));
                mx = rand.nextGaussian() * (1.0D + (n / 50.0D));
                mz = rand.nextGaussian() * (1.0D + (n / 50.0D));
            } else {
                my = rand.nextGaussian() * (1.0D + (n / 100.0D));
                mx = rand.nextGaussian() * (1.0D + (n / 150.0D));
                mz = rand.nextGaussian() * (1.0D + (n / 150.0D));
                if (rand.nextBoolean()) {
                    my = Math.abs(my);
                }
            }
            fx.setMotion(mx, my, mz);
            mc.particleEngine.add(fx);
        }
    }

    /** SoS-delayed near/far bang used by composeEffect and bunker-buster cloud FX. */
    public static void playLargeExplosionBang(double x, double y, double z, float soundRange) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        double dist = Math.sqrt(mc.player.distanceToSqr(x, y, z));
        if (dist > soundRange) {
            return;
        }
        SoundEvent sound = dist <= soundRange * 0.4D
                ? ModSounds.EXPLOSION_LARGE_NEAR.get()
                : ModSounds.EXPLOSION_LARGE_FAR.get();
        float pitch = 0.9F + level.random.nextFloat() * 0.2F;
        mc.getSoundManager().playDelayed(
                carrying(sound, SoundSource.BLOCKS, 1.0F, pitch, x, y, z),
                Math.max(0, (int) (dist / SPEED_OF_SOUND)));
    }

    /**
     * Pad takeoff. 1.7.10 {@code playSoundEffect} volume 2 → ~32 blocks.
     */
    public static void playMissileTakeoff(double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (mc.player.distanceToSqr(x, y, z) > 32.0D * 32.0D) {
            return;
        }
        mc.getSoundManager().play(carrying(
                ModSounds.MISSILE_TAKEOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F, x, y, z));
    }

    /**
     * Shuttle {@code robin_explosion}. 1.7.10 volume 4 → ~64 blocks.
     */
    public static void playRobinExplosion(double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        if (mc.player.distanceToSqr(x, y, z) > 64.0D * 64.0D) {
            return;
        }
        float pitch = (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F;
        mc.getSoundManager().play(carrying(
                ModSounds.ROBIN_EXPLOSION.get(), SoundSource.PLAYERS, 1.0F, pitch, x, y, z));
    }

    /** Torex shock-front boom (1.7.10 {@code playSoundClient} nuclearExplosion). */
    public static boolean playNuclearExplosionIfInRange(double x, double y, double z, double hearRange) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        if (mc.player.distanceToSqr(x, y, z) >= hearRange * hearRange) {
            return false;
        }
        mc.getSoundManager().play(carrying(
                ModSounds.NUCLEAR_EXPLOSION.get(), SoundSource.BLOCKS, 1.0F, 1.0F, x, y, z));
        return true;
    }

    /** Positional SFX that still carries after 1.20's volume clamp (caller gates distance). */
    private static SimpleSoundInstance carrying(SoundEvent sound, SoundSource source,
                                                float volume, float pitch,
                                                double x, double y, double z) {
        return new SimpleSoundInstance(
                sound.getLocation(), source, volume, pitch, RandomSource.create(),
                false, 0, SoundInstance.Attenuation.NONE, x, y, z, false);
    }

    /**
     * Legacy AuxParticle {@code type=rbmkmush} — 30-frame additive mushroom strip.
     */
    public static void spawnRbmkMush(double x, double y, double z, float scale) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        mc.particleEngine.add(new ParticleRBMKMush(level, x, y, z, scale));
        playRobinExplosion(x, y, z);
    }

    /**
     * Legacy AuxParticle {@code type=muke}: shockwave + flash (which seeds mushroom clouds).
     * Also applies a short hurt-cam punch like legacy ClientProxy.
     */
    public static void spawnMuke(double x, double y, double z, boolean balefire) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        mc.particleEngine.add(new ParticleMukeWave(level, x, y, z));
        mc.particleEngine.add(new ParticleMukeFlash(level, x, y, z, balefire));
        if (Math.sqrt(mc.player.distanceToSqr(x, y, z)) <= 240.0D) {
            mc.getSoundManager().play(carrying(
                    ModSounds.MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 1.0F, 1.0F, x, y, z));
        }
        // Legacy: player.hurtTime = 15; player.maxHurtTime = 15;
        mc.player.hurtTime = 15;
        mc.player.hurtDuration = 15;
    }
}
