package com.hbm.handler;

import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MissileStruct {
    public ItemCustomMissilePart warhead;
    public ItemCustomMissilePart fuselage;
    public ItemCustomMissilePart fins;
    public ItemCustomMissilePart thruster;

    public MissileStruct() {
    }

    public MissileStruct(ItemStack w, ItemStack f, ItemStack s, ItemStack t) {
        warhead = ItemCustomMissilePart.of(w);
        fuselage = ItemCustomMissilePart.of(f);
        fins = ItemCustomMissilePart.of(s);
        thruster = ItemCustomMissilePart.of(t);
        if (warhead != null && warhead.type != PartType.WARHEAD) {
            warhead = null;
        }
        if (fuselage != null && fuselage.type != PartType.FUSELAGE) {
            fuselage = null;
        }
        if (fins != null && fins.type != PartType.FINS) {
            fins = null;
        }
        if (thruster != null && thruster.type != PartType.THRUSTER) {
            thruster = null;
        }
    }

    public MissileStruct(Item w, Item f, Item s, Item t) {
        warhead = w instanceof ItemCustomMissilePart p && p.type == PartType.WARHEAD ? p : null;
        fuselage = f instanceof ItemCustomMissilePart p && p.type == PartType.FUSELAGE ? p : null;
        fins = s instanceof ItemCustomMissilePart p && p.type == PartType.FINS ? p : null;
        thruster = t instanceof ItemCustomMissilePart p && p.type == PartType.THRUSTER ? p : null;
    }

    public boolean isComplete() {
        return warhead != null && fuselage != null && thruster != null;
    }
}
