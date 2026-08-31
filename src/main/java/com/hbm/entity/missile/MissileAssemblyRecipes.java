package com.hbm.entity.missile;

import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy {@code TileEntityMachineMissileAssembly} slot checks.
 */
public final class MissileAssemblyRecipes {
    private MissileAssemblyRecipes() {
    }

    public static boolean isChip(ItemStack stack) {
        ItemCustomMissilePart part = ItemCustomMissilePart.of(stack);
        return part != null && part.type == PartType.CHIP;
    }

    public static boolean isWarhead(ItemStack stack) {
        ItemCustomMissilePart part = ItemCustomMissilePart.of(stack);
        return part != null && part.type == PartType.WARHEAD;
    }

    public static boolean isFuselage(ItemStack stack) {
        ItemCustomMissilePart part = ItemCustomMissilePart.of(stack);
        return part != null && part.type == PartType.FUSELAGE;
    }

    public static boolean isFins(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemCustomMissilePart part = ItemCustomMissilePart.of(stack);
        return part != null && part.type == PartType.FINS;
    }

    public static boolean isThruster(ItemStack stack) {
        ItemCustomMissilePart part = ItemCustomMissilePart.of(stack);
        return part != null && part.type == PartType.THRUSTER;
    }

    public static int chipState(ItemStack chip) {
        return isChip(chip) ? 1 : 0;
    }

    public static int fuselageState(ItemStack fuselage) {
        return isFuselage(fuselage) ? 1 : 0;
    }

    public static int warheadState(ItemStack warhead, ItemStack fuselage, ItemStack thruster) {
        ItemCustomMissilePart part = ItemCustomMissilePart.of(warhead);
        ItemCustomMissilePart body = ItemCustomMissilePart.of(fuselage);
        ItemCustomMissilePart engine = ItemCustomMissilePart.of(thruster);
        if (part == null || body == null || engine == null) {
            return 0;
        }
        if (part.type == PartType.WARHEAD && body.type == PartType.FUSELAGE && engine.type == PartType.THRUSTER) {
            float weight = (Float) part.attributes[2];
            float thrust = (Float) engine.attributes[2];
            if (MissileSystemRules.warheadFits(part.bottom.name(), body.top.name(), weight, thrust)) {
                return 1;
            }
        }
        return 0;
    }

    public static int stabilityState(ItemStack fins, ItemStack fuselage) {
        if (fins.isEmpty()) {
            return -1;
        }
        ItemCustomMissilePart part = ItemCustomMissilePart.of(fins);
        ItemCustomMissilePart body = ItemCustomMissilePart.of(fuselage);
        if (part != null && body != null && part.type == PartType.FINS
                && MissileSystemRules.finsFit(part.top.name(), body.bottom.name())) {
            return 1;
        }
        return 0;
    }

    public static int thrusterState(ItemStack thruster, ItemStack fuselage) {
        ItemCustomMissilePart part = ItemCustomMissilePart.of(thruster);
        ItemCustomMissilePart body = ItemCustomMissilePart.of(fuselage);
        if (part != null && body != null && part.type == PartType.THRUSTER && body.type == PartType.FUSELAGE
                && MissileSystemRules.thrusterFits(
                        ((FuelType) part.attributes[0]).name(),
                        ((FuelType) body.attributes[0]).name(),
                        part.top.name(),
                        body.bottom.name())) {
            return 1;
        }
        return 0;
    }

    public static boolean canBuild(ItemStack chip, ItemStack warhead, ItemStack fuselage, ItemStack fins,
                                    ItemStack thruster, ItemStack output) {
        return MissileSystemRules.canAssemble(
                chipState(chip),
                warheadState(warhead, fuselage, thruster),
                fuselageState(fuselage),
                thrusterState(thruster, fuselage),
                stabilityState(fins, fuselage),
                output.isEmpty());
    }

    public static ItemStack construct(ItemStack chip, ItemStack warhead, ItemStack fuselage, ItemStack fins,
                                        ItemStack thruster) {
        return ItemCustomMissile.buildMissile(chip, warhead, fuselage,
                fins.isEmpty() ? null : fins, thruster);
    }
}
