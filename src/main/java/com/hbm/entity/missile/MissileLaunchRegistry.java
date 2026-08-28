package com.hbm.entity.missile;

import com.hbm.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Maps missile items to spawn factories for the launch pad.
 * <p>
 * Size/preset ideas adapted from HBM-Modernized (GPL-3.0) launchable registration.
 */
public final class MissileLaunchRegistry {
    @FunctionalInterface
    public interface Spawner {
        EntityMissileBaseNT spawn(Level level, double x, double y, double z,
                                  int targetX, int targetY, int targetZ);
    }

    private static final Map<Item, Spawner> LAUNCHABLES = new LinkedHashMap<>();
    private static boolean bootstrapped;

    private MissileLaunchRegistry() {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        register(ModItems.MISSILE_GENERIC, EntityMissileGeneric::new);
        register(ModItems.MISSILE_STRONG,
                (level, x, y, z, tx, ty, tz) -> new EntityMissileStrong(level, x, y, z, tx, ty, tz));
        register(ModItems.MISSILE_INCENDIARY,
                (level, x, y, z, tx, ty, tz) -> new EntityMissileIncendiary(level, x, y, z, tx, ty, tz, false));
        register(ModItems.MISSILE_INCENDIARY_STRONG,
                (level, x, y, z, tx, ty, tz) -> new EntityMissileIncendiary(level, x, y, z, tx, ty, tz, true));
        register(ModItems.MISSILE_CLUSTER,
                (level, x, y, z, tx, ty, tz) -> new EntityMissileCluster(level, x, y, z, tx, ty, tz, false));
        register(ModItems.MISSILE_CLUSTER_STRONG,
                (level, x, y, z, tx, ty, tz) -> new EntityMissileCluster(level, x, y, z, tx, ty, tz, true));
        register(ModItems.MISSILE_BUSTER,
                (level, x, y, z, tx, ty, tz) -> new EntityMissileBuster(level, x, y, z, tx, ty, tz, false));
        register(ModItems.MISSILE_BUSTER_STRONG,
                (level, x, y, z, tx, ty, tz) -> new EntityMissileBuster(level, x, y, z, tx, ty, tz, true));
        register(ModItems.MISSILE_TAINT, EntityMissileTaint::new);
        register(ModItems.MISSILE_MICRO, EntityMissileMicro::new);
        register(ModItems.MISSILE_BHOLE, EntityMissileBHole::new);
        register(ModItems.MISSILE_SCHRABIDIUM, EntityMissileSchrabidium::new);
        register(ModItems.MISSILE_EMP, EntityMissileEMP::new);
        register(ModItems.MISSILE_EMP_STRONG,
                (level, x, y, z, tx, ty, tz) -> new EntityMissileEMPStrong(level, x, y, z, tx, ty, tz));
        register(ModItems.MISSILE_DECOY, EntityMissileDecoy::new);
        register(ModItems.MISSILE_STEALTH, EntityMissileStealth::new);
        register(ModItems.MISSILE_BURST, EntityMissileBurst::new);
    }

    public static void register(Supplier<? extends Item> item, Spawner spawner) {
        LAUNCHABLES.put(item.get(), spawner);
    }

    public static void register(Item item, Spawner spawner) {
        LAUNCHABLES.put(item, spawner);
    }

    public static boolean isLaunchable(ItemStack stack) {
        bootstrap();
        return !stack.isEmpty() && LAUNCHABLES.containsKey(stack.getItem());
    }

    public static boolean isLaunchable(Item item) {
        bootstrap();
        return item != null && LAUNCHABLES.containsKey(item);
    }

    public static Spawner getSpawner(Item item) {
        bootstrap();
        return LAUNCHABLES.get(item);
    }

    public static Set<Item> launchables() {
        bootstrap();
        return Collections.unmodifiableSet(LAUNCHABLES.keySet());
    }
}
