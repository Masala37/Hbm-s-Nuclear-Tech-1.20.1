package com.hbm.entity.missile;

/**
 * Pad / assembly / interceptor rules used by the launch pad and custom missiles.
 */
public final class MissileSystemRules {
    public static final String SIZE_10 = "SIZE_10";
    public static final String SIZE_15 = "SIZE_15";
    public static final String SIZE_20 = "SIZE_20";

    public static final String FUEL_SOLID = "SOLID";
    public static final String FUEL_XENON = "XENON";

    public static final int ABM_CLIMB_TICKS = 40;
    public static final double ABM_PROXIMITY = 10.0D;
    public static final float ABM_PROXIMITY_BLAST = 15.0F;
    public static final float ABM_GROUND_BLAST = 20.0F;
    public static final double ABM_SEARCH = 1_000.0D;
    public static final int ABM_MAX_TICKS_NO_TARGET = 600;
    public static final double ABM_CEILING = 2_000.0D;
    public static final double ABM_BASE_SPEED = 1.5D;

    public static final int PAD_LAUNCH_COST = 75_000;
    public static final int PAD_TANK = 24_000;
    public static final int PAD_COOLDOWN = 100;

    public static final int COMPACT_TANK = 25_000;
    public static final int TABLE_TANK = 100_000;
    public static final int COMPACT_SOLID = 25_000;
    public static final int TABLE_SOLID = 100_000;
    public static final int ROCKET_FUEL_PER_ITEM = 250;
    public static final int LAUNCHER_MAX_POWER = 100_000;
    public static final int LAUNCHER_LAUNCH_COST = 75_000;

    private MissileSystemRules() {
    }

    public static boolean isPadLaunchableFuselageTop(String topSize) {
        return SIZE_10.equals(topSize) || SIZE_15.equals(topSize);
    }

    public static boolean isPadLaunchable(String topSize, String bottomSize) {
        return isPadLaunchableFuselageTop(topSize) && !SIZE_20.equals(bottomSize);
    }

    /** Compact launcher: fuselage {@code top == SIZE_10} (includes 10/15 adapters). */
    public static boolean compactAcceptsTop(String topSize) {
        return SIZE_10.equals(topSize);
    }

    /** Launch table: fuselage {@code top} must match the selected pad size. */
    public static boolean tableAcceptsTop(String topSize, String padSize) {
        return topSize != null && topSize.equals(padSize);
    }

    public static boolean launcherPowerReady(int energy, int maxPower) {
        return energy >= (int) (maxPower * 0.75D);
    }

    public static boolean compactCanLaunch(boolean powerReady, boolean missileValid,
                                               boolean hasDesignator, boolean hasFuel) {
        return powerReady && missileValid && hasDesignator && hasFuel;
    }

    public static boolean tableCanLaunch(boolean powerReady, boolean missileValid, boolean hasFuel) {
        return powerReady && missileValid && hasFuel;
    }

    /**
     * Lamp: {@code -1} unused, {@code 0} insufficient, {@code 1} ready.
     * {@code hasFuel} is true when none of the three lamps is 0.
     */
    public static int fuelLamp(boolean used, int stored, float need) {
        if (!used) {
            return -1;
        }
        return stored >= need ? 1 : 0;
    }

    public static boolean hasLauncherFuel(int solidState, int liquidState, int oxidizerState) {
        return solidState != 0 && liquidState != 0 && oxidizerState != 0;
    }

    public static boolean usesSolidFuel(String fuelType) {
        return FUEL_SOLID.equals(fuelType);
    }

    public static boolean usesLiquidFuel(String fuelType) {
        return "KEROSENE".equals(fuelType)
                || "HYDROGEN".equals(fuelType)
                || FUEL_XENON.equals(fuelType)
                || "BALEFIRE".equals(fuelType);
    }

