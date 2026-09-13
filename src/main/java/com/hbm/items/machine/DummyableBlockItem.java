package com.hbm.items.machine;

import com.hbm.blocks.machine.DummyablePlacement;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/**
 * {@link com.hbm.blocks.BlockDummyable} cores must be allowed to occupy dummy cells while placing.
 */
public class DummyableBlockItem extends BlockItem {
    public DummyableBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        DummyablePlacement.begin();
        try {
            return super.place(context);
        } finally {
            DummyablePlacement.end();
        }
    }
}
