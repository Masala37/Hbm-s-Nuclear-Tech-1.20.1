package com.hbm.blocks.bomb;

import net.minecraft.util.StringRepresentable;

/**
 * Crashed-bomb / dud payload variants (legacy {@code BlockCrashedBomb.EnumDudType}).
 */
public enum DudType implements StringRepresentable {
    BALEFIRE("balefire"),
    CONVENTIONAL("conventional"),
    NUKE("nuke"),
    SALTED("salted");

    private final String name;

    DudType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static DudType byOrdinal(int ordinal) {
        DudType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return BALEFIRE;
        }
        return values[ordinal];
    }
}
