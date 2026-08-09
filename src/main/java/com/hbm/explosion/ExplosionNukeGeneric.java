package com.hbm.explosion;

import net.minecraft.world.entity.Entity;
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
        if (level.isClientSide) {
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
            entity.hurt(level.damageSources().explosion(null), (float) damage);
            entity.setSecondsOnFire(5);

            Vec3 knock = new Vec3(entity.getX() - x, entity.getEyeY() - y, entity.getZ() - z);
            double len = knock.length();
            if (len > 1.0E-4D) {
                knock = knock.normalize().scale(0.2D);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knock));
            }
        }
    }

    private static boolean isExplosionExempt(Entity entity) {
        if (entity instanceof ItemEntity) {
            return true;
        }
        if (entity instanceof Player player && player.isCreative()) {
            return true;
        }
        return false;
    }
}
