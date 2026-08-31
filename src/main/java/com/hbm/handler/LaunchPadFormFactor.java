package com.hbm.handler;

import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.MissileItem;
import net.minecraft.world.item.ItemStack;

/**
 * 1.7.10 {@code ItemMissile.MissileFormFactor} — erector mesh set on the large pad.
 */
public enum LaunchPadFormFactor {
    ABM(1.5D, 1.25D, "ABM_Pad", "ABM_Erector", "ABM_Pivot", "ABM_Rope", "erector_abm"),
    MICRO(1.5D, 1.25D, "Micro_Pad", "Micro_Erector", "Micro_Pivot", "Micro_Rope", "erector_micro"),
    V2(1.75D, 1.25D, "V2_Pad", "V2_Erector", "V2_Pivot", "V2_Rope", "erector_v2"),
    STRONG(3.0D, 1.5D, "Strong_Pad", "Strong_Erector", "Strong_Pivot", "Strong_Rope", "erector_strong"),
    HUGE(3.0D, 1.5D, "Huge_Pad", "Huge_Erector", "Huge_Pivot", "Huge_Rope", "erector_huge"),
    ATLAS(4.0D, 1.5D, "Atlas_Pad", "Atlas_Erector", "Atlas_Pivot", "Atlas_Rope", "erector_atlas"),
    OTHER(1.5D, 1.25D, "ABM_Pad", "ABM_Erector", "ABM_Pivot", "ABM_Rope", "erector_abm");

    public final double offsetZ;
    public final double offsetY;
    public final String padPart;
    public final String erectorPart;
    public final String pivotPart;
    public final String ropePart;
    public final String texture;

    LaunchPadFormFactor(double offsetZ, double offsetY, String padPart, String erectorPart,
                        String pivotPart, String ropePart, String texture) {
        this.offsetZ = offsetZ;
        this.offsetY = offsetY;
        this.padPart = padPart;
        this.erectorPart = erectorPart;
        this.pivotPart = pivotPart;
        this.ropePart = ropePart;
        this.texture = texture;
    }

    public boolean slowErector() {
        return this == ATLAS || this == HUGE;
    }

    public static LaunchPadFormFactor of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return V2;
        }
        if (stack.getItem() instanceof MissileItem mi) {
            return switch (mi.getTier()) {
                case ABM -> ABM;
                case TIER0 -> MICRO;
                case TIER1 -> V2;
                case TIER2, STEALTH -> STRONG;
                case TIER3 -> HUGE;
                case TIER4 -> ATLAS;
                case ROBIN -> OTHER;
            };
        }
        if (stack.getItem() instanceof ItemCustomMissile) {
            ItemCustomMissilePart fuselage = ItemCustomMissile.readPart(stack, ItemCustomMissile.TAG_FUSELAGE);
            if (fuselage != null && fuselage.top != null) {
                return switch (fuselage.top) {
                    case SIZE_15 -> STRONG;
                    case SIZE_20 -> HUGE;
                    default -> V2;
                };
            }
        }
        return V2;
    }
}
