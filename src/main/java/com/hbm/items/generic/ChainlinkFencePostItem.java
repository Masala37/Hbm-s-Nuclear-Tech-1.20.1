package com.hbm.items.generic;

import com.hbm.blocks.generic.ChainlinkFenceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Places {@link ChainlinkFenceBlock} with the forced pole (legacy meta 1).
 */
public class ChainlinkFencePostItem extends BlockItem {
    public ChainlinkFencePostItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public String getDescriptionId() {
        return "block.hbm.fence_metal_post";
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = getBlock().getStateForPlacement(context);
        if (state == null || !canPlace(context, state)) {
            return null;
        }
        return ((ChainlinkFenceBlock) getBlock()).withForcedPost(state, true);
    }
}
