package com.hbm.rbmk;

import net.minecraft.util.StringRepresentable;

public enum RBMKColumnPart implements StringRepresentable {
    CORE("core"),
    DUMMY("dummy");

    private final String name;

    RBMKColumnPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
