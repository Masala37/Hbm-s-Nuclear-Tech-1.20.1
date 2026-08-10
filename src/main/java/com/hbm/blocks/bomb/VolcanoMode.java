package com.hbm.blocks.bomb;

import net.minecraft.util.StringRepresentable;

/**
 * Volcano core operating modes (legacy {@code BlockVolcano} metadata 0–4).
 */
public enum VolcanoMode implements StringRepresentable {
    STATIC_ACTIVE("static_active"),
    STATIC_EXTINGUISHING("static_extinguishing"),
    GROWING_ACTIVE("growing_active"),
    GROWING_EXTINGUISHING("growing_extinguishing"),
    SMOLDERING("smoldering");

    private final String name;

    VolcanoMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean grows() {
        return this == GROWING_ACTIVE || this == GROWING_EXTINGUISHING;
    }

    public boolean extinguishes() {
        return this == STATIC_EXTINGUISHING || this == GROWING_EXTINGUISHING;
    }

    public boolean smoldering() {
        return this == SMOLDERING;
    }
}
