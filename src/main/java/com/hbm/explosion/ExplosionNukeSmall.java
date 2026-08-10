package com.hbm.explosion;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * Compact nuke/muke blasts (legacy {@code ExplosionNukeSmall}).
 */
public final class ExplosionNukeSmall {
    private ExplosionNukeSmall() {
    }

    public static final MukeParams PARAMS_SAFE = new MukeParams()
            .safe(true).killRadius(45.0F).radiationLevel(2.0F);
    public static final MukeParams PARAMS_TOTS = new MukeParams()
            .blastRadius(10.0F).killRadius(30.0F).shrapnelCount(0).resolution(32).radiationLevel(1.0F);
    public static final MukeParams PARAMS_LOW = new MukeParams()
            .blastRadius(15.0F).killRadius(45.0F).radiationLevel(2.0F);
    public static final MukeParams PARAMS_MEDIUM = new MukeParams()
            .blastRadius(20.0F).killRadius(55.0F).radiationLevel(3.0F);
    public static final MukeParams PARAMS_HIGH = new MukeParams()
            .miniNuke(false).blastRadius(80.0F).shrapnelCount(0);

    public static void explode(Level level, double x, double y, double z, MukeParams params) {
        if (level.isClientSide) {
            return;
        }

        level.playSound(null, x, y, z, ModSounds.MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 15.0F, 1.0F);

        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLASH, x, y + 0.5D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 1.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            int sparks = Math.max(0, params.shrapnelCount);
            if (sparks > 0) {
                server.sendParticles(ParticleTypes.FIREWORK, x, y + 0.5D, z, sparks, 1.5D, 1.0D, 1.5D, 0.15D);
            }
        }

        if (params.shrapnelCount > 0) {
            ExplosionLarge.spawnShrapnels(level, x, y, z, params.shrapnelCount);
        }

        if (params.miniNuke && !params.safe) {
            ExplosionNT dig = new ExplosionNT(level, null, x, y, z, params.blastRadius)
                    .overrideResolution(params.resolution);
            for (ExplosionNT.ExAttrib attrib : params.explosionAttribs) {
                dig.addAttrib(attrib);
            }
            dig.explode();
            EntityNukeTorex.statFacStandard(level, x, y + 0.5D, z, params.blastRadius);
        }

        if (params.killRadius > 0.0F) {
            ExplosionNukeGeneric.dealDamage(level, x, y, z, params.killRadius);
        }

        if (!params.miniNuke) {
            float radius = params.blastRadius > 0.0F
                    ? params.blastRadius
                    : BombConfig.fatmanRadius.get();
            com.hbm.entity.logic.EntityNukeExplosionMK5.statFac(level, (int) radius, x, y, z);
        }

        // Legacy mini-nuke: spread rad into a diamond of neighboring chunks
        if (params.miniNuke && params.radiationLevel > 0.0F) {
            float radMod = params.radiationLevel / 3.0F;
            int ix = (int) Math.floor(x);
            int iy = (int) Math.floor(y);
            int iz = (int) Math.floor(z);
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    if (Math.abs(i) + Math.abs(j) < 4) {
                        float amount = (50.0F / (Math.abs(i) + Math.abs(j) + 1)) * radMod;
                        ChunkRadiationManager.INSTANCE.incrementRad(
                                level, ix + i * 16, iy, iz + j * 16, amount);
                    }
                }
            }
        }
    }

    public static final class MukeParams {
        public boolean miniNuke = true;
        public boolean safe = false;
        public float blastRadius = 15.0F;
        public float killRadius = 45.0F;
        public float radiationLevel = 1.0F;
        public int shrapnelCount = 25;
        public int resolution = 64;
        public ExplosionNT.ExAttrib[] explosionAttribs = new ExplosionNT.ExAttrib[]{
                ExplosionNT.ExAttrib.FIRE,
                ExplosionNT.ExAttrib.NOPARTICLE,
                ExplosionNT.ExAttrib.NOSOUND,
                ExplosionNT.ExAttrib.NODROP,
                ExplosionNT.ExAttrib.NOHURT
        };

        public MukeParams miniNuke(boolean value) {
            this.miniNuke = value;
            return this;
        }

        public MukeParams safe(boolean value) {
            this.safe = value;
            return this;
        }

        public MukeParams blastRadius(float value) {
            this.blastRadius = value;
            return this;
        }

        public MukeParams killRadius(float value) {
            this.killRadius = value;
            return this;
        }

        public MukeParams radiationLevel(float value) {
            this.radiationLevel = value;
            return this;
        }

        public MukeParams shrapnelCount(int value) {
            this.shrapnelCount = value;
            return this;
        }

        public MukeParams resolution(int value) {
            this.resolution = value;
            return this;
        }
    }
}
