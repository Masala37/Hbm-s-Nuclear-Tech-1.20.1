package com.hbm.entity.missile;

import com.hbm.blocks.generic.TaintBlock;
import com.hbm.entity.effect.EntityMist;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.WarheadType;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy {@code EntityMissileCustom.onMissileImpact}.
 */
public final class CustomMissileImpacts {
    private CustomMissileImpacts() {
    }

    public static void onImpact(EntityMissileCustom missile, HitResult hit) {
        Level level = missile.level();
        if (level.isClientSide) {
            return;
        }
        ItemCustomMissilePart part = missile.warhead();
        if (part == null || part.attributes == null) {
            return;
        }
        WarheadType type = (WarheadType) part.attributes[0];
        float strength = (Float) part.attributes[1];
        if (type.impactCustom != null) {
            type.impactCustom.accept(missile);
            return;
        }
        double x = missile.getX();
        double y = missile.getY();
        double z = missile.getZ();
        switch (type) {
            case HE -> {
                ExplosionLarge.explode(level, x, y, z, strength, true, false, true, missile);
                ExplosionLarge.jolt(level, x, y, z, strength, (int) (strength * 50), 0.25);
            }
            case INC -> {
                ExplosionLarge.explodeFire(level, x, y, z, strength, true, false, true);
                ExplosionLarge.jolt(level, x, y, z, strength * 1.5, (int) (strength * 50), 0.25);
            }
            case BUSTER -> ExplosionLarge.buster(level, x, y, z, missile.getDeltaMovement(), strength, strength * 4);
            case NUCLEAR, TX -> {
                level.addFreshEntity(EntityNukeExplosionMK5.statFac(level, (int) strength, x, y, z));
                EntityNukeTorex.statFacStandard(level, x, y, z, strength);
            }
            case N2 -> {
                level.addFreshEntity(EntityNukeExplosionMK5.statFacNoRad(level, (int) strength, x, y, z));
                EntityNukeTorex.statFacStandard(level, x, y, z, strength);
            }
            case BALEFIRE -> {
                level.addFreshEntity(EntityBalefire.statFac(level, x, y, z, (int) strength));
                EntityNukeTorex.statFacBale(level, x, y, z, strength);
            }
            case TURBINE -> {
                ExplosionLarge.explode(level, x, y, z, 10, true, false, true, missile);
                int count = (int) strength;
                Vec3 vec = new Vec3(0.5, 0, 0);
                Vec3 motion = missile.getDeltaMovement();
                for (int i = 0; i < count; i++) {
                    EntityShrapnel blade = new EntityShrapnel(level,
                            x - motion.x, y - motion.y + level.random.nextGaussian(), z - motion.z);
                    blade.setDeltaMovement(vec.x, 0, vec.z);
                    level.addFreshEntity(blade);
                    vec = vec.yRot((float) (Math.PI * 2.0 / (float) count));
                }
            }
            case TAINT -> {
                int r = (int) strength;
                for (int i = 0; i < r * 10; i++) {
                    int a = level.random.nextInt(r) + (int) x - (r / 2 - 1);
                    int b = level.random.nextInt(r) + (int) y - (r / 2 - 1);
                    int c = level.random.nextInt(r) + (int) z - (r / 2 - 1);
                    BlockPos pos = new BlockPos(a, b, c);
                    if (!level.isInWorldBounds(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && state.isSolidRender(level, pos)
                            && state.getDestroySpeed(level, pos) >= 0.0F) {
                        level.setBlock(pos, ModBlocks.TAINT.get().defaultBlockState()
                                .setValue(TaintBlock.AGE, level.random.nextInt(3) + 4), 2);
                    }
                }
            }
            case CLOUD -> {
                level.levelEvent(2002, BlockPos.containing(x, y, z), 0);
                Vec3 motion = missile.getDeltaMovement();
                EntityMist mist = new EntityMist(level).setArea(Math.max(4.0F, strength), 4.0F)
                        .setDuration(200).setChlorine();
                mist.setPos(x - motion.x, y - motion.y, z - motion.z);
                level.addFreshEntity(mist);
            }
            default -> {
            }
        }
    }
}
