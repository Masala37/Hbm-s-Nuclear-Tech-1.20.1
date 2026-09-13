package com.hbm.world.gen.nbt;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * One wand_jigsaw cell extracted while loading a 1.7 NBT structure.
 */
public final class JigsawConnection {
    public final Vec3i pos;
    public final Direction dir;
    public final String poolName;
    public final String targetName;
    public final boolean rollable;
    public final int selectionPriority;
    public final int placementPriority;

    public JigsawConnection(Vec3i pos, Direction dir, String poolName, String targetName,
                            boolean rollable, int selectionPriority, int placementPriority) {
        this.pos = pos;
        this.dir = dir;
        this.poolName = poolName;
        this.targetName = targetName;
        this.rollable = rollable;
        this.selectionPriority = selectionPriority;
        this.placementPriority = placementPriority;
    }
}