    public static boolean usesOxidizer(String fuelType) {
        return "KEROSENE".equals(fuelType)
                || "HYDROGEN".equals(fuelType)
                || "BALEFIRE".equals(fuelType);
    }

    /**
     * SOUTH/NORTH: ports along X, plates on Z. WEST/EAST: plates on X, ports on Z.
     * Corners are always ports. {@code facing} is the player look used on place.
     */
    public static boolean launchTableIsPlate(int dx, int dz, String facing) {
        if (dx == 0 && dz == 0) {
            return false;
        }
        boolean portsOnX = "south".equals(facing) || "north".equals(facing);
        if (portsOnX) {
            return dx == 0 && dz != 0;
        }
        return dz == 0 && dx != 0;
    }

    public static boolean launchTableIsPort(int dx, int dz, String facing) {
        return (dx != 0 || dz != 0) && !launchTableIsPlate(dx, dz, facing);
    }

    /** 1.7.10 launch-table TESR yaw from place meta (SOUTH 90, WEST 0, NORTH 270, EAST 180). */
    public static float launchTableYaw(String facing) {
        return switch (facing) {
            case "south" -> 90.0F;
            case "west" -> 0.0F;
            case "north" -> 270.0F;
            default -> 180.0F;
        };
    }

    public static int fuelCapacity(String fuelType, float tank) {
        if (FUEL_SOLID.equals(fuelType) || FUEL_XENON.equals(fuelType)) {
            return 0;
        }
        return (int) tank;
    }

    public static boolean warheadFits(String warheadBottom, String fuselageTop, float weight, float lift) {
        return warheadBottom.equals(fuselageTop) && weight <= lift;
    }

    public static boolean finsFit(String finSize, String fuselageBottom) {
        return finSize.equals(fuselageBottom);
    }

    public static boolean thrusterFits(String thrusterFuel, String fuselageFuel,
                                         String thrusterTop, String fuselageBottom) {
        return thrusterFuel.equals(fuselageFuel) && thrusterTop.equals(fuselageBottom);
    }

    public static boolean canAssemble(int chipState, int warheadState, int fuselageState,
                                        int thrusterState, int stabilityState, boolean outputEmpty) {
        return outputEmpty
                && chipState == 1
                && warheadState == 1
                && fuselageState == 1
                && thrusterState == 1
                && stabilityState != 0;
    }

    public static boolean abmTracks(boolean stealthTarget) {
        return !stealthTarget;
    }

    public static boolean abmProximityDetonate(double distance, int activationTimer) {
        return activationTimer >= ABM_CLIMB_TICKS && distance < ABM_PROXIMITY;
    }

    public static boolean abmArmed(int activationTimer) {
        return activationTimer >= ABM_CLIMB_TICKS;
    }

    public static boolean abmGiveUp(int tickCount, boolean hasLiveTarget, double y) {
        if (y > ABM_CEILING && !hasLiveTarget) {
            return true;
        }
        return !hasLiveTarget && tickCount > ABM_MAX_TICKS_NO_TARGET;
    }

    public static boolean canLaunch(boolean missileValid, boolean hasFuel, int energy, int delay,
                                     boolean needsDesignator, boolean hasTarget) {
        return missileValid
                && hasFuel
                && energy >= PAD_LAUNCH_COST
                && delay <= 0
                && (!needsDesignator || hasTarget);
    }

    /**
     * Compact-launcher miss: scale (pad − target) by chip × fin, then rotate.
     * {@code angle} is passed to {@code Math.cos}/{@code Math.sin} as-is.
     */
    public static int[] scatterTarget(int padX, int padZ, int targetX, int targetZ,
                                       float chip, float fin, float angle) {
        double dx = (padX - targetX) * (double) chip * (double) fin;
        double dz = (padZ - targetZ) * (double) chip * (double) fin;
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        double rx = dx * cos + dz * sin;
        double rz = dz * cos - dx * sin;
        return new int[] { targetX + (int) rx, targetZ + (int) rz };
    }
}
