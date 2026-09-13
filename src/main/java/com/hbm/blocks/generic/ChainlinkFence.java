package com.hbm.blocks.generic;

/**
 * 1.7.10 {@code BlockMetalFence} / {@code RenderFence} connection and pole rules.
 */
public final class ChainlinkFence {
    private ChainlinkFence() {
    }

    /**
     * Pole (post model) is shown for the placed post item, or whenever the run is not a straight
     * east-west / north-south span.
     */
    public static boolean showPole(boolean post, boolean north, boolean east, boolean south, boolean west) {
        boolean hasX = west || east;
        boolean hasZ = north || south;
        boolean straightX = !hasZ && west && east;
        boolean straightZ = !hasX && north && south;
        return post || (!straightX && !straightZ);
    }

    /** North-south collision slab (x 6–10), used when connected north or south. */
    public static boolean usesNorthSouthBox(boolean north, boolean south) {
        return north || south;
    }

    /**
     * East-west collision slab (z 6–10). Also used for a lone post when there is no north-south run
     * ({@code !north && !south} in 1.7 {@code addCollisionBoxesToList}).
     */
    public static boolean usesEastWestBox(boolean north, boolean east, boolean south, boolean west) {
        return east || west || !(north || south);
    }
}
