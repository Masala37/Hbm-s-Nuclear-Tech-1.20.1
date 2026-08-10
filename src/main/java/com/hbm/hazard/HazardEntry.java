package com.hbm.hazard;

import com.hbm.hazard.type.HazardTypeBase;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Single hazard binding on an item (legacy {@code HazardEntry}, modifiers deferred).
 */
public class HazardEntry {

    private final HazardTypeBase type;
    private final float baseLevel;

    public HazardEntry(HazardTypeBase type) {
        this(type, 1.0F);
    }

    public HazardEntry(HazardTypeBase type, float level) {
        this.type = type;
        this.baseLevel = level;
    }

    public void applyHazard(ItemStack stack, LivingEntity entity) {
        type.onUpdate(entity, baseLevel, stack);
    }

    public HazardTypeBase getType() {
        return type;
    }

    public float getBaseLevel() {
        return baseLevel;
    }

    public HazardEntry clone(float mult) {
        return new HazardEntry(type, baseLevel * mult);
    }
}
