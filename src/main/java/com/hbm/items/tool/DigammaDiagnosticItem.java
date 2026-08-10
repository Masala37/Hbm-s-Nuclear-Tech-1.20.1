package com.hbm.items.tool;

import com.hbm.util.ContaminationUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Digamma diagnostic (legacy digamma_diagnostic RMB readout).
 */
public class DigammaDiagnosticItem extends Item {
    public DigammaDiagnosticItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ContaminationUtil.printDiagnosticData(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
