package com.hbm.items.tool;

import net.minecraft.world.item.Item;

/** Opens loot crates. 1.7 {@code crowbar}. */
public class CrowbarItem extends Item {
    public CrowbarItem() {
        super(new Item.Properties().stacksTo(1).durability(250));
    }
}
