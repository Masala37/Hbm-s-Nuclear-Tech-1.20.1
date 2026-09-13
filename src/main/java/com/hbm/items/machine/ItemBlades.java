package com.hbm.items.machine;

import net.minecraft.world.item.Item;

public class ItemBlades extends Item {
    public ItemBlades(int durability) {
        super(durability > 0
                ? new Item.Properties().stacksTo(1).durability(durability)
                : new Item.Properties().stacksTo(1));
    }
}
