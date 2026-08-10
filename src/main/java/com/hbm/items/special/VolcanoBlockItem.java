package com.hbm.items.special;

import com.hbm.blocks.bomb.VolcanoBlock;
import com.hbm.blocks.bomb.VolcanoMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Volcano core item preserving {@link VolcanoMode}.
 */
public class VolcanoBlockItem extends BlockItem {
    public VolcanoBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Block block = getBlock();
        boolean rad = block instanceof VolcanoBlock volcano && volcano.isRadioactive();
        return VolcanoBlock.nameFor(rad, VolcanoBlock.modeFromStack(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        VolcanoMode mode = VolcanoBlock.modeFromStack(stack);
        if (mode.smoldering()) {
            tooltip.add(Component.translatable("block.hbm.volcano_core.tip.shield"));
            return;
        }
        tooltip.add(Component.translatable(mode.grows()
                ? "block.hbm.volcano_core.tip.grows"
                : "block.hbm.volcano_core.tip.no_grow"));
        tooltip.add(Component.translatable(mode.extinguishes()
                ? "block.hbm.volcano_core.tip.extinguishes"
                : "block.hbm.volcano_core.tip.no_extinguish"));
    }
}
