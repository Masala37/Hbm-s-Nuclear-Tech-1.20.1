package com.hbm.rbmk;

import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class RBMKBlockStateProperties {
    public static final EnumProperty<RBMKColumnPart> COLUMN_PART =
            EnumProperty.create("part", RBMKColumnPart.class);

    /** Dummy segment index: 1 = first block above core. Unused on core (0). */
    public static final IntegerProperty SEGMENT =
            IntegerProperty.create("segment", 0, 8);

    public static final EnumProperty<RBMKLidType> LID =
            EnumProperty.create("lid", RBMKLidType.class);

    private RBMKBlockStateProperties() {
    }
}
