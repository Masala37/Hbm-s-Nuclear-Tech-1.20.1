package com.hbm.items.special;

import com.hbm.blocks.bomb.CrashedBombBlock;
import com.hbm.blocks.bomb.DudType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * BlockItem that preserves dud {@code type} and shows per-variant names.
 */
public class CrashedBombBlockItem extends BlockItem {
    public CrashedBombBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return CrashedBombBlock.nameFor(CrashedBombBlock.typeFromStack(stack));
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        DudType type = CrashedBombBlock.typeFromStack(stack);
        return "block.hbm.crashed_bomb." + type.getSerializedName();
    }
}
