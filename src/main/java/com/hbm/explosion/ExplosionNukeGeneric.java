package com.hbm.explosion;

import com.hbm.entity.effect.EntityFalloutRain;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shared nuke helpers. Fallout / EMP / waste paths deferred.
 */
public final class ExplosionNukeGeneric {
    private ExplosionNukeGeneric() {
    }

    public static void dealDamage(Level level, double x, double y, double z, double radius) {
        dealDamage(level, x, y, z, radius, 250.0F);
    }

    public static void dealDamage(Level level, double x, double y, double z, double radius, float maxDamage) {
        if (level.isClientSide || radius <= 0.0D) {
            return;
        }

        AABB box = new AABB(x, y, z, x, y, z).inflate(radius);
        List<Entity> list = level.getEntities(null, box);

        for (Entity entity : list) {
            if (isExplosionExempt(entity)) {
                continue;
            }

            double dist = entity.distanceToSqr(x, y, z);
            double radiusSq = radius * radius;
            if (dist > radiusSq) {
                continue;
            }

            double distance = Math.sqrt(dist);
            double damage = maxDamage * (radius - distance) / radius;

            // Only living targets get fire/knockback; applying motion to MK5/Torex drifted the blast.
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            boolean hit = living.hurt(level.damageSources().explosion(null), (float) damage);
            if (!hit) {
                continue;
            }

            living.setSecondsOnFire(5);

            Vec3 knock = new Vec3(living.getX() - x, living.getEyeY() - y, living.getZ() - z);
            double len = knock.length();
            if (len > 1.0E-4D) {
                knock = knock.normalize().scale(0.2D);
                living.setDeltaMovement(living.getDeltaMovement().add(knock));
            }
        }
    }

    private static boolean isExplosionExempt(Entity entity) {
        if (entity instanceof ItemEntity) {
            return true;
        }
        if (entity instanceof EntityNukeExplosionMK5
                || entity instanceof EntityFalloutRain
                || entity instanceof EntityNukeTorex) {
            return true;
        }
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return true;
        }
        return false;
    }
}
