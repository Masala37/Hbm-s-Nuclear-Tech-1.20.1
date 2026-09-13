package com.hbm.blocks.network;

import com.hbm.blockentity.network.PylonBlockEntity;
import com.hbm.blocks.BlockDummyable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Shared dye click and 1.7 connection tooltips.
 */
public final class PylonBlocks {
    private PylonBlocks() {
    }

    public static InteractionResult use(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos core = pos;
        if (level.getBlockState(pos).getBlock() instanceof BlockDummyable dummyable) {
            BlockPos found = dummyable.findCore(level, pos);
            if (found != null) {
                core = found;
            }
        }
        BlockEntity be = level.getBlockEntity(core);
        if (be instanceof PylonBlockEntity pylon && pylon.setColor(player.getItemInHand(hand))) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static void tooltip(PylonKindBlock block, List<Component> tooltip) {
        var kind = block.pylonKind();
        tooltip.add(Component.literal("Connection Type: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(kind.connectionType().displayName()).withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("Connection Range: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(kind.rangeLabel()).withStyle(ChatFormatting.YELLOW)));
        if (kind.requiresSubstation()) {
            tooltip.add(Component.literal("This pylon requires a substation!").withStyle(ChatFormatting.GOLD));
        }
    }
}
