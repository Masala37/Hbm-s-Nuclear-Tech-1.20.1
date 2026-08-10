package com.hbm.items.special;

import com.hbm.registry.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Contents for working assembled-nuke kits (legacy bomb kits + N² / Balefire).
 */
public final class BombKits {
    private BombKits() {
    }

    public static List<ItemStack> gadget() {
        List<ItemStack> out = base(ModItems.NUKE_GADGET.get());
        out.add(new ItemStack(ModItems.EARLY_EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EARLY_EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EARLY_EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EARLY_EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.GADGET_WIRING.get()));
        out.add(new ItemStack(ModItems.GADGET_CORE.get()));
        return out;
    }

    public static List<ItemStack> boy() {
        List<ItemStack> out = base(ModItems.NUKE_BOY.get());
        out.add(new ItemStack(ModItems.BOY_SHIELDING.get()));
        out.add(new ItemStack(ModItems.BOY_TARGET.get()));
        out.add(new ItemStack(ModItems.BOY_BULLET.get()));
        out.add(new ItemStack(ModItems.BOY_PROPELLANT.get()));
        out.add(new ItemStack(ModItems.BOY_IGNITER.get()));
        return out;
    }

    public static List<ItemStack> man() {
        List<ItemStack> out = base(ModItems.NUKE_MAN.get());
        out.add(new ItemStack(ModItems.EARLY_EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EARLY_EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EARLY_EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EARLY_EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.MAN_IGNITER.get()));
        out.add(new ItemStack(ModItems.MAN_CORE.get()));
        return out;
    }

    public static List<ItemStack> mike() {
        List<ItemStack> out = base(ModItems.NUKE_MIKE.get());
        out.add(new ItemStack(ModItems.EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.MAN_CORE.get()));
        out.add(new ItemStack(ModItems.MIKE_CORE.get()));
        out.add(new ItemStack(ModItems.MIKE_DEUT.get()));
        out.add(new ItemStack(ModItems.MIKE_COOLING_UNIT.get()));
        return out;
    }

    public static List<ItemStack> tsar() {
        List<ItemStack> out = base(ModItems.NUKE_TSAR.get());
        out.add(new ItemStack(ModItems.EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.EXPLOSIVE_LENSES.get()));
        out.add(new ItemStack(ModItems.MAN_CORE.get()));
        out.add(new ItemStack(ModItems.TSAR_CORE.get()));
        return out;
    }

    public static List<ItemStack> fleija() {
        List<ItemStack> out = base(ModItems.NUKE_FLEIJA.get());
        out.add(new ItemStack(ModItems.FLEIJA_IGNITER.get()));
        out.add(new ItemStack(ModItems.FLEIJA_IGNITER.get()));
        out.add(new ItemStack(ModItems.FLEIJA_PROPELLANT.get()));
        out.add(new ItemStack(ModItems.FLEIJA_PROPELLANT.get()));
        out.add(new ItemStack(ModItems.FLEIJA_PROPELLANT.get()));
        for (int i = 0; i < 6; i++) {
            out.add(new ItemStack(ModItems.FLEIJA_CORE.get()));
        }
        return out;
    }

    public static List<ItemStack> solinium() {
        List<ItemStack> out = base(ModItems.NUKE_SOLINIUM.get());
        out.add(new ItemStack(ModItems.SOLINIUM_IGNITER.get()));
        out.add(new ItemStack(ModItems.SOLINIUM_IGNITER.get()));
        out.add(new ItemStack(ModItems.SOLINIUM_IGNITER.get()));
        out.add(new ItemStack(ModItems.SOLINIUM_IGNITER.get()));
        out.add(new ItemStack(ModItems.SOLINIUM_PROPELLANT.get()));
        out.add(new ItemStack(ModItems.SOLINIUM_PROPELLANT.get()));
        out.add(new ItemStack(ModItems.SOLINIUM_PROPELLANT.get()));
        out.add(new ItemStack(ModItems.SOLINIUM_PROPELLANT.get()));
        out.add(new ItemStack(ModItems.SOLINIUM_CORE.get()));
        return out;
    }

    public static List<ItemStack> n2() {
        List<ItemStack> out = base(ModItems.NUKE_N2.get());
        for (int i = 0; i < 12; i++) {
            out.add(new ItemStack(ModItems.N2_CHARGE.get()));
        }
        return out;
    }

    public static List<ItemStack> prototype() {
        List<ItemStack> out = base(ModItems.NUKE_PROTOTYPE.get());
        out.add(new ItemStack(ModItems.IGNITER.get()));
        out.add(new ItemStack(ModItems.CELL_SAS3.get(), 4));
        out.add(new ItemStack(ModItems.ROD_QUAD_URANIUM.get(), 4));
        out.add(new ItemStack(ModItems.ROD_QUAD_LEAD.get(), 4));
        out.add(new ItemStack(ModItems.ROD_QUAD_NP237.get(), 2));
        return out;
    }

    public static List<ItemStack> balefire() {
        List<ItemStack> out = base(ModItems.NUKE_FSTBMB.get());
        out.add(new ItemStack(ModItems.EGG_BALEFIRE.get()));
        out.add(new ItemStack(ModItems.BATTERY_SPARK.get()));
        return out;
    }

    public static List<ItemStack> custom() {
        List<ItemStack> out = base(ModItems.NUKE_CUSTOM.get());
        for (int i = 0; i < 6; i++) {
            out.add(new ItemStack(ModItems.CUSTOM_TNT.get()));
        }
        for (int i = 0; i < 4; i++) {
            out.add(new ItemStack(ModItems.CUSTOM_NUKE_PART.get()));
        }
        out.add(new ItemStack(ModItems.CUSTOM_HYDRO.get()));
        out.add(new ItemStack(ModItems.CUSTOM_HYDRO.get()));
        out.add(new ItemStack(ModItems.CUSTOM_AMAT.get()));
        out.add(new ItemStack(ModItems.CUSTOM_AMAT.get()));
        out.add(new ItemStack(ModItems.CUSTOM_DIRTY.get()));
        out.add(new ItemStack(ModItems.CUSTOM_DIRTY.get()));
        out.add(new ItemStack(ModItems.CUSTOM_DIRTY.get()));
        out.add(new ItemStack(ModItems.CUSTOM_SCHRAB.get()));
        out.add(new ItemStack(ModItems.CUSTOM_FALL.get()));
        return out;
    }

    public static List<ItemStack> multi() {
        List<ItemStack> out = new ArrayList<>();
        out.add(new ItemStack(ModItems.BOMB_MULTI.get(), 6));
        out.add(new ItemStack(net.minecraft.world.item.Items.TNT, 26));
        out.add(new ItemStack(net.minecraft.world.item.Items.GUNPOWDER, 2));
        out.add(new ItemStack(ModItems.PELLET_CLUSTER.get(), 2));
        out.add(new ItemStack(ModItems.POWDER_FIRE.get(), 2));
        out.add(new ItemStack(ModItems.POWDER_POISON.get(), 2));
        out.add(new ItemStack(ModItems.PELLET_GAS.get(), 2));
        out.add(new ItemStack(ModItems.DETONATOR.get()));
        return out;
    }

    private static List<ItemStack> base(net.minecraft.world.item.Item bomb) {
        List<ItemStack> out = new ArrayList<>();
        out.add(new ItemStack(bomb));
        out.add(new ItemStack(ModItems.DETONATOR.get()));
        return out;
    }
}
