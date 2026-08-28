package com.hbm.explosion;

import com.hbm.config.BombConfig;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.network.ModMessages;
import com.hbm.network.MukeEffectPacket;
import com.hbm.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

/**
 * Compact nuke/muke blasts (legacy {@code ExplosionNukeSmall}).
 */
public final class ExplosionNukeSmall {
    private ExplosionNukeSmall() {
    }

    public static final MukeParams PARAMS_SAFE = new MukeParams()
            .safe(true).killRadius(45.0F).radiationLevel(2.0F);
    public static final MukeParams PARAMS_TOTS = new MukeParams()
            .blastRadius(10.0F).killRadius(30.0F).shrapnelCount(0).resolution(32).radiationLevel(1.0F)
            .particle("tinytot");
    public static final MukeParams PARAMS_LOW = new MukeParams()
            .blastRadius(15.0F).killRadius(45.0F).radiationLevel(2.0F);
    public static final MukeParams PARAMS_MEDIUM = new MukeParams()
            .blastRadius(20.0F).killRadius(55.0F).radiationLevel(3.0F);
    /**
     * Legacy: {@code miniNuke = false; blastRadius = BombConfig.fatmanRadius; shrapnelCount = 0}.
     * Default particle {@code "muke"}; killRadius stays 0 — MK5 deals entity damage.
     */
    public static final MukeParams PARAMS_HIGH = new MukeParams()
            .miniNuke(false).blastRadius(0.0F).killRadius(0.0F).shrapnelCount(0);

    public static void explode(Level level, double x, double y, double z, MukeParams params) {
        if (level.isClientSide) {
            return;
        }

        // Legacy AuxParticle type=muke (PARAMS_* default) — wave + flash + clouds.
        if (params.particle != null && "muke".equals(params.particle) && level instanceof ServerLevel server) {
            boolean balefire = server.random.nextInt(100) == 0;
            ModMessages.CHANNEL.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                            x, y, z, 250.0D, server.dimension())),
                    new MukeEffectPacket(x, y + 0.5D, z, balefire));
        }

        level.playSound(null, x, y, z, ModSounds.MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 15.0F, 1.0F);

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
            // Mini-nuke still gets a small torex stand-in (legacy had no MK5 for mini);
            // PARAMS_MEDIUM path uses ExplosionNT + muke FX only in legacy — torex here was a
            // port addition. Keep for medium/low visual fill until a dedicated mini FX lands.
            com.hbm.entity.effect.EntityNukeTorex.statFacStandard(level, x, y + 0.5D, z, params.blastRadius);
        }

        if (params.killRadius > 0.0F) {
            ExplosionNukeGeneric.dealDamage(level, x, y, z, params.killRadius);
        }

        if (!params.miniNuke) {
            float radius = params.blastRadius > 0.0F
                    ? params.blastRadius
                    : BombConfig.fatmanRadius.get();
            // Legacy: WorldUtil.loadAndSpawnEntityInWorld(EntityNukeExplosionMK5.statFac(...))
            // No Torex — muke particle is the mushroom visual for PARAMS_HIGH.
            EntityNukeExplosionMK5 mk5 = EntityNukeExplosionMK5.statFac(level, (int) radius, x, y, z);
            mk5.suppressFlashFx();
            level.addFreshEntity(mk5);
        }

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
        public String particle = "muke";
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

        public MukeParams particle(String value) {
            this.particle = value;
            return this;
        }
    }
}
