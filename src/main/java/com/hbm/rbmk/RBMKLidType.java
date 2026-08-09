package com.hbm.rbmk;

import net.minecraft.util.StringRepresentable;

public enum RBMKLidType implements StringRepresentable {
    NONE("none"),
    CONCRETE("concrete"),
    GLASS("glass");

    private final String name;

    RBMKLidType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean hasLid() {
        return this != NONE;
    }
}
