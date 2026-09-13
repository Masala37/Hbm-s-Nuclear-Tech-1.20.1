package com.hbm.items.machine;

import com.hbm.inventory.recipes.StampType;
import net.minecraft.world.item.Item;

public class ItemStamp extends Item {
    private final StampType type;

    public ItemStamp(int durability, StampType type) {
        super(durability > 0
                ? new Item.Properties().stacksTo(1).durability(durability)
                : new Item.Properties().stacksTo(1));
        this.type = type;
    }

    public StampType getStampType() {
        return type;
    }
}
