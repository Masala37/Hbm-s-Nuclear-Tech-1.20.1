package com.hbm.entity.missile;

import com.hbm.blocks.generic.TaintBlock;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.entity.logic.EntityEMP;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.network.ExplosionLargeEffectPacket;
import com.hbm.network.ModMessages;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.PacketDistributor;

/**
 * Shared warhead impact helpers matching legacy {@code EntityMissileTier1}/{@code Tier2}/{@code Tier3}.
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

    /**
     * Legacy {@code ExplosionCreator.composeEffectLarge} (huge / spare warheads).
     */
    public static void composeEffectLarge(Level level, double x, double y, double z) {
        composeEffect(level, x, y, z, ExplosionLargeEffectPacket.Preset.LARGE);
    }

    private static void composeEffect(Level level, double x, double y, double z,
                                      ExplosionLargeEffectPacket.Preset preset) {
        if (level.isClientSide || !(level instanceof ServerLevel server)) {
            return;
        }
        // Legacy packet range: Math.max(300, soundRange). LARGE soundRange is 350.
        double range = preset == ExplosionLargeEffectPacket.Preset.LARGE ? 350.0D : 300.0D;
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
     * Legacy {@code EntityMissileMicro}:
     * {@code ExplosionNukeSmall.explode(..., posY + 0.5, ..., PARAMS_HIGH)}.
     * {@code PARAMS_HIGH}: not mini-nuke → MK5 dig at {@code BombConfig.fatmanRadius} (default 35),
     * no shrapnel, no kill-radius pass (MK5 deals damage itself), muke FX / sound.
     */
    public static void microNuke(Level level, Entity source) {
        if (level.isClientSide) {
            return;
        }
        ExplosionNukeSmall.explode(level, source.getX(), source.getY() + 0.5D, source.getZ(),
                ExplosionNukeSmall.PARAMS_HIGH);
    }

    /**
     * Legacy {@code EntityMissileBHole}: vanilla 1.5F blast then a 1.5-size black hole
     * at the missile position.
     */
    public static void blackHoleMicro(Level level, Entity source) {
        if (level.isClientSide) {
            return;
        }
        double x = source.getX();
        double y = source.getY();
        double z = source.getZ();
        level.explode(source, x, y, z, 1.5F, true, Level.ExplosionInteraction.TNT);
        EntityBlackHole hole = new EntityBlackHole(level, 1.5F);
        hole.setPos(x, y, z);
        level.addFreshEntity(hole);
    }

    /**
     * Legacy {@code EntityMissileTaint}: vanilla 5F blast at hit + 100 solid→taint(age 0)
     * replacements in an 11³ cube centered on the impact <em>block</em> (mop.blockX/Y/Z).
     */
    public static void taintMicro(Level level, Entity source, HitResult hit) {
        if (level.isClientSide) {
            return;
        }
        double x = hit.getLocation().x;
        double y = hit.getLocation().y;
        double z = hit.getLocation().z;
        level.explode(source, x, y, z, 5.0F, true, Level.ExplosionInteraction.TNT);
        BlockPos origin;
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            origin = blockHit.getBlockPos();
        } else {
            origin = BlockPos.containing(x, y, z);
        }
        BlockState taint = ModBlocks.TAINT.get().defaultBlockState()
                .setValue(TaintBlock.AGE, 0);
        for (int i = 0; i < 100; i++) {
            BlockPos target = origin.offset(
                    level.random.nextInt(11) - 5,
                    level.random.nextInt(11) - 5,
                    level.random.nextInt(11) - 5);
            if (!level.isInWorldBounds(target)) {
                continue;
            }
            BlockState state = level.getBlockState(target);
            if (!state.isAir() && state.isSolidRender(level, target)
                    && state.getDestroySpeed(level, target) >= 0.0F) {
                level.setBlock(target, taint, 2);
            }
        }
    }

    /**
     * Legacy {@code EntityMissileSchrabidium}: MK3 FLEIJA dig + cyan cloud at
     * {@code BombConfig.aSchrabRadius} (default 20). Cloud is skipped if the blast
     * entity is already discarded (legacy AT-suppression {@code isDead} check).
     */
    public static void schrabidiumMicro(Level level, Entity source) {
        if (level.isClientSide) {
            return;
        }
        int radius = BombConfig.aSchrabRadius.get();
        EntityNukeExplosionMK3 blast = EntityNukeExplosionMK3.statFacFleija(
                level, source.getX(), source.getY(), source.getZ(), radius);
        if (blast.isRemoved()) {
            return;
        }
        level.addFreshEntity(blast);
        EntityCloudFleija cloud = new EntityCloudFleija(level, radius);
        cloud.setPos(source.getX(), source.getY(), source.getZ());
        level.addFreshEntity(cloud);
    }

    /**
     * Legacy {@code EntityMissileEMP}: sphere EMP drain/scrap radius 50 plus expanding ring visual.
     */
    public static void empMicro(Level level, Entity source) {
        if (level.isClientSide) {
            return;
        }
        ExplosionNukeGeneric.empBlast(level,
                (int) source.getX(), (int) source.getY(), (int) source.getZ(), 50);
        EntityEMPBlast.spawn(level, source.getX(), source.getY(), source.getZ(), 50);
    }

    /**
     * Legacy {@code EntityMissileDecoy}: 4F blast, no fire, no block damage
     * ({@code newExplosion(..., 4F, false, false)} → {@link Level.ExplosionInteraction#NONE}).
     */
    public static void decoy(Level level, Entity source) {
        if (level.isClientSide) {
            return;
        }
        level.explode(source, source.getX(), source.getY(), source.getZ(),
                4.0F, false, Level.ExplosionInteraction.NONE);
    }

    /**
     * Legacy {@code EntityMissileEMPStrong}: spawn the 10-minute {@code EntityEMP} logic field.
     */
    public static void empStrong(Level level, Entity source) {
        if (level.isClientSide) {
            return;
        }
        EntityEMP.spawn(level, source.getX(), source.getY(), source.getZ());
    }

    /**
     * Legacy {@code EntityMissileBurst}: explodeStandard(50F, 48, false) + composeEffectLarge.
     */
    public static void burstTier3(Level level, Entity source, double x, double y, double z) {
        explodeStandard(level, source, x, y, z, 50.0F, 48, false);
        composeEffectLarge(level, x, y, z);
    }

    /** Alias for {@link #burstTier3} using the missile position. */
    public static void spare(Level level, Entity source) {
        burstTier3(level, source, source.getX(), source.getY(), source.getZ());
    }

    /**
     * Legacy {@code EntityMissileStealth}: explodeStandard(20F, 24, false) + composeEffectStandard.
     */
    public static void stealth(Level level, Entity source) {
        explodeStandard(level, source, source.getX(), source.getY(), source.getZ(), 20.0F, 24, false);
        composeEffectStandard(level, source.getX(), source.getY(), source.getZ());
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
