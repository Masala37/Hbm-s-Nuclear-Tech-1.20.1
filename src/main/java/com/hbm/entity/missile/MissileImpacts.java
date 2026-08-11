package com.hbm.entity.missile;

import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.network.ExplosionLargeEffectPacket;
import com.hbm.network.ModMessages;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

/**
 * Shared warhead impact helpers matching legacy {@code EntityMissileTier1}/{@code Tier2}.
 */
public final class MissileImpacts {
    private MissileImpacts() {
    }

    /**
     * Stand-in for legacy {@code explodeStandard} via {@link ExplosionNT}
     * (resolution + {@code NODROP}, optional {@code FIRE}).
     */
    public static void explodeStandard(Level level, Entity source, double x, double y, double z,
                                       float strength, int resolution, boolean fire) {
        if (level.isClientSide) {
            return;
        }
        ExplosionNT blast = new ExplosionNT(level, source, x, y, z, strength)
                .overrideResolution(resolution)
                .addAttrib(ExplosionNT.ExAttrib.NODROP);
        if (fire) {
            blast.addAttrib(ExplosionNT.ExAttrib.FIRE);
        }
        blast.explode();
    }

    /**
     * Legacy {@code ExplosionCreator.composeEffectSmall}: client near/far bang + muke wave +
     * rocket-flame plume + WorldInAJar {@code ParticleDebris}.
     */
    public static void composeEffectSmall(Level level, double x, double y, double z) {
        composeEffect(level, x, y, z, ExplosionLargeEffectPacket.Preset.SMALL);
    }

    /**
     * Legacy {@code ExplosionCreator.composeEffectStandard} (strong / medium warheads).
     */
    public static void composeEffectStandard(Level level, double x, double y, double z) {
        composeEffect(level, x, y, z, ExplosionLargeEffectPacket.Preset.STANDARD);
    }

    private static void composeEffect(Level level, double x, double y, double z,
                                      ExplosionLargeEffectPacket.Preset preset) {
        if (level.isClientSide || !(level instanceof ServerLevel server)) {
            return;
        }
        // Legacy packet range: Math.max(300, soundRange).
        double range = 300.0D;
        ModMessages.CHANNEL.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        x, y, z, range, server.dimension())),
                new ExplosionLargeEffectPacket(x, y, z, preset));
    }

    /** Legacy Tier-1 generic HE. */
    public static void heTier1(Level level, Entity source, double x, double y, double z) {
        explodeStandard(level, source, x, y, z, 15.0F, 24, false);
        composeEffectSmall(level, x, y, z);
    }

    /** Legacy Tier-1 incendiary (FIRE attrib, no Thermo scatter). */
    public static void incendiaryTier1(Level level, Entity source, double x, double y, double z) {
        explodeStandard(level, source, x, y, z, 15.0F, 24, true);
        composeEffectSmall(level, x, y, z);
    }

    /**
     * Legacy Tier-1 cluster: vanilla 5F casing blast + {@link ExplosionChaos#cluster} ×25.
     */
    public static void clusterTier1(Level level, Entity source, double x, double y, double z,
                                    float yawDeg, float pitchDeg) {
        if (level.isClientSide) {
            return;
        }
        level.explode(source, x, y, z, 5.0F, Level.ExplosionInteraction.TNT);
        float yawRad = (float) Math.toRadians(yawDeg);
        float pitchRad = (float) Math.toRadians(pitchDeg);
        ExplosionChaos.cluster(level, x, y, z, 25, yawRad, pitchRad,
                (float) Math.PI * 0.25F, (float) Math.PI * 0.25F, 1.0F);
    }

    /**
     * Legacy Tier-1 bunker buster: 15 vertical digs @5F, debris FX, no follow-up HE shell.
     * Digs use {@link ExplosionNT} (vanilla TNT stacking is weak/noisy on 1.20.1).
     */
    public static void busterTier1(Level level, Entity source, double x, double y, double z) {
        digBunkerShaft(level, source, x, y, z, 15, 5.0F);
        ExplosionLarge.spawnParticles(level, x, y, z, 5);
        ExplosionLarge.spawnShrapnels(level, x, y, z, 5);
        ExplosionLarge.spawnRubble(level, x, y, z, 5);
    }

    /** Legacy Tier-2 strong HE: {@code explodeStandard(30, 32)} + composeEffectStandard. */
    public static void heTier2(Level level, Entity source, double x, double y, double z) {
        explodeStandard(level, source, x, y, z, 30.0F, 32, false);
        composeEffectStandard(level, x, y, z);
    }

    /**
     * Legacy Tier-2 strong incendiary: FIRE blast + composeEffectStandard + flammable ignite ×25.
     */
    public static void incendiaryTier2(Level level, Entity source, double x, double y, double z) {
        explodeStandard(level, source, x, y, z, 30.0F, 32, true);
        composeEffectStandard(level, x, y, z);
        ExplosionChaos.igniteFlammableBlocks(level,
                Mth.floor(x + 0.5F), Mth.floor(y + 0.5F), Mth.floor(z + 0.5F), 25);
    }

    /**
     * Legacy Tier-2 strong cluster: vanilla 15F casing + {@link ExplosionChaos#cluster} ×50.
     */
    public static void clusterTier2(Level level, Entity source, double x, double y, double z,
                                    float yawDeg, float pitchDeg) {
        if (level.isClientSide) {
            return;
        }
        level.explode(source, x, y, z, 15.0F, Level.ExplosionInteraction.TNT);
        float yawRad = (float) Math.toRadians(yawDeg);
        float pitchRad = (float) Math.toRadians(pitchDeg);
        ExplosionChaos.cluster(level, x, y, z, 50, yawRad, pitchRad,
                (float) Math.PI * 0.25F, (float) Math.PI * 0.25F, 1.0F);
    }

    /**
     * Legacy Tier-2 strong bunker buster: 20 digs @7.5F + debris ×8 (no follow-up HE).
     */
    public static void busterTier2(Level level, Entity source, double x, double y, double z) {
        digBunkerShaft(level, source, x, y, z, 20, 7.5F);
        ExplosionLarge.spawnParticles(level, x, y, z, 8);
        ExplosionLarge.spawnShrapnels(level, x, y, z, 8);
        ExplosionLarge.spawnRubble(level, x, y, z, 8);
    }

    /**
     * Vertical bunker shaft. Dig layers are silent (HBM bang comes from smoke cloud packet);
     * only the surface layer deals entity damage.
     */
    private static void digBunkerShaft(Level level, Entity source, double x, double y, double z,
                                       int depth, float strength) {
        if (level.isClientSide || depth <= 0) {
            return;
        }
        int resolution = Math.max(16, Mth.ceil(strength * 3.0F));
        for (int i = 0; i < depth; i++) {
            ExplosionNT blast = new ExplosionNT(level, source, x, y - i, z, strength)
                    .overrideResolution(resolution)
                    .addAttrib(ExplosionNT.ExAttrib.NODROP)
                    .addAttrib(ExplosionNT.ExAttrib.NOSOUND)
                    .addAttrib(ExplosionNT.ExAttrib.NOPARTICLE);
            if (i > 0) {
                blast.addAttrib(ExplosionNT.ExAttrib.NOHURT);
            }
            blast.explode();
        }
    }
}
