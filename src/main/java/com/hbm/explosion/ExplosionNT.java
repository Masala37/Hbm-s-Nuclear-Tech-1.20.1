package com.hbm.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lightweight port of legacy ExplosionNT: resistance raycast dig + entity damage.
 * Fallout, radiation, and digamma effects are intentionally omitted for now.
 */
public class ExplosionNT {
    public enum ExAttrib {
        FIRE,
        NODROP,
        NOHURT,
        NOSOUND,
        NOPARTICLE
    }

    private final Level level;
    @Nullable
    private final Entity source;
    private final double x;
    private final double y;
    private final double z;
    private final float size;
    private int resolution = 16;
    private final EnumSet<ExAttrib> attributes = EnumSet.noneOf(ExAttrib.class);
    private final Set<BlockPos> affectedBlocks = new HashSet<>();

    public ExplosionNT(Level level, @Nullable Entity source, double x, double y, double z, float size) {
        this.level = level;
        this.source = source;
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
    }

    public ExplosionNT addAttrib(ExAttrib attrib) {
        attributes.add(attrib);
        return this;
    }

    public ExplosionNT overrideResolution(int resolution) {
        this.resolution = Math.max(4, resolution);
        return this;
    }

    public void explode() {
        if (level.isClientSide) {
            return;
        }
        collectBlocks();
        if (!attributes.contains(ExAttrib.NOHURT)) {
            hurtEntities();
        }
        destroyBlocks();
        playEffects();
    }

    private void collectBlocks() {
        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                for (int k = 0; k < resolution; k++) {
                    if (i != 0 && i != resolution - 1 && j != 0 && j != resolution - 1 && k != 0 && k != resolution - 1) {
                        continue;
                    }

                    double dx = i / (double) (resolution - 1) * 2.0D - 1.0D;
                    double dy = j / (double) (resolution - 1) * 2.0D - 1.0D;
                    double dz = k / (double) (resolution - 1) * 2.0D - 1.0D;
                    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (length == 0.0D) {
                        continue;
                    }
                    dx /= length;
                    dy /= length;
                    dz /= length;

                    float power = size * (0.7F + level.random.nextFloat() * 0.6F);
                    double cx = x;
                    double cy = y;
                    double cz = z;

                    for (float step = 0.3F; power > 0.0F; power -= step * 0.75F) {
                        BlockPos pos = BlockPos.containing(cx, cy, cz);
                        if (!level.isInWorldBounds(pos)) {
                            break;
                        }

                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir()) {
                            float resistance = Math.max(0.0F, state.getExplosionResistance(level, pos, null));
                            power -= (resistance + 0.3F) * step;
                            if (power > 0.0F) {
                                affectedBlocks.add(pos.immutable());
                            }
                        }

                        cx += dx * step;
                        cy += dy * step;
                        cz += dz * step;
                    }
                }
            }
        }
    }

    private void hurtEntities() {
        float diameter = size * 2.0F;
        AABB box = new AABB(x, y, z, x, y, z).inflate(diameter + 1.0D);
        List<Entity> entities = level.getEntities(source, box);
        Vec3 center = new Vec3(x, y, z);
        DamageSource damage = explosionDamage();

        for (Entity entity : entities) {
            double distanceScale = Math.sqrt(entity.distanceToSqr(center)) / diameter;
            if (distanceScale > 1.0D) {
                continue;
            }

            double dx = entity.getX() - x;
            double dy = entity.getEyeY() - y;
            double dz = entity.getZ() - z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist == 0.0D) {
                continue;
            }
            dx /= dist;
            dy /= dist;
            dz /= dist;

            double exposure = getSeenPercent(center, entity);
            double impact = (1.0D - distanceScale) * exposure;
            float amount = (float) ((impact * impact + impact) / 2.0D * 8.0D * diameter + 1.0D);
            entity.hurt(damage, amount);

            double knock = impact;
            if (entity instanceof LivingEntity living) {
                knock = ProtectionEnchantment.getExplosionKnockbackAfterDampener(living, impact);
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(dx * knock, dy * knock, dz * knock));
        }
    }

    private static double getSeenPercent(Vec3 explosionPos, Entity entity) {
        AABB bb = entity.getBoundingBox();
        double samples = 0.0D;
        double hits = 0.0D;
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                for (int k = 0; k <= 1; k++) {
                    double sx = Mth.lerp(i, bb.minX, bb.maxX);
                    double sy = Mth.lerp(j, bb.minY, bb.maxY);
                    double sz = Mth.lerp(k, bb.minZ, bb.maxZ);
                    Vec3 sample = new Vec3(sx, sy, sz);
                    samples++;
                    BlockHitResult hit = entity.level().clip(new ClipContext(
                            explosionPos, sample, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
                    if (hit.getType() == HitResult.Type.MISS) {
                        hits++;
                    }
                }
            }
        }
        return samples == 0.0D ? 0.0D : hits / samples;
    }

    private DamageSource explosionDamage() {
        LivingEntity attacker = source instanceof LivingEntity living ? living : null;
        return level.damageSources().explosion(source, attacker);
    }

    private void destroyBlocks() {
        boolean nodrop = attributes.contains(ExAttrib.NODROP);
        boolean fire = attributes.contains(ExAttrib.FIRE);
        Explosion vanilla = new Explosion(level, source, null, null, x, y, z, size, false, Explosion.BlockInteraction.DESTROY);

        for (BlockPos pos : affectedBlocks) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            if (!nodrop && state.canDropFromExplosion(level, pos, vanilla)
                    && level.random.nextFloat() <= (1.0F / Math.max(1.0F, size))) {
                Block.dropResources(state, level, pos, level.getBlockEntity(pos));
            }

            state.onBlockExploded(level, pos, vanilla);

            if (fire && level.random.nextInt(3) == 0 && level.getBlockState(pos).isAir()) {
                BlockPos below = pos.below();
                if (level.getBlockState(below).isSolidRender(level, below)) {
                    level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                }
            }
        }
    }

    private void playEffects() {
        if (!attributes.contains(ExAttrib.NOSOUND)) {
            level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                    4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
        }

        if (!attributes.contains(ExAttrib.NOPARTICLE) && level instanceof ServerLevel server) {
            if (size >= 2.0F) {
                server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            } else {
                server.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
